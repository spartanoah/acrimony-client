/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultAddressResolver;
import org.apache.hc.core5.http.impl.io.DefaultBHttpClientConnectionFactory;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.EofSensorInputStream;
import org.apache.hc.core5.http.io.EofSensorWatcher;
import org.apache.hc.core5.http.io.HttpClientConnection;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.HttpResponseInformationCallback;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.http.io.ssl.SSLSessionVerifier;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.pool.ManagedConnPool;
import org.apache.hc.core5.pool.PoolEntry;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public class HttpRequester
implements ConnPoolControl<HttpHost>,
ModalCloseable {
    private final HttpRequestExecutor requestExecutor;
    private final HttpProcessor httpProcessor;
    private final ManagedConnPool<HttpHost, HttpClientConnection> connPool;
    private final SocketConfig socketConfig;
    private final HttpConnectionFactory<? extends HttpClientConnection> connectFactory;
    private final SSLSocketFactory sslSocketFactory;
    private final Callback<SSLParameters> sslSetupHandler;
    private final SSLSessionVerifier sslSessionVerifier;
    private final Resolver<HttpHost, InetSocketAddress> addressResolver;

    @Internal
    public HttpRequester(HttpRequestExecutor requestExecutor, HttpProcessor httpProcessor, ManagedConnPool<HttpHost, HttpClientConnection> connPool, SocketConfig socketConfig, HttpConnectionFactory<? extends HttpClientConnection> connectFactory, SSLSocketFactory sslSocketFactory, Callback<SSLParameters> sslSetupHandler, SSLSessionVerifier sslSessionVerifier, Resolver<HttpHost, InetSocketAddress> addressResolver) {
        this.requestExecutor = Args.notNull(requestExecutor, "Request executor");
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.connPool = Args.notNull(connPool, "Connection pool");
        this.socketConfig = socketConfig != null ? socketConfig : SocketConfig.DEFAULT;
        this.connectFactory = connectFactory != null ? connectFactory : new DefaultBHttpClientConnectionFactory(Http1Config.DEFAULT, CharCodingConfig.DEFAULT);
        this.sslSocketFactory = sslSocketFactory != null ? sslSocketFactory : (SSLSocketFactory)SSLSocketFactory.getDefault();
        this.sslSetupHandler = sslSetupHandler;
        this.sslSessionVerifier = sslSessionVerifier;
        this.addressResolver = addressResolver != null ? addressResolver : DefaultAddressResolver.INSTANCE;
    }

    @Override
    public PoolStats getTotalStats() {
        return this.connPool.getTotalStats();
    }

    @Override
    public PoolStats getStats(HttpHost route) {
        return this.connPool.getStats(route);
    }

    @Override
    public void setMaxTotal(int max) {
        this.connPool.setMaxTotal(max);
    }

    @Override
    public int getMaxTotal() {
        return this.connPool.getMaxTotal();
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        this.connPool.setDefaultMaxPerRoute(max);
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return this.connPool.getDefaultMaxPerRoute();
    }

    @Override
    public void setMaxPerRoute(HttpHost route, int max) {
        this.connPool.setMaxPerRoute(route, max);
    }

    @Override
    public int getMaxPerRoute(HttpHost route) {
        return this.connPool.getMaxPerRoute(route);
    }

    @Override
    public void closeIdle(TimeValue idleTime) {
        this.connPool.closeIdle(idleTime);
    }

    @Override
    public void closeExpired() {
        this.connPool.closeExpired();
    }

    @Override
    public Set<HttpHost> getRoutes() {
        return this.connPool.getRoutes();
    }

    public ClassicHttpResponse execute(HttpClientConnection connection, ClassicHttpRequest request, HttpResponseInformationCallback informationCallback, HttpContext context) throws HttpException, IOException {
        Args.notNull(connection, "HTTP connection");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        if (!connection.isOpen()) {
            throw new ConnectionClosedException();
        }
        this.requestExecutor.preProcess(request, this.httpProcessor, context);
        ClassicHttpResponse response = this.requestExecutor.execute(request, connection, informationCallback, context);
        this.requestExecutor.postProcess(response, this.httpProcessor, context);
        return response;
    }

    public ClassicHttpResponse execute(HttpClientConnection connection, ClassicHttpRequest request, HttpContext context) throws HttpException, IOException {
        return this.execute(connection, request, null, context);
    }

    public boolean keepAlive(HttpClientConnection connection, ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws IOException {
        boolean keepAlive = this.requestExecutor.keepAlive(request, response, connection, context);
        if (!keepAlive) {
            connection.close();
        }
        return keepAlive;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public <T> T execute(HttpClientConnection connection, ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<T> responseHandler) throws HttpException, IOException {
        try (ClassicHttpResponse response = this.execute(connection, request, context);){
            T result = responseHandler.handleResponse(response);
            EntityUtils.consume(response.getEntity());
            boolean keepAlive = this.requestExecutor.keepAlive(request, response, connection, context);
            if (!keepAlive) {
                connection.close();
            }
            T t = result;
            return t;
        } catch (IOException | RuntimeException | HttpException ex) {
            connection.close(CloseMode.IMMEDIATE);
            throw ex;
        }
    }

    private Socket createSocket(HttpHost targetHost) throws IOException {
        int linger;
        final Socket sock = this.socketConfig.getSocksProxyAddress() != null ? new Socket(new Proxy(Proxy.Type.SOCKS, this.socketConfig.getSocksProxyAddress())) : new Socket();
        sock.setSoTimeout(this.socketConfig.getSoTimeout().toMillisecondsIntBound());
        sock.setReuseAddress(this.socketConfig.isSoReuseAddress());
        sock.setTcpNoDelay(this.socketConfig.isTcpNoDelay());
        sock.setKeepAlive(this.socketConfig.isSoKeepAlive());
        if (this.socketConfig.getRcvBufSize() > 0) {
            sock.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
        }
        if (this.socketConfig.getSndBufSize() > 0) {
            sock.setSendBufferSize(this.socketConfig.getSndBufSize());
        }
        if ((linger = this.socketConfig.getSoLinger().toMillisecondsIntBound()) >= 0) {
            sock.setSoLinger(true, linger);
        }
        final InetSocketAddress targetAddress = this.addressResolver.resolve(targetHost);
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

                @Override
                public Object run() throws IOException {
                    sock.connect(targetAddress, HttpRequester.this.socketConfig.getSoTimeout().toMillisecondsIntBound());
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            Asserts.check(e.getCause() instanceof IOException, "method contract violation only checked exceptions are wrapped: " + e.getCause());
            throw (IOException)e.getCause();
        }
        if (URIScheme.HTTPS.same(targetHost.getSchemeName())) {
            SSLSocket sslSocket = (SSLSocket)this.sslSocketFactory.createSocket(sock, targetHost.getHostName(), targetAddress.getPort(), true);
            if (this.sslSetupHandler != null) {
                SSLParameters sslParameters = sslSocket.getSSLParameters();
                this.sslSetupHandler.execute(sslParameters);
                sslSocket.setSSLParameters(sslParameters);
            }
            try {
                sslSocket.startHandshake();
                SSLSession session = sslSocket.getSession();
                if (session == null) {
                    throw new SSLHandshakeException("SSL session not available");
                }
                if (this.sslSessionVerifier != null) {
                    this.sslSessionVerifier.verify(targetHost, session);
                }
            } catch (IOException ex) {
                Closer.closeQuietly(sslSocket);
                throw ex;
            }
            return sslSocket;
        }
        return sock;
    }

    public ClassicHttpResponse execute(HttpHost targetHost, final ClassicHttpRequest request, HttpResponseInformationCallback informationCallback, Timeout connectTimeout, final HttpContext context) throws HttpException, IOException {
        PoolEntry<HttpHost, HttpClientConnection> poolEntry;
        Args.notNull(targetHost, "HTTP host");
        Args.notNull(request, "HTTP request");
        Future leaseFuture = this.connPool.lease(targetHost, null, connectTimeout, null);
        Timeout timeout = Timeout.defaultsToDisabled(connectTimeout);
        try {
            poolEntry = leaseFuture.get(timeout.getDuration(), timeout.getTimeUnit());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw new HttpException("Unexpected failure leasing connection", ex);
        } catch (TimeoutException ex) {
            throw new ConnectionRequestTimeoutException("Connection request timeout");
        }
        final PoolEntryHolder connectionHolder = new PoolEntryHolder(poolEntry);
        try {
            ClassicHttpResponse response;
            HttpEntity entity;
            HttpClientConnection connection = poolEntry.getConnection();
            if (connection == null) {
                Socket socket = this.createSocket(targetHost);
                connection = this.connectFactory.createConnection(socket);
                poolEntry.assignConnection(connection);
            }
            if (request.getAuthority() == null) {
                request.setAuthority(new URIAuthority(targetHost.getHostName(), targetHost.getPort()));
            }
            if ((entity = (response = this.execute(connection, request, informationCallback, context)).getEntity()) != null) {
                response.setEntity(new HttpEntityWrapper(entity){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    private void releaseConnection() throws IOException {
                        try {
                            HttpClientConnection localConn = connectionHolder.getConnection();
                            if (localConn != null && HttpRequester.this.requestExecutor.keepAlive(request, response, localConn, context)) {
                                if (super.isStreaming()) {
                                    Closer.close(super.getContent());
                                }
                                connectionHolder.releaseConnection();
                            }
                        } finally {
                            connectionHolder.discardConnection();
                        }
                    }

                    private void abortConnection() {
                        connectionHolder.discardConnection();
                    }

                    @Override
                    public boolean isStreaming() {
                        return true;
                    }

                    @Override
                    public InputStream getContent() throws IOException {
                        return new EofSensorInputStream(super.getContent(), new EofSensorWatcher(){

                            @Override
                            public boolean eofDetected(InputStream wrapped) throws IOException {
                                this.releaseConnection();
                                return false;
                            }

                            @Override
                            public boolean streamClosed(InputStream wrapped) throws IOException {
                                this.releaseConnection();
                                return false;
                            }

                            @Override
                            public boolean streamAbort(InputStream wrapped) throws IOException {
                                this.abortConnection();
                                return false;
                            }
                        });
                    }

                    @Override
                    public void writeTo(OutputStream outStream) throws IOException {
                        try {
                            if (outStream != null) {
                                super.writeTo(outStream);
                            }
                            this.close();
                        } catch (IOException | RuntimeException ex) {
                            this.abortConnection();
                        }
                    }

                    @Override
                    public void close() throws IOException {
                        this.releaseConnection();
                    }
                });
            } else {
                HttpClientConnection localConn = connectionHolder.getConnection();
                if (!this.requestExecutor.keepAlive(request, response, localConn, context)) {
                    localConn.close();
                }
                connectionHolder.releaseConnection();
            }
            return response;
        } catch (IOException | RuntimeException | HttpException ex) {
            connectionHolder.discardConnection();
            throw ex;
        }
    }

    public ClassicHttpResponse execute(HttpHost targetHost, ClassicHttpRequest request, Timeout connectTimeout, HttpContext context) throws HttpException, IOException {
        return this.execute(targetHost, request, null, connectTimeout, context);
    }

    public <T> T execute(HttpHost targetHost, ClassicHttpRequest request, Timeout connectTimeout, HttpContext context, HttpClientResponseHandler<T> responseHandler) throws HttpException, IOException {
        try (ClassicHttpResponse response = this.execute(targetHost, request, null, connectTimeout, context);){
            T result = responseHandler.handleResponse(response);
            EntityUtils.consume(response.getEntity());
            T t = result;
            return t;
        }
    }

    public ConnPoolControl<HttpHost> getConnPoolControl() {
        return this.connPool;
    }

    @Override
    public void close(CloseMode closeMode) {
        this.connPool.close(closeMode);
    }

    @Override
    public void close() throws IOException {
        this.connPool.close();
    }

    private class PoolEntryHolder {
        private final AtomicReference<PoolEntry<HttpHost, HttpClientConnection>> poolEntryRef;

        PoolEntryHolder(PoolEntry<HttpHost, HttpClientConnection> poolEntry) {
            this.poolEntryRef = new AtomicReference<PoolEntry<HttpHost, HttpClientConnection>>(poolEntry);
        }

        HttpClientConnection getConnection() {
            PoolEntry<HttpHost, HttpClientConnection> poolEntry = this.poolEntryRef.get();
            return poolEntry != null ? poolEntry.getConnection() : null;
        }

        void releaseConnection() {
            PoolEntry poolEntry = this.poolEntryRef.getAndSet(null);
            if (poolEntry != null) {
                HttpClientConnection connection = (HttpClientConnection)poolEntry.getConnection();
                HttpRequester.this.connPool.release(poolEntry, connection != null && connection.isOpen());
            }
        }

        void discardConnection() {
            PoolEntry poolEntry = this.poolEntryRef.getAndSet(null);
            if (poolEntry != null) {
                poolEntry.discardConnection(CloseMode.GRACEFUL);
                HttpRequester.this.connPool.release(poolEntry, false);
            }
        }
    }
}

