/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.support.classic.ContentOutputStream;
import org.apache.hc.core5.http.nio.support.classic.SharedOutputBuffer;
import org.apache.hc.core5.util.Args;

public abstract class AbstractClassicEntityProducer
implements AsyncEntityProducer {
    private final SharedOutputBuffer buffer;
    private final ContentType contentType;
    private final Executor executor;
    private final AtomicReference<State> state;
    private final AtomicReference<Exception> exception;

    public AbstractClassicEntityProducer(int initialBufferSize, ContentType contentType, Executor executor) {
        this.buffer = new SharedOutputBuffer(initialBufferSize);
        this.contentType = contentType;
        this.executor = Args.notNull(executor, "Executor");
        this.state = new AtomicReference<State>(State.IDLE);
        this.exception = new AtomicReference<Object>(null);
    }

    protected abstract void produceData(ContentType var1, OutputStream var2) throws IOException;

    @Override
    public final boolean isRepeatable() {
        return false;
    }

    @Override
    public final int available() {
        return this.buffer.length();
    }

    @Override
    public final void produce(DataStreamChannel channel) throws IOException {
        if (this.state.compareAndSet(State.IDLE, State.ACTIVE)) {
            this.executor.execute(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        AbstractClassicEntityProducer.this.produceData(AbstractClassicEntityProducer.this.contentType, new ContentOutputStream(AbstractClassicEntityProducer.this.buffer));
                        AbstractClassicEntityProducer.this.buffer.writeCompleted();
                    } catch (Exception ex) {
                        AbstractClassicEntityProducer.this.buffer.abort();
                    } finally {
                        AbstractClassicEntityProducer.this.state.set(State.COMPLETED);
                    }
                }
            });
        }
        this.buffer.flush(channel);
    }

    @Override
    public final long getContentLength() {
        return -1L;
    }

    @Override
    public final String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public final boolean isChunked() {
        return false;
    }

    @Override
    public final Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public final void failed(Exception cause) {
        if (this.exception.compareAndSet(null, cause)) {
            this.releaseResources();
        }
    }

    public final Exception getException() {
        return this.exception.get();
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

