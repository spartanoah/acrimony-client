/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.util.Args;

public class BasicAsyncEntityProducer
implements AsyncEntityProducer {
    private final ByteBuffer bytebuf;
    private final int length;
    private final ContentType contentType;
    private final boolean chunked;
    private final AtomicReference<Exception> exception;

    public BasicAsyncEntityProducer(byte[] content, ContentType contentType, boolean chunked) {
        Args.notNull(content, "Content");
        this.bytebuf = ByteBuffer.wrap(content);
        this.length = this.bytebuf.remaining();
        this.contentType = contentType;
        this.chunked = chunked;
        this.exception = new AtomicReference<Object>(null);
    }

    public BasicAsyncEntityProducer(byte[] content, ContentType contentType) {
        this(content, contentType, false);
    }

    public BasicAsyncEntityProducer(byte[] content) {
        this(content, ContentType.APPLICATION_OCTET_STREAM);
    }

    public BasicAsyncEntityProducer(CharSequence content, ContentType contentType, boolean chunked) {
        Charset charset;
        Args.notNull(content, "Content");
        this.contentType = contentType;
        Charset charset2 = charset = contentType != null ? contentType.getCharset() : null;
        if (charset == null) {
            charset = StandardCharsets.US_ASCII;
        }
        this.bytebuf = charset.encode(CharBuffer.wrap(content));
        this.length = this.bytebuf.remaining();
        this.chunked = chunked;
        this.exception = new AtomicReference<Object>(null);
    }

    public BasicAsyncEntityProducer(CharSequence content, ContentType contentType) {
        this(content, contentType, false);
    }

    public BasicAsyncEntityProducer(CharSequence content) {
        this(content, ContentType.TEXT_PLAIN);
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public final String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public long getContentLength() {
        return this.length;
    }

    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return this.chunked;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public final void produce(DataStreamChannel channel) throws IOException {
        if (this.bytebuf.hasRemaining()) {
            channel.write(this.bytebuf);
        }
        if (!this.bytebuf.hasRemaining()) {
            channel.endStream();
        }
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
        this.bytebuf.clear();
    }
}

