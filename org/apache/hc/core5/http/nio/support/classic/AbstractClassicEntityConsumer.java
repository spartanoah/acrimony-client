/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.support.classic.ContentInputStream;
import org.apache.hc.core5.http.nio.support.classic.SharedInputBuffer;
import org.apache.hc.core5.util.Args;

public abstract class AbstractClassicEntityConsumer<T>
implements AsyncEntityConsumer<T> {
    private final Executor executor;
    private final SharedInputBuffer buffer;
    private final AtomicReference<State> state;
    private final AtomicReference<T> resultRef;
    private final AtomicReference<Exception> exceptionRef;

    public AbstractClassicEntityConsumer(int initialBufferSize, Executor executor) {
        this.executor = Args.notNull(executor, "Executor");
        this.buffer = new SharedInputBuffer(initialBufferSize);
        this.state = new AtomicReference<State>(State.IDLE);
        this.resultRef = new AtomicReference<Object>(null);
        this.exceptionRef = new AtomicReference<Object>(null);
    }

    protected abstract T consumeData(ContentType var1, InputStream var2) throws IOException;

    @Override
    public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        this.buffer.updateCapacity(capacityChannel);
    }

    @Override
    public final void streamStart(EntityDetails entityDetails, final FutureCallback<T> resultCallback) throws HttpException, IOException {
        ContentType contentType;
        try {
            contentType = ContentType.parse(entityDetails.getContentType());
        } catch (UnsupportedCharsetException ex) {
            throw new UnsupportedEncodingException(ex.getMessage());
        }
        if (this.state.compareAndSet(State.IDLE, State.ACTIVE)) {
            this.executor.execute(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        Object result = AbstractClassicEntityConsumer.this.consumeData(contentType, new ContentInputStream(AbstractClassicEntityConsumer.this.buffer));
                        AbstractClassicEntityConsumer.this.resultRef.set(result);
                        resultCallback.completed(result);
                    } catch (Exception ex) {
                        AbstractClassicEntityConsumer.this.buffer.abort();
                        resultCallback.failed(ex);
                    } finally {
                        AbstractClassicEntityConsumer.this.state.set(State.COMPLETED);
                    }
                }
            });
        }
    }

    @Override
    public final void consume(ByteBuffer src) throws IOException {
        this.buffer.fill(src);
    }

    @Override
    public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        this.buffer.markEndStream();
    }

    @Override
    public final void failed(Exception cause) {
        if (this.exceptionRef.compareAndSet(null, cause)) {
            this.releaseResources();
        }
    }

    public final Exception getException() {
        return this.exceptionRef.get();
    }

    @Override
    public final T getContent() {
        return this.resultRef.get();
    }

    @Override
    public void releaseResources() {
    }

    private static enum State {
        IDLE,
        ACTIVE,
        COMPLETED;

    }
}

