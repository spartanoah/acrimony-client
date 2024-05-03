/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.impl.DefaultAddressResolver;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequester;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.http.nio.support.BasicClientExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.pool.ManagedConnPool;
import org.apache.hc.core5.pool.PoolEntry;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public class HttpAsyncRequester
extends AsyncRequester
implements ConnPoolControl<HttpHost> {
    private final ManagedConnPool<HttpHost, IOSession> connPool;

    @Internal
    public HttpAsyncRequester(IOReactorConfig ioReactorConfig, IOEventHandlerFactory eventHandlerFactory, Decorator<IOSession> ioSessionDecorator, Callback<Exception> exceptionCallback, IOSessionListener sessionListener, ManagedConnPool<HttpHost, IOSession> connPool) {
        super(eventHandlerFactory, ioReactorConfig, ioSessionDecorator, exceptionCallback, sessionListener, ShutdownCommand.GRACEFUL_IMMEDIATE_CALLBACK, DefaultAddressResolver.INSTANCE);
        this.connPool = Args.notNull(connPool, "Connection pool");
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

    public Future<AsyncClientEndpoint> connect(HttpHost host, Timeout timeout, Object attachment, FutureCallback<AsyncClientEndpoint> callback) {
        return this.doConnect(host, timeout, attachment, callback);
    }

    protected Future<AsyncClientEndpoint> doConnect(final HttpHost host, final Timeout timeout, final Object attachment, FutureCallback<AsyncClientEndpoint> callback) {
        Args.notNull(host, "Host");
        Args.notNull(timeout, "Timeout");
        final ComplexFuture<AsyncClientEndpoint> resultFuture = new ComplexFuture<AsyncClientEndpoint>(callback);
        Future<PoolEntry<HttpHost, IOSession>> leaseFuture = this.connPool.lease(host, null, timeout, new FutureCallback<PoolEntry<HttpHost, IOSession>>(){

            @Override
            public void completed(final PoolEntry<HttpHost, IOSession> poolEntry) {
                final InternalAsyncClientEndpoint endpoint = new InternalAsyncClientEndpoint(poolEntry);
                IOSession ioSession = poolEntry.getConnection();
                if (ioSession != null && !ioSession.isOpen()) {
                    poolEntry.discardConnection(CloseMode.IMMEDIATE);
                }
                if (poolEntry.hasConnection()) {
                    resultFuture.completed(endpoint);
                } else {
                    Future<IOSession> futute = HttpAsyncRequester.this.requestSession(host, timeout, attachment, new FutureCallback<IOSession>(){

                        @Override
                        public void completed(IOSession session) {
                            session.setSocketTimeout(timeout);
                            poolEntry.assignConnection(session);
                            resultFuture.completed(endpoint);
                        }

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void failed(Exception cause) {
                            try {
                                resultFuture.failed(cause);
                            } finally {
                                endpoint.releaseAndDiscard();
                            }
                        }

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void cancelled() {
                            try {
                                resultFuture.cancel();
                            } finally {
                                endpoint.releaseAndDiscard();
                            }
                        }
                    });
                    resultFuture.setDependency(futute);
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
        resultFuture.setDependency(leaseFuture);
        return resultFuture;
    }

    public Future<AsyncClientEndpoint> connect(HttpHost host, Timeout timeout) {
        return this.connect(host, timeout, null, null);
    }

    public void execute(final AsyncClientExchangeHandler exchangeHandler, final HandlerFactory<AsyncPushConsumer> pushHandlerFactory, final Timeout timeout, final HttpContext executeContext) {
        Args.notNull(exchangeHandler, "Exchange handler");
        Args.notNull(timeout, "Timeout");
        Args.notNull(executeContext, "Context");
        try {
            exchangeHandler.produceRequest(new RequestChannel(){

                @Override
                public void sendRequest(final HttpRequest request, final EntityDetails entityDetails, HttpContext requestContext) throws HttpException, IOException {
                    String scheme = request.getScheme();
                    URIAuthority authority = request.getAuthority();
                    if (authority == null) {
                        throw new ProtocolException("Request authority not specified");
                    }
                    HttpHost target = new HttpHost(scheme, authority);
                    HttpAsyncRequester.this.connect(target, timeout, null, new FutureCallback<AsyncClientEndpoint>(){

                        @Override
                        public void completed(final AsyncClientEndpoint endpoint) {
                            endpoint.execute(new AsyncClientExchangeHandler(){

                                @Override
                                public void releaseResources() {
                                    endpoint.releaseAndDiscard();
                                    exchangeHandler.releaseResources();
                                }

                                @Override
                                public void failed(Exception cause) {
                                    endpoint.releaseAndDiscard();
                                    exchangeHandler.failed(cause);
                                }

                                @Override
                                public void cancel() {
                                    endpoint.releaseAndDiscard();
                                    exchangeHandler.cancel();
                                }

                                @Override
                                public void produceRequest(RequestChannel channel, HttpContext httpContext) throws HttpException, IOException {
                                    channel.sendRequest(request, entityDetails, httpContext);
                                }

                                @Override
                                public int available() {
                                    return exchangeHandler.available();
                                }

                                @Override
                                public void produce(DataStreamChannel channel) throws IOException {
                                    exchangeHandler.produce(channel);
                                }

                                @Override
                                public void consumeInformation(HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
                                    exchangeHandler.consumeInformation(response, httpContext);
                                }

                                @Override
                                public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
                                    if (entityDetails == null) {
                                        endpoint.releaseAndReuse();
                                    }
                                    exchangeHandler.consumeResponse(response, entityDetails, httpContext);
                                }

                                @Override
                                public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
                                    exchangeHandler.updateCapacity(capacityChannel);
                                }

                                @Override
                                public void consume(ByteBuffer src) throws IOException {
                                    exchangeHandler.consume(src);
                                }

                                @Override
                                public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
                                    endpoint.releaseAndReuse();
                                    exchangeHandler.streamEnd(trailers);
                                }
                            }, pushHandlerFactory, executeContext);
                        }

                        @Override
                        public void failed(Exception ex) {
                            exchangeHandler.failed(ex);
                        }

                        @Override
                        public void cancelled() {
                            exchangeHandler.cancel();
                        }
                    });
                }
            }, executeContext);
        } catch (IOException | HttpException ex) {
            exchangeHandler.failed(ex);
        }
    }

    public void execute(AsyncClientExchangeHandler exchangeHandler, Timeout timeout, HttpContext executeContext) {
        this.execute(exchangeHandler, null, timeout, executeContext);
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, Timeout timeout, HttpContext context, FutureCallback<T> callback) {
        Args.notNull(requestProducer, "Request producer");
        Args.notNull(responseConsumer, "Response consumer");
        Args.notNull(timeout, "Timeout");
        final BasicFuture<T> future = new BasicFuture<T>(callback);
        BasicClientExchangeHandler<T> exchangeHandler = new BasicClientExchangeHandler<T>(requestProducer, responseConsumer, new FutureCallback<T>(){

            @Override
            public void completed(T result) {
                future.completed(result);
            }

            @Override
            public void failed(Exception ex) {
                future.failed(ex);
            }

            @Override
            public void cancelled() {
                future.cancel();
            }
        });
        this.execute(exchangeHandler, pushHandlerFactory, timeout, context != null ? context : HttpCoreContext.create());
        return future;
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, Timeout timeout, HttpContext context, FutureCallback<T> callback) {
        return this.execute(requestProducer, responseConsumer, null, timeout, context, callback);
    }

    public final <T> Future<T> execute(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, Timeout timeout, FutureCallback<T> callback) {
        return this.execute(requestProducer, responseConsumer, null, timeout, null, callback);
    }

    private class InternalAsyncClientEndpoint
    extends AsyncClientEndpoint {
        final AtomicReference<PoolEntry<HttpHost, IOSession>> poolEntryRef;

        InternalAsyncClientEndpoint(PoolEntry<HttpHost, IOSession> poolEntry) {
            this.poolEntryRef = new AtomicReference<PoolEntry<HttpHost, IOSession>>(poolEntry);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void execute(AsyncClientExchangeHandler exchangeHandler, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context) {
            PoolEntry<HttpHost, IOSession> poolEntry = this.poolEntryRef.get();
            if (poolEntry == null) {
                throw new IllegalStateException("Endpoint has already been released");
            }
            IOSession ioSession = poolEntry.getConnection();
            if (ioSession == null) {
                throw new IllegalStateException("I/O session is invalid");
            }
            ioSession.enqueue(new RequestExecutionCommand(exchangeHandler, pushHandlerFactory, null, context), Command.Priority.NORMAL);
            if (!ioSession.isOpen()) {
                try {
                    exchangeHandler.failed(new ConnectionClosedException());
                } finally {
                    exchangeHandler.releaseResources();
                }
            }
        }

        @Override
        public boolean isConnected() {
            IOSession ioSession;
            PoolEntry<HttpHost, IOSession> poolEntry = this.poolEntryRef.get();
            return poolEntry != null && (ioSession = poolEntry.getConnection()) != null && ioSession.isOpen();
        }

        @Override
        public void releaseAndReuse() {
            PoolEntry poolEntry = this.poolEntryRef.getAndSet(null);
            if (poolEntry != null) {
                IOSession ioSession = (IOSession)poolEntry.getConnection();
                HttpAsyncRequester.this.connPool.release(poolEntry, ioSession != null && ioSession.isOpen());
            }
        }

        @Override
        public void releaseAndDiscard() {
            PoolEntry poolEntry = this.poolEntryRef.getAndSet(null);
            if (poolEntry != null) {
                poolEntry.discardConnection(CloseMode.GRACEFUL);
                HttpAsyncRequester.this.connPool.release(poolEntry, false);
            }
        }
    }
}

