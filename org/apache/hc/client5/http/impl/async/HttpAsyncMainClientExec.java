/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.UserTokenHandler;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.AsyncExecRuntime;
import org.apache.hc.client5.http.impl.async.LoggingAsyncClientExchangeHandler;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
class HttpAsyncMainClientExec
implements AsyncExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HttpAsyncMainClientExec.class);
    private final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final UserTokenHandler userTokenHandler;

    HttpAsyncMainClientExec(ConnectionKeepAliveStrategy keepAliveStrategy, UserTokenHandler userTokenHandler) {
        this.keepAliveStrategy = keepAliveStrategy;
        this.userTokenHandler = userTokenHandler;
    }

    @Override
    public void execute(final HttpRequest request, final AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        String exchangeId = scope.exchangeId;
        final HttpRoute route = scope.route;
        CancellableDependency operation = scope.cancellableDependency;
        final HttpClientContext clientContext = scope.clientContext;
        final AsyncExecRuntime execRuntime = scope.execRuntime;
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: executing {}", (Object)exchangeId, (Object)new RequestLine(request));
        }
        final AtomicInteger messageCountDown = new AtomicInteger(2);
        AsyncClientExchangeHandler internalExchangeHandler = new AsyncClientExchangeHandler(){
            private final AtomicReference<AsyncDataConsumer> entityConsumerRef = new AtomicReference<Object>(null);

            @Override
            public void releaseResources() {
                AsyncDataConsumer entityConsumer = this.entityConsumerRef.getAndSet(null);
                if (entityConsumer != null) {
                    entityConsumer.releaseResources();
                }
            }

            @Override
            public void failed(Exception cause) {
                AsyncDataConsumer entityConsumer = this.entityConsumerRef.getAndSet(null);
                if (entityConsumer != null) {
                    entityConsumer.releaseResources();
                }
                execRuntime.markConnectionNonReusable();
                asyncExecCallback.failed(cause);
            }

            @Override
            public void cancel() {
                this.failed(new InterruptedIOException());
            }

            @Override
            public void produceRequest(RequestChannel channel, HttpContext context) throws HttpException, IOException {
                channel.sendRequest(request, entityProducer, context);
                if (entityProducer == null) {
                    messageCountDown.decrementAndGet();
                }
            }

            @Override
            public int available() {
                return entityProducer.available();
            }

            @Override
            public void produce(final DataStreamChannel channel) throws IOException {
                entityProducer.produce(new DataStreamChannel(){

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
                            asyncExecCallback.completed();
                        }
                    }

                    @Override
                    public void endStream() throws IOException {
                        channel.endStream();
                        if (messageCountDown.decrementAndGet() <= 0) {
                            asyncExecCallback.completed();
                        }
                    }
                });
            }

            @Override
            public void consumeInformation(HttpResponse response, HttpContext context) throws HttpException, IOException {
                asyncExecCallback.handleInformationResponse(response);
            }

            @Override
            public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
                this.entityConsumerRef.set(asyncExecCallback.handleResponse(response, entityDetails));
                if (response.getCode() >= 400) {
                    messageCountDown.decrementAndGet();
                }
                TimeValue keepAliveDuration = HttpAsyncMainClientExec.this.keepAliveStrategy.getKeepAliveDuration(response, clientContext);
                Object userToken = clientContext.getUserToken();
                if (userToken == null) {
                    userToken = HttpAsyncMainClientExec.this.userTokenHandler.getUserToken(route, clientContext);
                    clientContext.setAttribute("http.user-token", userToken);
                }
                execRuntime.markConnectionReusable(userToken, keepAliveDuration);
                if (entityDetails == null) {
                    execRuntime.validateConnection();
                    if (messageCountDown.decrementAndGet() <= 0) {
                        asyncExecCallback.completed();
                    }
                }
            }

            @Override
            public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
                AsyncDataConsumer entityConsumer = this.entityConsumerRef.get();
                if (entityConsumer != null) {
                    entityConsumer.updateCapacity(capacityChannel);
                } else {
                    capacityChannel.update(Integer.MAX_VALUE);
                }
            }

            @Override
            public void consume(ByteBuffer src) throws IOException {
                AsyncDataConsumer entityConsumer = this.entityConsumerRef.get();
                if (entityConsumer != null) {
                    entityConsumer.consume(src);
                }
            }

            @Override
            public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
                AsyncDataConsumer entityConsumer = this.entityConsumerRef.getAndSet(null);
                if (entityConsumer != null) {
                    entityConsumer.streamEnd(trailers);
                } else {
                    execRuntime.validateConnection();
                }
                if (messageCountDown.decrementAndGet() <= 0) {
                    asyncExecCallback.completed();
                }
            }
        };
        if (LOG.isDebugEnabled()) {
            operation.setDependency(execRuntime.execute(exchangeId, new LoggingAsyncClientExchangeHandler(LOG, exchangeId, internalExchangeHandler), clientContext));
        } else {
            operation.setDependency(execRuntime.execute(exchangeId, internalExchangeHandler, clientContext));
        }
    }
}

