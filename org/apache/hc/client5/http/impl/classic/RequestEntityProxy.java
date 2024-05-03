/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

class RequestEntityProxy
implements HttpEntity {
    private final HttpEntity original;
    private boolean consumed = false;

    static void enhance(ClassicHttpRequest request) {
        HttpEntity entity = request.getEntity();
        if (entity != null && !entity.isRepeatable() && !RequestEntityProxy.isEnhanced(entity)) {
            request.setEntity(new RequestEntityProxy(entity));
        }
    }

    static boolean isEnhanced(HttpEntity entity) {
        return entity instanceof RequestEntityProxy;
    }

    RequestEntityProxy(HttpEntity original) {
        this.original = original;
    }

    public HttpEntity getOriginal() {
        return this.original;
    }

    public boolean isConsumed() {
        return this.consumed;
    }

    @Override
    public boolean isRepeatable() {
        if (!this.consumed) {
            return true;
        }
        return this.original.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return this.original.isChunked();
    }

    @Override
    public long getContentLength() {
        return this.original.getContentLength();
    }

    @Override
    public String getContentType() {
        return this.original.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return this.original.getContentEncoding();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return this.original.getContent();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        this.consumed = true;
        this.original.writeTo(outStream);
    }

    @Override
    public boolean isStreaming() {
        return this.original.isStreaming();
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return this.original.getTrailers();
    }

    @Override
    public Set<String> getTrailerNames() {
        return this.original.getTrailerNames();
    }

    @Override
    public void close() throws IOException {
        this.original.close();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RequestEntityProxy{");
        sb.append(this.original);
        sb.append('}');
        return sb.toString();
    }
}

