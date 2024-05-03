/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.nio.AsyncPingHandler
 *  org.apache.hc.core5.http2.nio.command.PingCommand
 *  org.apache.hc.core5.http2.nio.support.BasicPingHandler
 */
package org.apache.hc.client5.http.impl.nio;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.ConnectionShutdownException;
import org.apache.hc.client5.http.impl.nio.DefaultAsyncClientConnectionOperator;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncClientConnectionOperator;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.nio.ManagedAsyncClientConnection;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.nio.AsyncPingHandler;
import org.apache.hc.core5.http2.nio.command.PingCommand;
import org.apache.hc.core5.http2.nio.support.BasicPingHandler;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.pool.LaxConnPool;
import org.apache.hc.core5.pool.ManagedConnPool;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolEntry;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.pool.StrictConnPool;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.Identifiable;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public class PoolingAsyncClientConnectionManager
implements AsyncClientConnectionManager,
ConnPoolControl<HttpRoute> {
    private static final Logger LOG = LoggerFactory.getLogger(PoolingAsyncClientConnectionManager.class);
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 25;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;
    private final ManagedConnPool<HttpRoute, ManagedAsyncClientConnection> pool;
    private final AsyncClientConnectionOperator connectionOperator;
    private final AtomicBoolean closed;
    private volatile TimeValue validateAfterInactivity;
    private static final AtomicLong COUNT = new AtomicLong(0L);

    public PoolingAsyncClientConnectionManager() {
        this(RegistryBuilder.create().register("https", DefaultClientTlsStrategy.getDefault()).build());
    }

    public PoolingAsyncClientConnectionManager(Lookup<TlsStrategy> tlsStrategyLookup) {
        this(tlsStrategyLookup, PoolConcurrencyPolicy.STRICT, TimeValue.NEG_ONE_MILLISECOND);
    }

    public PoolingAsyncClientConnectionManager(Lookup<TlsStrategy> tlsStrategyLookup, PoolConcurrencyPolicy poolConcurrencyPolicy, TimeValue timeToLive) {
        this(tlsStrategyLookup, poolConcurrencyPolicy, PoolReusePolicy.LIFO, timeToLive);
    }

    public PoolingAsyncClientConnectionManager(Lookup<TlsStrategy> tlsStrategyLookup, PoolConcurrencyPolicy poolConcurrencyPolicy, PoolReusePolicy poolReusePolicy, TimeValue timeToLive) {
        this(tlsStrategyLookup, poolConcurrencyPolicy, poolReusePolicy, timeToLive, null, null);
    }

    public PoolingAsyncClientConnectionManager(Lookup<TlsStrategy> tlsStrategyLookup, PoolConcurrencyPolicy poolConcurrencyPolicy, PoolReusePolicy poolReusePolicy, TimeValue timeToLive, SchemePortResolver schemePortResolver, DnsResolver dnsResolver) {
        this(new DefaultAsyncClientConnectionOperator(tlsStrategyLookup, schemePortResolver, dnsResolver), poolConcurrencyPolicy, poolReusePolicy, timeToLive);
    }

    @Internal
    protected PoolingAsyncClientConnectionManager(AsyncClientConnectionOperator connectionOperator, PoolConcurrencyPolicy poolConcurrencyPolicy, PoolReusePolicy poolReusePolicy, TimeValue timeToLive) {
        this.connectionOperator = Args.notNull(connectionOperator, "Connection operator");
        switch (poolConcurrencyPolicy != null ? poolConcurrencyPolicy : PoolConcurrencyPolicy.STRICT) {
            case STRICT: {
                this.pool = new StrictConnPool<HttpRoute, ManagedAsyncClientConnection>(5, 25, timeToLive, poolReusePolicy, null);
                break;
            }
            case LAX: {
                this.pool = new LaxConnPool<HttpRoute, ManagedAsyncClientConnection>(5, timeToLive, poolReusePolicy, null);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unexpected PoolConcurrencyPolicy value: " + (Object)((Object)poolConcurrencyPolicy));
            }
        }
        this.closed = new AtomicBoolean(false);
    }

    @Internal
    protected PoolingAsyncClientConnectionManager(ManagedConnPool<HttpRoute, ManagedAsyncClientConnection> pool, AsyncClientConnectionOperator connectionOperator) {
        this.connectionOperator = Args.notNull(connectionOperator, "Connection operator");
        this.pool = Args.notNull(pool, "Connection pool");
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.closed.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Shutdown connection pool {}", (Object)closeMode);
            }
            this.pool.close(closeMode);
            LOG.debug("Connection pool shut down");
        }
    }

    private InternalConnectionEndpoint cast(AsyncConnectionEndpoint endpoint) {
        if (endpoint instanceof InternalConnectionEndpoint) {
            return (InternalConnectionEndpoint)endpoint;
        }
        throw new IllegalStateException("Unexpected endpoint class: " + endpoint.getClass());
    }

    @Override
    public Future<AsyncConnectionEndpoint> lease(final String id, final HttpRoute route, final Object state, Timeout requestTimeout, FutureCallback<AsyncConnectionEndpoint> callback) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: endpoint lease request ({}) {}", id, requestTimeout, ConnPoolSupport.formatStats(route, state, this.pool));
        }
        final ComplexFuture<AsyncConnectionEndpoint> resultFuture = new ComplexFuture<AsyncConnectionEndpoint>(callback);
        Future<PoolEntry<HttpRoute, ManagedAsyncClientConnection>> leaseFuture = this.pool.lease(route, state, requestTimeout, new FutureCallback<PoolEntry<HttpRoute, ManagedAsyncClientConnection>>(){

            void leaseCompleted(PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry) {
                ManagedAsyncClientConnection connection = poolEntry.getConnection();
                if (connection != null) {
                    connection.activate();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: endpoint leased {}", (Object)id, (Object)ConnPoolSupport.formatStats(route, state, PoolingAsyncClientConnectionManager.this.pool));
                }
                InternalConnectionEndpoint endpoint = new InternalConnectionEndpoint(poolEntry);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: acquired {}", (Object)id, (Object)ConnPoolSupport.getId(endpoint));
                }
                resultFuture.completed(endpoint);
            }

            @Override
            public void completed(final PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry) {
                final ManagedAsyncClientConnection connection = poolEntry.getConnection();
                TimeValue timeValue = PoolingAsyncClientConnectionManager.this.validateAfterInactivity;
                if (TimeValue.isNonNegative(timeValue) && connection != null && poolEntry.getUpdated() + timeValue.toMilliseconds() <= System.currentTimeMillis()) {
                    ProtocolVersion protocolVersion = connection.getProtocolVersion();
                    if (protocolVersion != null && protocolVersion.greaterEquals(HttpVersion.HTTP_2_0)) {
                        connection.submitCommand((Command)new PingCommand((AsyncPingHandler)new BasicPingHandler((Callback)new Callback<Boolean>(){

                            @Override
                            public void execute(Boolean result) {
                                if (result == null || !result.booleanValue()) {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("{}: connection {} is stale", (Object)id, (Object)ConnPoolSupport.getId(connection));
                                    }
                                    poolEntry.discardConnection(CloseMode.IMMEDIATE);
                                }
                                this.leaseCompleted(poolEntry);
                            }
                        })), Command.Priority.IMMEDIATE);
                    } else {
                        if (!connection.isOpen()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("{}: connection {} is closed", (Object)id, (Object)ConnPoolSupport.getId(connection));
                            }
                            poolEntry.discardConnection(CloseMode.IMMEDIATE);
                        }
                        this.leaseCompleted(poolEntry);
                    }
                } else {
                    this.leaseCompleted(poolEntry);
                }
            }

            @Override
            public void failed(Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: endpoint lease failed", (Object)id);
                }
                resultFuture.failed(ex);
            }

            @Override
            public void cancelled() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: endpoint lease cancelled", (Object)id);
                }
                resultFuture.cancel();
            }
        });
        resultFuture.setDependency(leaseFuture);
        return resultFuture;
    }

    @Override
    public void release(AsyncConnectionEndpoint endpoint, Object state, TimeValue keepAlive) {
        ManagedAsyncClientConnection connection;
        Args.notNull(endpoint, "Managed endpoint");
        Args.notNull(keepAlive, "Keep-alive time");
        PoolEntry<HttpRoute, ManagedAsyncClientConnection> entry = this.cast(endpoint).detach();
        if (entry == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: releasing endpoint", (Object)ConnPoolSupport.getId(endpoint));
        }
        boolean reusable = (connection = entry.getConnection()) != null && connection.isOpen();
        try {
            if (reusable) {
                entry.updateState(state);
                entry.updateExpiry(keepAlive);
                connection.passivate();
                if (LOG.isDebugEnabled()) {
                    String s = TimeValue.isPositive(keepAlive) ? "for " + keepAlive : "indefinitely";
                    LOG.debug("{}: connection {} can be kept alive {}", ConnPoolSupport.getId(endpoint), ConnPoolSupport.getId(connection), s);
                }
            }
        } catch (RuntimeException ex) {
            reusable = false;
            throw ex;
        } finally {
            this.pool.release(entry, reusable);
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: connection released {}", (Object)ConnPoolSupport.getId(endpoint), (Object)ConnPoolSupport.formatStats(entry.getRoute(), entry.getState(), this.pool));
            }
        }
    }

    @Override
    public Future<AsyncConnectionEndpoint> connect(final AsyncConnectionEndpoint endpoint, ConnectionInitiator connectionInitiator, Timeout connectTimeout, Object attachment, HttpContext context, FutureCallback<AsyncConnectionEndpoint> callback) {
        Args.notNull(endpoint, "Endpoint");
        Args.notNull(connectionInitiator, "Connection initiator");
        Args.notNull(connectTimeout, "Timeout");
        final InternalConnectionEndpoint internalEndpoint = this.cast(endpoint);
        final ComplexFuture<AsyncConnectionEndpoint> resultFuture = new ComplexFuture<AsyncConnectionEndpoint>(callback);
        if (internalEndpoint.isConnected()) {
            resultFuture.completed(endpoint);
            return resultFuture;
        }
        final PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry = internalEndpoint.getPoolEntry();
        HttpRoute route = poolEntry.getRoute();
        HttpHost host = route.getProxyHost() != null ? route.getProxyHost() : route.getTargetHost();
        InetSocketAddress localAddress = route.getLocalSocketAddress();
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: connecting endpoint to {} ({})", ConnPoolSupport.getId(endpoint), host, connectTimeout);
        }
        Future<ManagedAsyncClientConnection> connectFuture = this.connectionOperator.connect(connectionInitiator, host, localAddress, connectTimeout, attachment, new FutureCallback<ManagedAsyncClientConnection>(){

            @Override
            public void completed(ManagedAsyncClientConnection connection) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{}: connected {}", (Object)ConnPoolSupport.getId(endpoint), (Object)ConnPoolSupport.getId(connection));
                    }
                    poolEntry.assignConnection(connection);
                    resultFuture.completed(internalEndpoint);
                } catch (RuntimeException ex) {
                    resultFuture.failed(ex);
                }
            }

            @Override
            public void failed(Exception ex) {
                resultFuture.failed(ex);
            }

            @Override
            public void cancelled() {
                resultFuture.cancel();
            }
        });
        resultFuture.setDependency(connectFuture);
        return resultFuture;
    }

    @Override
    public void upgrade(AsyncConnectionEndpoint endpoint, Object attachment, HttpContext context) {
        Args.notNull(endpoint, "Managed endpoint");
        InternalConnectionEndpoint internalEndpoint = this.cast(endpoint);
        PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry = internalEndpoint.getValidatedPoolEntry();
        HttpRoute route = poolEntry.getRoute();
        ManagedAsyncClientConnection connection = poolEntry.getConnection();
        this.connectionOperator.upgrade(poolEntry.getConnection(), route.getTargetHost(), attachment);
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: upgraded {}", (Object)ConnPoolSupport.getId(internalEndpoint), (Object)ConnPoolSupport.getId(connection));
        }
    }

    @Override
    public Set<HttpRoute> getRoutes() {
        return this.pool.getRoutes();
    }

    @Override
    public void setMaxTotal(int max) {
        this.pool.setMaxTotal(max);
    }

    @Override
    public int getMaxTotal() {
        return this.pool.getMaxTotal();
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        this.pool.setDefaultMaxPerRoute(max);
    }

    @Override
    public int getDefaultMaxPerRoute() {
        return this.pool.getDefaultMaxPerRoute();
    }

    @Override
    public void setMaxPerRoute(HttpRoute route, int max) {
        this.pool.setMaxPerRoute(route, max);
    }

    @Override
    public int getMaxPerRoute(HttpRoute route) {
        return this.pool.getMaxPerRoute(route);
    }

    @Override
    public void closeIdle(TimeValue idletime) {
        this.pool.closeIdle(idletime);
    }

    @Override
    public void closeExpired() {
        this.pool.closeExpired();
    }

    @Override
    public PoolStats getTotalStats() {
        return this.pool.getTotalStats();
    }

    @Override
    public PoolStats getStats(HttpRoute route) {
        return this.pool.getStats(route);
    }

    public TimeValue getValidateAfterInactivity() {
        return this.validateAfterInactivity;
    }

    public void setValidateAfterInactivity(TimeValue validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
    }

    class InternalConnectionEndpoint
    extends AsyncConnectionEndpoint
    implements Identifiable {
        private final AtomicReference<PoolEntry<HttpRoute, ManagedAsyncClientConnection>> poolEntryRef;
        private final String id;

        InternalConnectionEndpoint(PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry) {
            this.poolEntryRef = new AtomicReference<PoolEntry<HttpRoute, ManagedAsyncClientConnection>>(poolEntry);
            this.id = String.format("ep-%08X", COUNT.getAndIncrement());
        }

        @Override
        public String getId() {
            return this.id;
        }

        PoolEntry<HttpRoute, ManagedAsyncClientConnection> getPoolEntry() {
            PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry = this.poolEntryRef.get();
            if (poolEntry == null) {
                throw new ConnectionShutdownException();
            }
            return poolEntry;
        }

        PoolEntry<HttpRoute, ManagedAsyncClientConnection> getValidatedPoolEntry() {
            PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry = this.getPoolEntry();
            ManagedAsyncClientConnection connection = poolEntry.getConnection();
            Asserts.check(connection != null && connection.isOpen(), "Endpoint is not connected");
            return poolEntry;
        }

        PoolEntry<HttpRoute, ManagedAsyncClientConnection> detach() {
            return this.poolEntryRef.getAndSet(null);
        }

        @Override
        public void close(CloseMode closeMode) {
            PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry = this.poolEntryRef.get();
            if (poolEntry != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: close {}", (Object)this.id, (Object)closeMode);
                }
                poolEntry.discardConnection(closeMode);
            }
        }

        @Override
        public boolean isConnected() {
            PoolEntry<HttpRoute, ManagedAsyncClientConnection> poolEntry = this.poolEntryRef.get();
            if (poolEntry == null) {
                return false;
            }
            ManagedAsyncClientConnection connection = poolEntry.getConnection();
            if (connection == null) {
                return false;
            }
            if (!connection.isOpen()) {
                poolEntry.discardConnection(CloseMode.IMMEDIATE);
                return false;
            }
            return true;
        }

        @Override
        public void setSocketTimeout(Timeout timeout) {
            this.getValidatedPoolEntry().getConnection().setSocketTimeout(timeout);
        }

        @Override
        public void execute(String exchangeId, AsyncClientExchangeHandler exchangeHandler, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context) {
            ManagedAsyncClientConnection connection = this.getValidatedPoolEntry().getConnection();
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: executing exchange {} over {}", this.id, exchangeId, ConnPoolSupport.getId(connection));
            }
            connection.submitCommand(new RequestExecutionCommand(exchangeHandler, pushHandlerFactory, context), Command.Priority.NORMAL);
        }
    }
}

