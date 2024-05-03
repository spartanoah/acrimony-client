/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.impl.io.EmptyInputStream;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.io.Closer;

class IncomingHttpEntity
implements HttpEntity {
    private final InputStream content;
    private final long len;
    private final boolean chunked;
    private final Header contentType;
    private final Header contentEncoding;

    IncomingHttpEntity(InputStream content, long len, boolean chunked, Header contentType, Header contentEncoding) {
        this.content = content;
        this.len = len;
        this.chunked = chunked;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isChunked() {
        return this.chunked;
    }

    @Override
    public long getContentLength() {
        return this.len;
    }

    @Override
    public String getContentType() {
        return this.contentType != null ? this.contentType.getValue() : null;
    }

    @Override
    public String getContentEncoding() {
        return this.contentEncoding != null ? this.contentEncoding.getValue() : null;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return this.content;
    }

    @Override
    public boolean isStreaming() {
        return this.content != null && this.content != EmptyInputStream.INSTANCE;
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        AbstractHttpEntity.writeTo(this, outStream);
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return Collections.emptySet();
    }

    @Override
    public void close() throws IOException {
        Closer.close(this.content);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("Content-Type: ");
        sb.append(this.getContentType());
        sb.append(',');
        sb.append("Content-Encoding: ");
        sb.append(this.getContentEncoding());
        sb.append(',');
        long len = this.getContentLength();
        if (len >= 0L) {
            sb.append("Content-Length: ");
            sb.append(len);
            sb.append(',');
        }
        sb.append("Chunked: ");
        sb.append(this.isChunked());
        sb.append(']');
        return sb.toString();
    }
}

