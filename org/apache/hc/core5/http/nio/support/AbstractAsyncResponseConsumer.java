/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public abstract class AbstractAsyncResponseConsumer<T, E>
implements AsyncResponseConsumer<T> {
    private final Supplier<AsyncEntityConsumer<E>> dataConsumerSupplier;
    private final AtomicReference<AsyncEntityConsumer<E>> dataConsumerRef;

    public AbstractAsyncResponseConsumer(Supplier<AsyncEntityConsumer<E>> dataConsumerSupplier) {
        this.dataConsumerSupplier = Args.notNull(dataConsumerSupplier, "Data consumer supplier");
        this.dataConsumerRef = new AtomicReference<Object>(null);
    }

    public AbstractAsyncResponseConsumer(final AsyncEntityConsumer<E> dataConsumer) {
        this(new Supplier<AsyncEntityConsumer<E>>(){

            @Override
            public AsyncEntityConsumer<E> get() {
                return dataConsumer;
            }
        });
    }

    protected abstract T buildResult(HttpResponse var1, E var2, ContentType var3);

    @Override
    public final void consumeResponse(final HttpResponse response, final EntityDetails entityDetails, HttpContext httpContext, final FutureCallback<T> resultCallback) throws HttpException, IOException {
        if (entityDetails != null) {
            AsyncEntityConsumer<E> dataConsumer = this.dataConsumerSupplier.get();
            if (dataConsumer == null) {
                throw new HttpException("Supplied data consumer is null");
            }
            this.dataConsumerRef.set(dataConsumer);
            dataConsumer.streamStart(entityDetails, new FutureCallback<E>(){

                @Override
                public void completed(E entity) {
                    block3: {
                        try {
                            ContentType contentType = ContentType.parse(entityDetails.getContentType());
                            Object result = AbstractAsyncResponseConsumer.this.buildResult(response, entity, contentType);
                            if (resultCallback != null) {
                                resultCallback.completed(result);
                            }
                        } catch (UnsupportedCharsetException ex) {
                            if (resultCallback == null) break block3;
                            resultCallback.failed(ex);
                        }
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
            T result = this.buildResult(response, null, null);
            if (resultCallback != null) {
                resultCallback.completed(result);
            }
        }
    }

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        AsyncEntityConsumer<E> dataConsumer = this.dataConsumerRef.get();
        dataConsumer.updateCapacity(capacityChannel);
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        AsyncEntityConsumer<E> dataConsumer = this.dataConsumerRef.get();
        dataConsumer.consume(src);
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        AsyncEntityConsumer<E> dataConsumer = this.dataConsumerRef.get();
        dataConsumer.streamEnd(trailers);
    }

    @Override
    public final void failed(Exception cause) {
        this.releaseResources();
    }

    @Override
    public final void releaseResources() {
        AsyncEntityConsumer dataConsumer = this.dataConsumerRef.getAndSet(null);
        if (dataConsumer != null) {
            dataConsumer.releaseResources();
        }
    }
}

