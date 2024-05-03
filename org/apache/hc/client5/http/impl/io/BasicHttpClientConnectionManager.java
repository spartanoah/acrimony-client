/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.io;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.ConnectionShutdownException;
import org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionOperator;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.LangUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE)
public class BasicHttpClientConnectionManager
implements HttpClientConnectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(BasicHttpClientConnectionManager.class);
    private final HttpClientConnectionOperator connectionOperator;
    private final HttpConnectionFactory<ManagedHttpClientConnection> connFactory;
    private ManagedHttpClientConnection conn;
    private HttpRoute route;
    private Object state;
    private long updated;
    private long expiry;
    private boolean leased;
    private SocketConfig socketConfig;
    private final AtomicBoolean closed;

    private static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.create().register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory()).register(URIScheme.HTTPS.id, (PlainConnectionSocketFactory)((Object)SSLConnectionSocketFactory.getSocketFactory())).build();
    }

    public BasicHttpClientConnectionManager(Lookup<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<ManagedHttpClientConnection> connFactory, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        this(new DefaultHttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver), connFactory);
    }

    public BasicHttpClientConnectionManager(HttpClientConnectionOperator httpClientConnectionOperator, HttpConnectionFactory<ManagedHttpClientConnection> connFactory) {
        this.connectionOperator = Args.notNull(httpClientConnectionOperator, "Connection operator");
        this.connFactory = connFactory != null ? connFactory : ManagedHttpClientConnectionFactory.INSTANCE;
        this.expiry = Long.MAX_VALUE;
        this.socketConfig = SocketConfig.DEFAULT;
        this.closed = new AtomicBoolean(false);
    }

    public BasicHttpClientConnectionManager(Lookup<ConnectionSocketFactory> socketFactoryRegistry, HttpConnectionFactory<ManagedHttpClientConnection> connFactory) {
        this(socketFactoryRegistry, connFactory, null, null);
    }

    public BasicHttpClientConnectionManager(Lookup<ConnectionSocketFactory> socketFactoryRegistry) {
        this(socketFactoryRegistry, null, null, null);
    }

    public BasicHttpClientConnectionManager() {
        this(BasicHttpClientConnectionManager.getDefaultRegistry(), null, null, null);
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.closed.compareAndSet(false, true)) {
            this.closeConnection(closeMode);
        }
    }

    HttpRoute getRoute() {
        return this.route;
    }

    Object getState() {
        return this.state;
    }

    public synchronized SocketConfig getSocketConfig() {
        return this.socketConfig;
    }

    public synchronized void setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig != null ? socketConfig : SocketConfig.DEFAULT;
    }

    public LeaseRequest lease(String id, HttpRoute route, Object state) {
        return this.lease(id, route, Timeout.DISABLED, state);
    }

    @Override
    public LeaseRequest lease(String id, final HttpRoute route, Timeout requestTimeout, final Object state) {
        return new LeaseRequest(){

            @Override
            public ConnectionEndpoint get(Timeout timeout) throws InterruptedException, ExecutionException, TimeoutException {
                try {
                    return new InternalConnectionEndpoint(route, BasicHttpClientConnectionManager.this.getConnection(route, state));
                } catch (IOException ex) {
                    throw new ExecutionException(ex.getMessage(), ex);
                }
            }

            @Override
            public boolean cancel() {
                return false;
            }
        };
    }

    private synchronized void closeConnection(CloseMode closeMode) {
        if (this.conn != null) {
            LOG.debug("Closing connection {}", (Object)closeMode);
            this.conn.close(closeMode);
            this.conn = null;
        }
    }

    private void checkExpiry() {
        if (this.conn != null && System.currentTimeMillis() >= this.expiry) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connection expired @ {}", (Object)new Date(this.expiry));
            }
            this.closeConnection(CloseMode.GRACEFUL);
        }
    }

    synchronized ManagedHttpClientConnection getConnection(HttpRoute route, Object state) throws IOException {
        Asserts.check(!this.closed.get(), "Connection manager has been shut down");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get connection for route {}", (Object)route);
        }
        Asserts.check(!this.leased, "Connection is still allocated");
        if (!LangUtils.equals(this.route, route) || !LangUtils.equals(this.state, state)) {
            this.closeConnection(CloseMode.GRACEFUL);
        }
        this.route = route;
        this.state = state;
        this.checkExpiry();
        if (this.conn == null) {
            this.conn = this.connFactory.createConnection(null);
        } else {
            this.conn.activate();
        }
        this.leased = true;
        return this.conn;
    }

    private InternalConnectionEndpoint cast(ConnectionEndpoint endpoint) {
        if (endpoint instanceof InternalConnectionEndpoint) {
            return (InternalConnectionEndpoint)endpoint;
        }
        throw new IllegalStateException("Unexpected endpoint class: " + endpoint.getClass());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void release(ConnectionEndpoint endpoint, Object state, TimeValue keepAlive) {
        Args.notNull(endpoint, "Managed endpoint");
        InternalConnectionEndpoint internalEndpoint = this.cast(endpoint);
        ManagedHttpClientConnection conn = internalEndpoint.detach();
        if (conn != null && LOG.isDebugEnabled()) {
            LOG.debug("Releasing connection {}", (Object)conn);
        }
        if (this.closed.get()) {
            return;
        }
        try {
            if (keepAlive == null) {
                this.conn.close(CloseMode.GRACEFUL);
            }
            this.updated = System.currentTimeMillis();
            if (!this.conn.isOpen() && !this.conn.isConsistent()) {
                this.conn = null;
                this.route = null;
                this.conn = null;
                this.expiry = Long.MAX_VALUE;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Connection is not kept alive");
                }
            } else {
                this.state = state;
                conn.passivate();
                if (TimeValue.isPositive(keepAlive)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Connection can be kept alive for {}", (Object)keepAlive);
                    }
                    this.expiry = this.updated + keepAlive.toMilliseconds();
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Connection can be kept alive indefinitely");
                    }
                    this.expiry = Long.MAX_VALUE;
                }
            }
        } finally {
            this.leased = false;
        }
    }

    @Override
    public void connect(ConnectionEndpoint endpoint, TimeValue connectTimeout, HttpContext context) throws IOException {
        Args.notNull(endpoint, "Endpoint");
        InternalConnectionEndpoint internalEndpoint = this.cast(endpoint);
        if (internalEndpoint.isConnected()) {
            return;
        }
        HttpRoute route = internalEndpoint.getRoute();
        HttpHost host = route.getProxyHost() != null ? route.getProxyHost() : route.getTargetHost();
        this.connectionOperator.connect(internalEndpoint.getConnection(), host, route.getLocalSocketAddress(), connectTimeout, this.socketConfig, context);
    }

    @Override
    public void upgrade(ConnectionEndpoint endpoint, HttpContext context) throws IOException {
        Args.notNull(endpoint, "Endpoint");
        Args.notNull(this.route, "HTTP route");
        InternalConnectionEndpoint internalEndpoint = this.cast(endpoint);
        this.connectionOperator.upgrade(internalEndpoint.getConnection(), internalEndpoint.getRoute().getTargetHost(), context);
    }

    public synchronized void closeExpired() {
        if (this.closed.get()) {
            return;
        }
        if (!this.leased) {
            this.checkExpiry();
        }
    }

    public synchronized void closeIdle(TimeValue idleTime) {
        Args.notNull(idleTime, "Idle time");
        if (this.closed.get()) {
            return;
        }
        if (!this.leased) {
            long deadline;
            long time = idleTime.toMilliseconds();
            if (time < 0L) {
                time = 0L;
            }
            if (this.updated <= (deadline = System.currentTimeMillis() - time)) {
                this.closeConnection(CloseMode.GRACEFUL);
            }
        }
    }

    class InternalConnectionEndpoint
    extends ConnectionEndpoint {
        private final HttpRoute route;
        private final AtomicReference<ManagedHttpClientConnection> connRef;

        public InternalConnectionEndpoint(HttpRoute route, ManagedHttpClientConnection conn) {
            this.route = route;
            this.connRef = new AtomicReference<ManagedHttpClientConnection>(conn);
        }

        HttpRoute getRoute() {
            return this.route;
        }

        ManagedHttpClientConnection getConnection() {
            ManagedHttpClientConnection conn = this.connRef.get();
            if (conn == null) {
                throw new ConnectionShutdownException();
            }
            return conn;
        }

        ManagedHttpClientConnection getValidatedConnection() {
            ManagedHttpClientConnection conn = this.getConnection();
            Asserts.check(conn.isOpen(), "Endpoint is not connected");
            return conn;
        }

        ManagedHttpClientConnection detach() {
            return this.connRef.getAndSet(null);
        }

        @Override
        public boolean isConnected() {
            ManagedHttpClientConnection conn = this.getConnection();
            return conn != null && conn.isOpen();
        }

        @Override
        public void close(CloseMode closeMode) {
            ManagedHttpClientConnection conn = this.detach();
            if (conn != null) {
                conn.close(closeMode);
            }
        }

        @Override
        public void close() throws IOException {
            ManagedHttpClientConnection conn = this.detach();
            if (conn != null) {
                conn.close();
            }
        }

        @Override
        public void setSocketTimeout(Timeout timeout) {
            this.getValidatedConnection().setSocketTimeout(timeout);
        }

        @Override
        public ClassicHttpResponse execute(String id, ClassicHttpRequest request, HttpRequestExecutor requestExecutor, HttpContext context) throws IOException, HttpException {
            Args.notNull(request, "HTTP request");
            Args.notNull(requestExecutor, "Request executor");
            return requestExecutor.execute(request, this.getValidatedConnection(), context);
        }
    }
}

