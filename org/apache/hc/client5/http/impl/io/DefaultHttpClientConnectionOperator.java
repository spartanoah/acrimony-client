/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.hc.client5.http.ConnectExceptionSupport;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.UnsupportedSchemeException;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.io.HttpClientConnectionOperator;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultHttpClientConnectionOperator
implements HttpClientConnectionOperator {
    static final String SOCKET_FACTORY_REGISTRY = "http.socket-factory-registry";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpClientConnectionOperator.class);
    private final Lookup<ConnectionSocketFactory> socketFactoryRegistry;
    private final SchemePortResolver schemePortResolver;
    private final DnsResolver dnsResolver;

    public DefaultHttpClientConnectionOperator(Lookup<ConnectionSocketFactory> socketFactoryRegistry, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        Args.notNull(socketFactoryRegistry, "Socket factory registry");
        this.socketFactoryRegistry = socketFactoryRegistry;
        this.schemePortResolver = schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE;
        this.dnsResolver = dnsResolver != null ? dnsResolver : SystemDefaultDnsResolver.INSTANCE;
    }

    private Lookup<ConnectionSocketFactory> getSocketFactoryRegistry(HttpContext context) {
        Lookup<ConnectionSocketFactory> reg = (Lookup<ConnectionSocketFactory>)context.getAttribute(SOCKET_FACTORY_REGISTRY);
        if (reg == null) {
            reg = this.socketFactoryRegistry;
        }
        return reg;
    }

    @Override
    public void connect(ManagedHttpClientConnection conn, HttpHost host, InetSocketAddress localAddress, TimeValue connectTimeout, SocketConfig socketConfig, HttpContext context) throws IOException {
        InetAddress[] inetAddressArray;
        Args.notNull(conn, "Connection");
        Args.notNull(host, "Host");
        Args.notNull(socketConfig, "Socket config");
        Args.notNull(context, "Context");
        Lookup<ConnectionSocketFactory> registry = this.getSocketFactoryRegistry(context);
        ConnectionSocketFactory sf = registry.lookup(host.getSchemeName());
        if (sf == null) {
            throw new UnsupportedSchemeException(host.getSchemeName() + " protocol is not supported");
        }
        if (host.getAddress() != null) {
            InetAddress[] inetAddressArray2 = new InetAddress[1];
            inetAddressArray = inetAddressArray2;
            inetAddressArray2[0] = host.getAddress();
        } else {
            inetAddressArray = this.dnsResolver.resolve(host.getHostName());
        }
        InetAddress[] addresses = inetAddressArray;
        int port = this.schemePortResolver.resolve(host);
        for (int i = 0; i < addresses.length; ++i) {
            int linger;
            InetAddress address = addresses[i];
            boolean last = i == addresses.length - 1;
            Socket sock = sf.createSocket(context);
            sock.setSoTimeout(socketConfig.getSoTimeout().toMillisecondsIntBound());
            sock.setReuseAddress(socketConfig.isSoReuseAddress());
            sock.setTcpNoDelay(socketConfig.isTcpNoDelay());
            sock.setKeepAlive(socketConfig.isSoKeepAlive());
            if (socketConfig.getRcvBufSize() > 0) {
                sock.setReceiveBufferSize(socketConfig.getRcvBufSize());
            }
            if (socketConfig.getSndBufSize() > 0) {
                sock.setSendBufferSize(socketConfig.getSndBufSize());
            }
            if ((linger = socketConfig.getSoLinger().toMillisecondsIntBound()) >= 0) {
                sock.setSoLinger(true, linger);
            }
            conn.bind(sock);
            InetSocketAddress remoteAddress = new InetSocketAddress(address, port);
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: connecting to {}", (Object)ConnPoolSupport.getId(conn), (Object)remoteAddress);
            }
            try {
                sock = sf.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
                conn.bind(sock);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: connection established {}", (Object)ConnPoolSupport.getId(conn), (Object)conn);
                }
                return;
            } catch (IOException ex) {
                if (last) {
                    throw ConnectExceptionSupport.enhance(ex, host, addresses);
                }
                if (!LOG.isDebugEnabled()) continue;
                LOG.debug("{}: connect to {} timed out. Connection will be retried using another IP address", (Object)ConnPoolSupport.getId(conn), (Object)remoteAddress);
                continue;
            }
        }
    }

    @Override
    public void upgrade(ManagedHttpClientConnection conn, HttpHost host, HttpContext context) throws IOException {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        Lookup<ConnectionSocketFactory> registry = this.getSocketFactoryRegistry(clientContext);
        ConnectionSocketFactory sf = registry.lookup(host.getSchemeName());
        if (sf == null) {
            throw new UnsupportedSchemeException(host.getSchemeName() + " protocol is not supported");
        }
        if (!(sf instanceof LayeredConnectionSocketFactory)) {
            throw new UnsupportedSchemeException(host.getSchemeName() + " protocol does not support connection upgrade");
        }
        LayeredConnectionSocketFactory lsf = (LayeredConnectionSocketFactory)sf;
        Socket sock = conn.getSocket();
        if (sock == null) {
            throw new ConnectionClosedException("Connection is closed");
        }
        int port = this.schemePortResolver.resolve(host);
        sock = lsf.createLayeredSocket(sock, host.getHostName(), port, context);
        conn.bind(sock);
    }
}

