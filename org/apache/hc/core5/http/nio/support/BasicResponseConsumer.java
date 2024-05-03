/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicResponseConsumer<T>
implements AsyncResponseConsumer<Message<HttpResponse, T>> {
    private final Supplier<AsyncEntityConsumer<T>> dataConsumerSupplier;
    private final AtomicReference<AsyncEntityConsumer<T>> dataConsumerRef;

    public BasicResponseConsumer(Supplier<AsyncEntityConsumer<T>> dataConsumerSupplier) {
        this.dataConsumerSupplier = Args.notNull(dataConsumerSupplier, "Data consumer supplier");
        this.dataConsumerRef = new AtomicReference<Object>(null);
    }

    public BasicResponseConsumer(final AsyncEntityConsumer<T> dataConsumer) {
        this(new Supplier<AsyncEntityConsumer<T>>(){

            @Override
            public AsyncEntityConsumer<T> get() {
                return dataConsumer;
            }
        });
    }

    @Override
    public void consumeResponse(final HttpResponse response, EntityDetails entityDetails, HttpContext httpContext, final FutureCallback<Message<HttpResponse, T>> resultCallback) throws HttpException, IOException {
        Args.notNull(response, "Response");
        if (entityDetails != null) {
            AsyncEntityConsumer<T> dataConsumer = this.dataConsumerSupplier.get();
            if (dataConsumer == null) {
                throw new HttpException("Supplied data consumer is null");
            }
            this.dataConsumerRef.set(dataConsumer);
            dataConsumer.streamStart(entityDetails, new FutureCallback<T>(){

                @Override
                public void completed(T body) {
                    Message result = new Message(response, body);
                    if (resultCallback != null) {
                        resultCallback.completed(result);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    if (resultCallback != null) {
                        resultCallback.failed(ex);
                    }
                }

                @Override
                public void cancelled() {
                    if (resultCallback != null) {
                        resultCallback.cancelled();
                    }
                }
            });
        } else {
            Message<HttpResponse, Object> result = new Message<HttpResponse, Object>(response, null);
            if (resultCallback != null) {
                resultCallback.completed(result);
            }
        }
    }

    @Override
    public void informationResponse(HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
    }

    @Override
    public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        AsyncEntityConsumer<T> dataConsumer = this.dataConsumerRef.get();
        dataConsumer.updateCapacity(capacityChannel);
    }

    @Override
    public void consume(ByteBuffer src) throws IOException {
        AsyncEntityConsumer<T> dataConsumer = this.dataConsumerRef.get();
        dataConsumer.consume(src);
    }

    @Override
    public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        AsyncEntityConsumer<T> dataConsumer = this.dataConsumerRef.get();
        dataConsumer.streamEnd(trailers);
    }

    @Override
    public void failed(Exception cause) {
        AsyncEntityConsumer<T> dataConsumer = this.dataConsumerRef.get();
        if (dataConsumer != null) {
            dataConsumer.failed(cause);
        }
        this.releaseResources();
    }

    @Override
    public void releaseResources() {
        AsyncEntityConsumer dataConsumer = this.dataConsumerRef.getAndSet(null);
        if (dataConsumer != null) {
            dataConsumer.releaseResources();
        }
    }
}

