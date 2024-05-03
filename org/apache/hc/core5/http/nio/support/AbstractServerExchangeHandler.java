/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.support.AsyncResponseBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Asserts;

public abstract class AbstractServerExchangeHandler<T>
implements AsyncServerExchangeHandler {
    private final AtomicReference<AsyncRequestConsumer<T>> requestConsumerRef = new AtomicReference<Object>(null);
    private final AtomicReference<AsyncResponseProducer> responseProducerRef = new AtomicReference<Object>(null);

    protected abstract AsyncRequestConsumer<T> supplyConsumer(HttpRequest var1, EntityDetails var2, HttpContext var3) throws HttpException;

    protected abstract void handle(T var1, AsyncServerRequestHandler.ResponseTrigger var2, HttpContext var3) throws HttpException, IOException;

    @Override
    public final void handleRequest(HttpRequest request, EntityDetails entityDetails, final ResponseChannel responseChannel, final HttpContext context) throws HttpException, IOException {
        AsyncRequestConsumer<T> requestConsumer = this.supplyConsumer(request, entityDetails, context);
        if (requestConsumer == null) {
            throw new HttpException("Unable to handle request");
        }
        this.requestConsumerRef.set(requestConsumer);
        final AsyncServerRequestHandler.ResponseTrigger responseTrigger = new AsyncServerRequestHandler.ResponseTrigger(){

            @Override
            public void sendInformation(HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
                responseChannel.sendInformation(response, httpContext);
            }

            @Override
            public void submitResponse(AsyncResponseProducer producer, HttpContext httpContext) throws HttpException, IOException {
                if (AbstractServerExchangeHandler.this.responseProducerRef.compareAndSet(null, producer)) {
                    producer.sendResponse(responseChannel, httpContext);
                }
            }

            @Override
            public void pushPromise(HttpRequest promise, HttpContext httpContext, AsyncPushProducer pushProducer) throws HttpException, IOException {
                responseChannel.pushPromise(promise, pushProducer, httpContext);
            }

            public String toString() {
                return "Response trigger: " + responseChannel;
            }
        };
        requestConsumer.consumeRequest(request, entityDetails, context, new FutureCallback<T>(){

            @Override
            public void completed(T result) {
                try {
                    AbstractServerExchangeHandler.this.handle(result, responseTrigger, context);
                } catch (HttpException ex) {
                    try {
                        responseTrigger.submitResponse(AsyncResponseBuilder.create(500).setEntity(ex.getMessage()).build(), context);
                    } catch (IOException | HttpException ex2) {
                        this.failed(ex2);
                    }
                } catch (IOException ex) {
                    this.failed(ex);
                }
            }

            @Override
            public void failed(Exception ex) {
                AbstractServerExchangeHandler.this.failed(ex);
            }

            @Override
            public void cancelled() {
                AbstractServerExchangeHandler.this.releaseResources();
            }
        });
    }

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        AsyncRequestConsumer<T> requestConsumer = this.requestConsumerRef.get();
        Asserts.notNull(requestConsumer, "Data consumer");
        requestConsumer.updateCapacity(capacityChannel);
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        AsyncRequestConsumer<T> requestConsumer = this.requestConsumerRef.get();
        Asserts.notNull(requestConsumer, "Data consumer");
        requestConsumer.consume(src);
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        AsyncRequestConsumer<T> requestConsumer = this.requestConsumerRef.get();
        Asserts.notNull(requestConsumer, "Data consumer");
        requestConsumer.streamEnd(trailers);
    }

    @Override
    public final int available() {
        AsyncResponseProducer dataProducer = this.responseProducerRef.get();
        return dataProducer != null ? dataProducer.available() : 0;
    }

    @Override
    public final void produce(DataStreamChannel channel) throws IOException {
        AsyncResponseProducer dataProducer = this.responseProducerRef.get();
        Asserts.notNull(dataProducer, "Data producer");
        dataProducer.produce(channel);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void failed(Exception cause) {
        try {
            AsyncResponseProducer dataProducer;
            AsyncRequestConsumer<T> requestConsumer = this.requestConsumerRef.get();
            if (requestConsumer != null) {
                requestConsumer.failed(cause);
            }
            if ((dataProducer = this.responseProducerRef.get()) != null) {
                dataProducer.failed(cause);
            }
        } finally {
            this.releaseResources();
        }
    }

    @Override
    public final void releaseResources() {
        AsyncResponseProducer dataProducer;
        AsyncRequestConsumer requestConsumer = this.requestConsumerRef.getAndSet(null);
        if (requestConsumer != null) {
            requestConsumer.releaseResources();
        }
        if ((dataProducer = (AsyncResponseProducer)this.responseProducerRef.getAndSet(null)) != null) {
            dataProducer.releaseResources();
        }
    }
}

