/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.nio.pool.H2ConnPool
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.ConnPoolSupport;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.async.AbstractMinimalHttpAsyncClientBase;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.impl.async.LoggingAsyncClientExchangeHandler;
import org.apache.hc.client5.http.impl.async.LoggingExceptionCallback;
import org.apache.hc.client5.http.impl.async.LoggingIOSessionDecorator;
import org.apache.hc.client5.http.impl.classic.RequestFailedException;
import org.apache.hc.client5.http.impl.nio.MultihomeConnectionInitiator;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.ComplexCancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.nio.pool.H2ConnPool;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public final class MinimalH2AsyncClient
extends AbstractMinimalHttpAsyncClientBase {
    private static final Logger LOG = LoggerFactory.getLogger(MinimalH2AsyncClient.class);
    private final H2ConnPool connPool;
    private final ConnectionInitiator connectionInitiator;

    MinimalH2AsyncClient(IOEventHandlerFactory eventHandlerFactory, AsyncPushConsumerRegistry pushConsumerRegistry, IOReactorConfig reactorConfig, ThreadFactory threadFactory, ThreadFactory workerThreadFactory, DnsResolver dnsResolver, TlsStrategy tlsStrategy) {
        super(new DefaultConnectingIOReactor(eventHandlerFactory, reactorConfig, workerThreadFactory, LoggingIOSessionDecorator.INSTANCE, LoggingExceptionCallback.INSTANCE, null, new Callback<IOSession>(){

            @Override
            public void execute(IOSession ioSession) {
                ioSession.enqueue(new ShutdownCommand(CloseMode.GRACEFUL), Command.Priority.IMMEDIATE);
            }
        }), pushConsumerRegistry, threadFactory);
        this.connectionInitiator = new MultihomeConnectionInitiator(this.getConnectionInitiator(), dnsResolver);
        this.connPool = new H2ConnPool(this.connectionInitiator, (Resolver)new Resolver<HttpHost, InetSocketAddress>(){

            @Override
            public InetSocketAddress resolve(HttpHost object) {
                return null;
            }
        }, tlsStrategy);
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
                    Timeout connectTimeout = requestConfig.getConnectTimeout();
                    HttpHost target = new HttpHost(request.getScheme(), request.getAuthority());
                    final Future sessionFuture = MinimalH2AsyncClient.this.connPool.getSession((Object)target, connectTimeout, (FutureCallback)new FutureCallback<IOSession>(){

                        @Override
                        public void completed(IOSession session) {
                            AsyncClientExchangeHandler internalExchangeHandler = new AsyncClientExchangeHandler(){

                                @Override
                                public void releaseResources() {
                                    exchangeHandler.releaseResources();
                                }

                                @Override
                                public void failed(Exception cause) {
                                    exchangeHandler.failed(cause);
                                }

                                @Override
                                public void cancel() {
                                    this.failed(new RequestFailedException("Request aborted"));
                                }

                                @Override
                                public void produceRequest(RequestChannel channel, HttpContext context) throws HttpException, IOException {
                                    channel.sendRequest(request, entityDetails, context);
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
                                public void consumeInformation(HttpResponse response, HttpContext context) throws HttpException, IOException {
                                    exchangeHandler.consumeInformation(response, context);
                                }

                                @Override
                                public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
                                    exchangeHandler.consumeResponse(response, entityDetails, context);
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
                                    exchangeHandler.streamEnd(trailers);
                                }
                            };
                            if (LOG.isDebugEnabled()) {
                                String exchangeId = ExecSupport.getNextExchangeId();
                                LOG.debug("{}: executing message exchange {}", (Object)ConnPoolSupport.getId(session), (Object)exchangeId);
                                session.enqueue(new RequestExecutionCommand(new LoggingAsyncClientExchangeHandler(LOG, exchangeId, internalExchangeHandler), pushHandlerFactory, cancellable, clientContext), Command.Priority.NORMAL);
                            } else {
                                session.enqueue(new RequestExecutionCommand(internalExchangeHandler, pushHandlerFactory, cancellable, clientContext), Command.Priority.NORMAL);
                            }
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
                            return sessionFuture.cancel(true);
                        }
                    });
                }
            }, context);
        } catch (IOException | IllegalStateException | HttpException ex) {
            exchangeHandler.failed(ex);
        }
        return cancellable;
    }
}

