/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.StreamChannel;
import org.apache.hc.core5.http.nio.entity.AbstractCharAsyncEntityProducer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public class StringAsyncEntityProducer
extends AbstractCharAsyncEntityProducer {
    private final CharBuffer content;
    private final AtomicReference<Exception> exception;

    public StringAsyncEntityProducer(CharSequence content, int bufferSize, int fragmentSizeHint, ContentType contentType) {
        super(bufferSize, fragmentSizeHint, contentType);
        Args.notNull(content, "Content");
        this.content = CharBuffer.wrap(content);
        this.exception = new AtomicReference<Object>(null);
    }

    public StringAsyncEntityProducer(CharSequence content, int bufferSize, ContentType contentType) {
        this(content, bufferSize, -1, contentType);
    }

    public StringAsyncEntityProducer(CharSequence content, ContentType contentType) {
        this(content, 4096, contentType);
    }

    public StringAsyncEntityProducer(CharSequence content) {
        this(content, ContentType.TEXT_PLAIN);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    protected int availableData() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void produceData(StreamChannel<CharBuffer> channel) throws IOException {
        Asserts.notNull(channel, "Channel");
        channel.write(this.content);
        if (!this.content.hasRemaining()) {
            channel.endStream();
        }
    }

    @Override
    public void failed(Exception cause) {
        if (this.exception.compareAndSet(null, cause)) {
            this.releaseResources();
        }
    }

    public Exception getException() {
        return this.exception.get();
    }

    @Override
    public void releaseResources() {
        this.content.clear();
        super.releaseResources();
    }
}

