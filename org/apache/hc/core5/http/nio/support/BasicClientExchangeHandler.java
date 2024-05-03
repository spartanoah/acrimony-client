/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public final class BasicClientExchangeHandler<T>
implements AsyncClientExchangeHandler {
    private final AsyncRequestProducer requestProducer;
    private final AsyncResponseConsumer<T> responseConsumer;
    private final AtomicBoolean completed;
    private final FutureCallback<T> resultCallback;
    private final AtomicBoolean outputTerminated;

    public BasicClientExchangeHandler(AsyncRequestProducer requestProducer, AsyncResponseConsumer<T> responseConsumer, FutureCallback<T> resultCallback) {
        this.requestProducer = Args.notNull(requestProducer, "Request producer");
        this.responseConsumer = Args.notNull(responseConsumer, "Response consumer");
        this.completed = new AtomicBoolean(false);
        this.resultCallback = resultCallback;
        this.outputTerminated = new AtomicBoolean(false);
    }

    @Override
    public void produceRequest(RequestChannel requestChannel, HttpContext httpContext) throws HttpException, IOException {
        this.requestProducer.sendRequest(requestChannel, httpContext);
    }

    @Override
    public int available() {
        return this.requestProducer.available();
    }

    @Override
    public void produce(DataStreamChannel channel) throws IOException {
        if (this.outputTerminated.get()) {
            channel.endStream();
            return;
        }
        this.requestProducer.produce(channel);
    }

    @Override
    public void consumeInformation(HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
        this.responseConsumer.informationResponse(response, httpContext);
    }

    @Override
    public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
        if (response.getCode() >= 400) {
            this.outputTerminated.set(true);
            this.requestProducer.releaseResources();
        }
        this.responseConsumer.consumeResponse(response, entityDetails, httpContext, new FutureCallback<T>(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void completed(T result) {
                if (BasicClientExchangeHandler.this.completed.compareAndSet(false, true)) {
                    try {
                        if (BasicClientExchangeHandler.this.resultCallback != null) {
                            BasicClientExchangeHandler.this.resultCallback.completed(result);
                        }
                    } finally {
                        BasicClientExchangeHandler.this.internalReleaseResources();
                    }
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void failed(Exception ex) {
                if (BasicClientExchangeHandler.this.completed.compareAndSet(false, true)) {
                    try {
                        if (BasicClientExchangeHandler.this.resultCallback != null) {
                            BasicClientExchangeHandler.this.resultCallback.failed(ex);
                        }
                    } finally {
                        BasicClientExchangeHandler.this.internalReleaseResources();
                    }
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void cancelled() {
                if (BasicClientExchangeHandler.this.completed.compareAndSet(false, true)) {
                    try {
                        if (BasicClientExchangeHandler.this.resultCallback != null) {
                            BasicClientExchangeHandler.this.resultCallback.cancelled();
                        }
                    } finally {
                        BasicClientExchangeHandler.this.internalReleaseResources();
                    }
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void cancel() {
        if (this.completed.compareAndSet(false, true)) {
            try {
                if (this.resultCallback != null) {
                    this.resultCallback.cancelled();
                }
            } finally {
                this.internalReleaseResources();
            }
        }
    }

    @Override
    public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        this.responseConsumer.updateCapacity(capacityChannel);
    }

    @Override
    public void consume(ByteBuffer src) throws IOException {
        this.responseConsumer.consume(src);
    }

    @Override
    public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        this.responseConsumer.streamEnd(trailers);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void failed(Exception cause) {
        try {
            this.requestProducer.failed(cause);
            this.responseConsumer.failed(cause);
        } finally {
            if (this.completed.compareAndSet(false, true)) {
                try {
                    if (this.resultCallback != null) {
                        this.resultCallback.failed(cause);
                    }
                } finally {
                    this.internalReleaseResources();
                }
            }
        }
    }

    private void internalReleaseResources() {
        this.requestProducer.releaseResources();
        this.responseConsumer.releaseResources();
    }

    @Override
    public final void releaseResources() {
    }
}

