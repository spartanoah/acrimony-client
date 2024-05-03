/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.support.AsyncServerFilterChainElement;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public final class AsyncServerFilterChainExchangeHandlerFactory
implements HandlerFactory<AsyncServerExchangeHandler> {
    private final AsyncServerFilterChainElement filterChain;
    private final Callback<Exception> exceptionCallback;

    public AsyncServerFilterChainExchangeHandlerFactory(AsyncServerFilterChainElement filterChain, Callback<Exception> exceptionCallback) {
        this.filterChain = Args.notNull(filterChain, "Filter chain");
        this.exceptionCallback = exceptionCallback;
    }

    public AsyncServerFilterChainExchangeHandlerFactory(AsyncServerFilterChainElement filterChain) {
        this(filterChain, null);
    }

    @Override
    public AsyncServerExchangeHandler create(HttpRequest request, HttpContext context) throws HttpException {
        return new AsyncServerExchangeHandler(){
            private final AtomicReference<AsyncDataConsumer> dataConsumerRef = new AtomicReference();
            private final AtomicReference<AsyncResponseProducer> responseProducerRef = new AtomicReference();

            @Override
            public void handleRequest(HttpRequest request, EntityDetails entityDetails, final ResponseChannel responseChannel, final HttpContext context) throws HttpException, IOException {
                this.dataConsumerRef.set(AsyncServerFilterChainExchangeHandlerFactory.this.filterChain.handle(request, entityDetails, context, new AsyncFilterChain.ResponseTrigger(){

                    @Override
                    public void sendInformation(HttpResponse response) throws HttpException, IOException {
                        responseChannel.sendInformation(response, context);
                    }

                    @Override
                    public void submitResponse(HttpResponse response, AsyncEntityProducer entityProducer) throws HttpException, IOException {
                        BasicResponseProducer responseProducer = new BasicResponseProducer(response, entityProducer);
                        responseProducerRef.set(responseProducer);
                        responseProducer.sendResponse(responseChannel, context);
                    }

                    @Override
                    public void pushPromise(HttpRequest promise, AsyncPushProducer responseProducer) throws HttpException, IOException {
                        responseChannel.pushPromise(promise, responseProducer, context);
                    }
                }));
            }

            @Override
            public void failed(Exception cause) {
                AsyncResponseProducer handler;
                if (AsyncServerFilterChainExchangeHandlerFactory.this.exceptionCallback != null) {
                    AsyncServerFilterChainExchangeHandlerFactory.this.exceptionCallback.execute(cause);
                }
                if ((handler = this.responseProducerRef.get()) != null) {
                    handler.failed(cause);
                }
            }

            @Override
            public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
                AsyncDataConsumer dataConsumer = this.dataConsumerRef.get();
                if (dataConsumer != null) {
                    dataConsumer.updateCapacity(capacityChannel);
                } else {
                    capacityChannel.update(Integer.MAX_VALUE);
                }
            }

            @Override
            public void consume(ByteBuffer src) throws IOException {
                AsyncDataConsumer dataConsumer = this.dataConsumerRef.get();
                if (dataConsumer != null) {
                    dataConsumer.consume(src);
                }
            }

            @Override
            public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
                AsyncDataConsumer dataConsumer = this.dataConsumerRef.get();
                if (dataConsumer != null) {
                    dataConsumer.streamEnd(trailers);
                }
            }

            @Override
            public int available() {
                AsyncResponseProducer responseProducer = this.responseProducerRef.get();
                Asserts.notNull(responseProducer, "Response producer");
                return responseProducer.available();
            }

            @Override
            public void produce(DataStreamChannel channel) throws IOException {
                AsyncResponseProducer responseProducer = this.responseProducerRef.get();
                Asserts.notNull(responseProducer, "Response producer");
                responseProducer.produce(channel);
            }

            @Override
            public void releaseResources() {
                AsyncResponseProducer responseProducer;
                AsyncDataConsumer dataConsumer = this.dataConsumerRef.getAndSet(null);
                if (dataConsumer != null) {
                    dataConsumer.releaseResources();
                }
                if ((responseProducer = (AsyncResponseProducer)this.responseProducerRef.getAndSet(null)) != null) {
                    responseProducer.releaseResources();
                }
            }
        };
    }
}

