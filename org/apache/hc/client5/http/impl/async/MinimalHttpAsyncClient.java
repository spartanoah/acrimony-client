/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.HttpVersionPolicy
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.async.AbstractMinimalHttpAsyncClientBase;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.impl.async.LoggingAsyncClientExchangeHandler;
import org.apache.hc.client5.http.impl.async.LoggingExceptionCallback;
import org.apache.hc.client5.http.impl.async.LoggingIOSessionDecorator;
import org.apache.hc.client5.http.impl.classic.RequestFailedException;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncConnectionEndpoint;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.ComplexCancellable;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public final class MinimalHttpAsyncClient
extends AbstractMinimalHttpAsyncClientBase {
    private static final Logger LOG = LoggerFactory.getLogger(MinimalHttpAsyncClient.class);
    private final AsyncClientConnectionManager manager;
    private final SchemePortResolver schemePortResolver;
    private final HttpVersionPolicy versionPolicy;

    MinimalHttpAsyncClient(IOEventHandlerFactory eventHandlerFactory, AsyncPushConsumerRegistry pushConsumerRegistry, HttpVersionPolicy versionPolicy, IOReactorConfig reactorConfig, ThreadFactory threadFactory, ThreadFactory workerThreadFactory, AsyncClientConnectionManager manager, SchemePortResolver schemePortResolver) {
        super(new DefaultConnectingIOReactor(eventHandlerFactory, reactorConfig, workerThreadFactory, LoggingIOSessionDecorator.INSTANCE, LoggingExceptionCallback.INSTANCE, null, new Callback<IOSession>(){

            @Override
            public void execute(IOSession ioSession) {
                ioSession.enqueue(new ShutdownCommand(CloseMode.GRACEFUL), Command.Priority.NORMAL);
            }
        }), pushConsumerRegistry, threadFactory);
        this.manager = manager;
        this.schemePortResolver = schemePortResolver != null ? schemePortResolver : DefaultSchemePortResolver.INSTANCE;
        this.versionPolicy = versionPolicy != null ? versionPolicy : HttpVersionPolicy.NEGOTIATE;
    }

    private Future<AsyncConnectionEndpoint> leaseEndpoint(HttpHost host, Timeout connectionRequestTimeout, final Timeout connectTimeout, final HttpClientContext clientContext, final FutureCallback<AsyncConnectionEndpoint> callback) {
        HttpRoute route = new HttpRoute(RoutingSupport.normalize(host, this.schemePortResolver));
        final ComplexFuture<AsyncConnectionEndpoint> resultFuture = new ComplexFuture<AsyncConnectionEndpoint>(callback);
        String exchangeId = ExecSupport.getNextExchangeId();
        Future<AsyncConnectionEndpoint> leaseFuture = this.manager.lease(exchangeId, route, null, connectionRequestTimeout, new FutureCallback<AsyncConnectionEndpoint>(){

            @Override
            public void completed(AsyncConnectionEndpoint connectionEndpoint) {
                if (connectionEndpoint.isConnected()) {
                    resultFuture.completed(connectionEndpoint);
                } else {
                    Future<AsyncConnectionEndpoint> connectFuture = MinimalHttpAsyncClient.this.manager.connect(connectionEndpoint, MinimalHttpAsyncClient.this.getConnectionInitiator(), connectTimeout, MinimalHttpAsyncClient.this.versionPolicy, clientContext, new FutureCallback<AsyncConnectionEndpoint>(){

                        @Override
                        public void completed(AsyncConnectionEndpoint result) {
                            resultFuture.completed(result);
                        }

                        @Override
                        public void failed(Exception ex) {
                            resultFuture.failed(ex);
                        }

                        @Override
                        public void cancelled() {
                            resultFuture.cancel(true);
                        }
                    });
                    resultFuture.setDependency(connectFuture);
                }
            }

            @Override
            public void failed(Exception ex) {
                callback.failed(ex);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
        resultFuture.setDependency(leaseFuture);
        return resultFuture;
    }

    public final Future<AsyncClientEndpoint> lease(HttpHost host, FutureCallback<AsyncClientEndpoint> callback) {
        return this.lease(host, HttpClientContext.create(), callback);
    }

    public Future<AsyncClientEndpoint> lease(HttpHost host, HttpContext context, FutureCallback<AsyncClientEndpoint> callback) {
        Args.notNull(host, "Host");
        Args.notNull(context, "HTTP context");
        final BasicFuture<AsyncClientEndpoint> future = new BasicFuture<AsyncClientEndpoint>(callback);
        if (!this.isRunning()) {
            future.failed(new CancellationException("Connection lease cancelled"));
            return future;
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        RequestConfig requestConfig = clientContext.getRequestConfig();
        Timeout connectionRequestTimeout = requestConfig.getConnectionRequestTimeout();
        Timeout connectTimeout = requestConfig.getConnectTimeout();
        this.leaseEndpoint(host, connectionRequestTimeout, connectTimeout, clientContext, new FutureCallback<AsyncConnectionEndpoint>(){

            @Override
            public void completed(AsyncConnectionEndpoint result) {
                future.completed(new InternalAsyncClientEndpoint(result));
            }

            @Override
            public void failed(Exception ex) {
                future.failed(ex);
            }

            @Override
            public void cancelled() {
                future.cancel(true);
            }
        });
        return future;
    }

    @Override
    public Cancellable execute(final AsyncClientExchangeHandler exchangeHandler, final HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context) {
        final ComplexCancellable cancellable = new ComplexCancellable();
        try {
            if (!this.isRunning()) {
                throw new CancellationException("Request execution cancelled");
            }
            final HttpClientContext clientContext = context != null ? HttpClientContext.adapt(context) : HttpClientContext.create();
            exchangeHandler.produceRequest(new RequestChannel(){

                @Override
                public void sendRequest(final HttpRequest request, final EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
                    RequestConfig requestConfig = null;
                    if (request instanceof Configurable) {
                        requestConfig = ((Configurable)((Object)request)).getConfig();
                    }
                    if (requestConfig != null) {
                        clientContext.setRequestConfig(requestConfig);
                    } else {
                        requestConfig = clientContext.getRequestConfig();
                    }
                    Timeout connectionRequestTimeout = requestConfig.getConnectionRequestTimeout();
                    Timeout connectTimeout = requestConfig.getConnectTimeout();
                    final Timeout responseTimeout = requestConfig.getResponseTimeout();
                    HttpHost target = new HttpHost(request.getScheme(), request.getAuthority());
                    final Future leaseFuture = MinimalHttpAsyncClient.this.leaseEndpoint(target, connectionRequestTimeout, connectTimeout, clientContext, new FutureCallback<AsyncConnectionEndpoint>(){

                        @Override
                        public void completed(AsyncConnectionEndpoint connectionEndpoint) {
                            final InternalAsyncClientEndpoint endpoint = new InternalAsyncClientEndpoint(connectionEndpoint);
                            final AtomicInteger messageCountDown = new AtomicInteger(2);
                            AsyncClientExchangeHandler internalExchangeHandler = new AsyncClientExchangeHandler(){

                                /*
                                 * WARNING - Removed try catching itself - possible behaviour change.
                                 */
                                @Override
                                public void releaseResources() {
                                    try {
                                        exchangeHandler.releaseResources();
                                    } finally {
                                        endpoint.releaseAndDiscard();
                                    }
                                }

                                /*
                                 * WARNING - Removed try catching itself - possible behaviour change.
                                 */
                                @Override
                                public void failed(Exception cause) {
                                    try {
                                        exchangeHandler.failed(cause);
                                    } finally {
                                        endpoint.releaseAndDiscard();
                                    }
                                }

                                @Override
                                public void cancel() {
                                    this.failed(new RequestFailedException("Request aborted"));
                                }

                                @Override
                                public void produceRequest(RequestChannel channel, HttpContext context) throws HttpException, IOException {
                                    channel.sendRequest(request, entityDetails, context);
                                    if (entityDetails == null) {
                                        messageCountDown.decrementAndGet();
                                    }
                                }

                                @Override
                                public int available() {
                                    return exchangeHandler.available();
                                }

                                @Override
                                public void produce(final DataStreamChannel channel) throws IOException {
                                    exchangeHandler.produce(new DataStreamChannel(){

                                        @Override
                                        public void requestOutput() {
                                            channel.requestOutput();
                                        }

                                        @Override
                                        public int write(ByteBuffer src) throws IOException {
                                            return channel.write(src);
                                        }

                                        @Override
                                        public void endStream(List<? extends Header> trailers) throws IOException {
                                            channel.endStream(trailers);
                                            if (messageCountDown.decrementAndGet() <= 0) {
                                                endpoint.releaseAndReuse();
                                            }
                                        }

                                        @Override
                                        public void endStream() throws IOException {
                                            channel.endStream();
                                            if (messageCountDown.decrementAndGet() <= 0) {
                                                endpoint.releaseAndReuse();
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void consumeInformation(HttpResponse response, HttpContext context) throws HttpException, IOException {
                                    exchangeHandler.consumeInformation(response, context);
                                }

                                @Override
                                public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
                                    exchangeHandler.consumeResponse(response, entityDetails, context);
                                    if (response.getCode() >= 400) {
                                        messageCountDown.decrementAndGet();
                                    }
                                    if (entityDetails == null && messageCountDown.decrementAndGet() <= 0) {
                                        endpoint.releaseAndReuse();
                                    }
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
                                    if (messageCountDown.decrementAndGet() <= 0) {
                                        endpoint.releaseAndReuse();
                                    }
                                    exchangeHandler.streamEnd(trailers);
                                }
                            };
                            if (responseTimeout != null) {
                                endpoint.setSocketTimeout(responseTimeout);
                            }
                            endpoint.execute(internalExchangeHandler, pushHandlerFactory, clientContext);
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
                    cancellable.setDependency(new Cancellable(){

                        @Override
                        public boolean cancel() {
                            return leaseFuture.cancel(true);
                        }
                    });
                }
            }, context);
        } catch (IOException | IllegalStateException | HttpException ex) {
            exchangeHandler.failed(ex);
        }
        return cancellable;
    }

    private class InternalAsyncClientEndpoint
    extends AsyncClientEndpoint {
        private final AsyncConnectionEndpoint connectionEndpoint;
        private final AtomicBoolean released;

        InternalAsyncClientEndpoint(AsyncConnectionEndpoint connectionEndpoint) {
            this.connectionEndpoint = connectionEndpoint;
            this.released = new AtomicBoolean(false);
        }

        boolean isReleased() {
            return this.released.get();
        }

        @Override
        public boolean isConnected() {
            return !this.isReleased() && this.connectionEndpoint.isConnected();
        }

        @Override
        public void execute(AsyncClientExchangeHandler exchangeHandler, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context) {
            Asserts.check(!this.released.get(), "Endpoint has already been released");
            String exchangeId = ExecSupport.getNextExchangeId();
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: executing message exchange {}", (Object)ConnPoolSupport.getId(this.connectionEndpoint), (Object)exchangeId);
                this.connectionEndpoint.execute(exchangeId, new LoggingAsyncClientExchangeHandler(LOG, exchangeId, exchangeHandler), pushHandlerFactory, context);
            } else {
                this.connectionEndpoint.execute(exchangeId, exchangeHandler, context);
            }
        }

        public void setSocketTimeout(Timeout timeout) {
            this.connectionEndpoint.setSocketTimeout(timeout);
        }

        @Override
        public void releaseAndReuse() {
            if (this.released.compareAndSet(false, true)) {
                MinimalHttpAsyncClient.this.manager.release(this.connectionEndpoint, null, TimeValue.NEG_ONE_MILLISECOND);
            }
        }

        @Override
        public void releaseAndDiscard() {
            if (this.released.compareAndSet(false, true)) {
                Closer.closeQuietly(this.connectionEndpoint);
                MinimalHttpAsyncClient.this.manager.release(this.connectionEndpoint, null, TimeValue.ZERO_MILLISECONDS);
            }
        }
    }
}

