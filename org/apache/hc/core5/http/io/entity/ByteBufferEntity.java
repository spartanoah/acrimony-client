/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;

public class ByteBufferEntity
extends AbstractHttpEntity {
    private final ByteBuffer buffer;
    private final long length;

    public ByteBufferEntity(ByteBuffer buffer, ContentType contentType, String contentEncoding) {
        super(contentType, contentEncoding);
        Args.notNull(buffer, "Source byte buffer");
        this.buffer = buffer;
        this.length = buffer.remaining();
    }

    public ByteBufferEntity(ByteBuffer buffer, ContentType contentType) {
        this(buffer, contentType, null);
    }

    @Override
    public final boolean isRepeatable() {
        return false;
    }

    @Override
    public final long getContentLength() {
        return this.length;
    }

    @Override
    public final InputStream getContent() throws IOException, UnsupportedOperationException {
        return new InputStream(){

            @Override
            public int read() throws IOException {
                if (!ByteBufferEntity.this.buffer.hasRemaining()) {
                    return -1;
                }
                return ByteBufferEntity.this.buffer.get() & 0xFF;
            }

            @Override
            public int read(byte[] bytes, int off, int len) throws IOException {
                if (!ByteBufferEntity.this.buffer.hasRemaining()) {
                    return -1;
                }
                int chunk = Math.min(len, ByteBufferEntity.this.buffer.remaining());
                ByteBufferEntity.this.buffer.get(bytes, off, chunk);
                return chunk;
            }
        };
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {
    }
}

