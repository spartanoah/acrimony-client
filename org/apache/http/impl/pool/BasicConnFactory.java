/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpClientConnectionFactory;
import org.apache.http.params.HttpParamConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.pool.ConnFactory;
import org.apache.http.util.Args;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
@Immutable
public class BasicConnFactory
implements ConnFactory<HttpHost, HttpClientConnection> {
    private final SocketFactory plainfactory;
    private final SSLSocketFactory sslfactory;
    private final int connectTimeout;
    private final SocketConfig sconfig;
    private final HttpConnectionFactory<? extends HttpClientConnection> connFactory;

    @Deprecated
    public BasicConnFactory(SSLSocketFactory sslfactory, HttpParams params) {
        Args.notNull(params, "HTTP params");
        this.plainfactory = null;
        this.sslfactory = sslfactory;
        this.connectTimeout = params.getIntParameter("http.connection.timeout", 0);
        this.sconfig = HttpParamConfig.getSocketConfig(params);
        this.connFactory = new DefaultBHttpClientConnectionFactory(HttpParamConfig.getConnectionConfig(params));
    }

    @Deprecated
    public BasicConnFactory(HttpParams params) {
        this(null, params);
    }

    public BasicConnFactory(SocketFactory plainfactory, SSLSocketFactory sslfactory, int connectTimeout, SocketConfig sconfig, ConnectionConfig cconfig) {
        this.plainfactory = plainfactory;
        this.sslfactory = sslfactory;
        this.connectTimeout = connectTimeout;
        this.sconfig = sconfig != null ? sconfig : SocketConfig.DEFAULT;
        this.connFactory = new DefaultBHttpClientConnectionFactory(cconfig != null ? cconfig : ConnectionConfig.DEFAULT);
    }

    public BasicConnFactory(int connectTimeout, SocketConfig sconfig, ConnectionConfig cconfig) {
        this(null, null, connectTimeout, sconfig, cconfig);
    }

    public BasicConnFactory(SocketConfig sconfig, ConnectionConfig cconfig) {
        this(null, null, 0, sconfig, cconfig);
    }

    public BasicConnFactory() {
        this(null, null, 0, SocketConfig.DEFAULT, ConnectionConfig.DEFAULT);
    }

    @Deprecated
    protected HttpClientConnection create(Socket socket, HttpParams params) throws IOException {
        int bufsize = params.getIntParameter("http.socket.buffer-size", 8192);
        DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(bufsize);
        conn.bind(socket);
        return conn;
    }

    @Override
    public HttpClientConnection create(HttpHost host) throws IOException {
        String scheme = host.getSchemeName();
        Socket socket = null;
        if ("http".equalsIgnoreCase(scheme)) {
            Socket socket2 = socket = this.plainfactory != null ? this.plainfactory.createSocket() : new Socket();
        }
        if ("https".equalsIgnoreCase(scheme)) {
            socket = (this.sslfactory != null ? this.sslfactory : SSLSocketFactory.getDefault()).createSocket();
        }
        if (socket == null) {
            throw new IOException(scheme + " scheme is not supported");
        }
        String hostname = host.getHostName();
        int port = host.getPort();
        if (port == -1) {
            if (host.getSchemeName().equalsIgnoreCase("http")) {
                port = 80;
            } else if (host.getSchemeName().equalsIgnoreCase("https")) {
                port = 443;
            }
        }
        socket.setSoTimeout(this.sconfig.getSoTimeout());
        socket.connect(new InetSocketAddress(hostname, port), this.connectTimeout);
        socket.setTcpNoDelay(this.sconfig.isTcpNoDelay());
        int linger = this.sconfig.getSoLinger();
        if (linger >= 0) {
            socket.setSoLinger(linger > 0, linger);
        }
        socket.setKeepAlive(this.sconfig.isSoKeepAlive());
        return this.connFactory.createConnection(socket);
    }
}

