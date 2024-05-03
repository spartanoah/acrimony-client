/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.conn;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Lookup;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionOperator;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class DefaultHttpClientConnectionOperator
implements HttpClientConnectionOperator {
    static final String SOCKET_FACTORY_REGISTRY = "http.socket-factory-registry";
    private final Log log = LogFactory.getLog(this.getClass());
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
    public void connect(ManagedHttpClientConnection conn, HttpHost host, InetSocketAddress localAddress, int connectTimeout, SocketConfig socketConfig, HttpContext context) throws IOException {
        InetAddress[] inetAddressArray;
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
            InetSocketAddress remoteAddress;
            block15: {
                int linger;
                InetAddress address = addresses[i];
                boolean last = i == addresses.length - 1;
                Socket sock = sf.createSocket(context);
                sock.setSoTimeout(socketConfig.getSoTimeout());
                sock.setReuseAddress(socketConfig.isSoReuseAddress());
                sock.setTcpNoDelay(socketConfig.isTcpNoDelay());
                sock.setKeepAlive(socketConfig.isSoKeepAlive());
                if (socketConfig.getRcvBufSize() > 0) {
                    sock.setReceiveBufferSize(socketConfig.getRcvBufSize());
                }
                if (socketConfig.getSndBufSize() > 0) {
                    sock.setSendBufferSize(socketConfig.getSndBufSize());
                }
                if ((linger = socketConfig.getSoLinger()) >= 0) {
                    sock.setSoLinger(true, linger);
                }
                conn.bind(sock);
                remoteAddress = new InetSocketAddress(address, port);
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Connecting to " + remoteAddress);
                }
                try {
                    sock = sf.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
                    conn.bind(sock);
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Connection established " + conn);
                    }
                    return;
                } catch (SocketTimeoutException ex) {
                    if (last) {
                        throw new ConnectTimeoutException(ex, host, addresses);
                    }
                } catch (ConnectException ex) {
                    if (last) {
                        String msg = ex.getMessage();
                        throw "Connection timed out".equals(msg) ? new ConnectTimeoutException(ex, host, addresses) : new HttpHostConnectException(ex, host, addresses);
                    }
                } catch (NoRouteToHostException ex) {
                    if (!last) break block15;
                    throw ex;
                }
            }
            if (!this.log.isDebugEnabled()) continue;
            this.log.debug("Connect to " + remoteAddress + " timed out. " + "Connection will be retried using another IP address");
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
        int port = this.schemePortResolver.resolve(host);
        sock = lsf.createLayeredSocket(sock, host.getHostName(), port, context);
        conn.bind(sock);
    }
}

