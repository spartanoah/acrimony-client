/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class HttpEntityWrapper
implements HttpEntity {
    private final HttpEntity wrappedEntity;

    public HttpEntityWrapper(HttpEntity wrappedEntity) {
        this.wrappedEntity = Args.notNull(wrappedEntity, "Wrapped entity");
    }

    @Override
    public boolean isRepeatable() {
        return this.wrappedEntity.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return this.wrappedEntity.isChunked();
    }

    @Override
    public long getContentLength() {
        return this.wrappedEntity.getContentLength();
    }

    @Override
    public String getContentType() {
        return this.wrappedEntity.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return this.wrappedEntity.getContentEncoding();
    }

    @Override
    public InputStream getContent() throws IOException {
        return this.wrappedEntity.getContent();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        this.wrappedEntity.writeTo(outStream);
    }

    @Override
    public boolean isStreaming() {
        return this.wrappedEntity.isStreaming();
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return this.wrappedEntity.getTrailers();
    }

    @Override
    public Set<String> getTrailerNames() {
        return this.wrappedEntity.getTrailerNames();
    }

    @Override
    public void close() throws IOException {
        this.wrappedEntity.close();
    }

    public String toString() {
        return "Wrapper [" + this.wrappedEntity + "]";
    }
}

