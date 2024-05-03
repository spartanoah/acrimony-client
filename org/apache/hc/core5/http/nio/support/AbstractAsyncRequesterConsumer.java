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
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public abstract class AbstractAsyncRequesterConsumer<T, E>
implements AsyncRequestConsumer<T> {
    private final Supplier<AsyncEntityConsumer<E>> dataConsumerSupplier;
    private final AtomicReference<AsyncEntityConsumer<E>> dataConsumerRef;

    public AbstractAsyncRequesterConsumer(Supplier<AsyncEntityConsumer<E>> dataConsumerSupplier) {
        this.dataConsumerSupplier = Args.notNull(dataConsumerSupplier, "Data consumer supplier");
        this.dataConsumerRef = new AtomicReference<Object>(null);
    }

    public AbstractAsyncRequesterConsumer(final AsyncEntityConsumer<E> dataConsumer) {
        this(new Supplier<AsyncEntityConsumer<E>>(){

            @Override
            public AsyncEntityConsumer<E> get() {
                return dataConsumer;
            }
        });
    }

    protected abstract T buildResult(HttpRequest var1, E var2, ContentType var3);

    @Override
    public final void consumeRequest(final HttpRequest request, final EntityDetails entityDetails, HttpContext httpContext, final FutureCallback<T> resultCallback) throws HttpException, IOException {
        if (entityDetails != null) {
            AsyncEntityConsumer<E> dataConsumer = this.dataConsumerSupplier.get();
            if (dataConsumer == null) {
                throw new HttpException("Supplied data consumer is null");
            }
            this.dataConsumerRef.set(dataConsumer);
            dataConsumer.streamStart(entityDetails, new FutureCallback<E>(){

                @Override
                public void completed(E entity) {
                    try {
                        ContentType contentType = ContentType.parse(entityDetails.getContentType());
                        Object result = AbstractAsyncRequesterConsumer.this.buildResult(request, entity, contentType);
                        resultCallback.completed(result);
                    } catch (UnsupportedCharsetException ex) {
                        resultCallback.failed(ex);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    resultCallback.failed(ex);
                }

                @Override
                public void cancelled() {
                    resultCallback.cancelled();
                }
            });
        } else {
            resultCallback.completed(this.buildResult(request, null, null));
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

