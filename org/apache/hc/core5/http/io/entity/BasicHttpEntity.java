/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.impl.io.EmptyInputStream;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class BasicHttpEntity
extends AbstractHttpEntity {
    private final InputStream content;
    private final long length;

    public BasicHttpEntity(InputStream content, long length, ContentType contentType, String contentEncoding, boolean chunked) {
        super(contentType, contentEncoding, chunked);
        this.content = Args.notNull(content, "Content stream");
        this.length = length;
    }

    public BasicHttpEntity(InputStream content, long length, ContentType contentType, String contentEncoding) {
        this(content, length, contentType, contentEncoding, false);
    }

    public BasicHttpEntity(InputStream content, long length, ContentType contentType) {
        this(content, length, contentType, null);
    }

    public BasicHttpEntity(InputStream content, ContentType contentType, String contentEncoding) {
        this(content, -1L, contentType, contentEncoding);
    }

    public BasicHttpEntity(InputStream content, ContentType contentType) {
        this(content, -1L, contentType, null);
    }

    public BasicHttpEntity(InputStream content, ContentType contentType, boolean chunked) {
        this(content, -1L, contentType, null, chunked);
    }

    @Override
    public final long getContentLength() {
        return this.length;
    }

    @Override
    public final InputStream getContent() throws IllegalStateException {
        return this.content;
    }

    @Override
    public final boolean isRepeatable() {
        return false;
    }

    @Override
    public final boolean isStreaming() {
        return this.content != null && this.content != EmptyInputStream.INSTANCE;
    }

    @Override
    public final void close() throws IOException {
        Closer.close(this.content);
    }
}

