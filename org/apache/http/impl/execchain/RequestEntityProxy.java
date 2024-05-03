/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.execchain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

class RequestEntityProxy
implements HttpEntity {
    private final HttpEntity original;
    private boolean consumed = false;

    static void enhance(HttpEntityEnclosingRequest request) {
        HttpEntity entity = request.getEntity();
        if (entity != null && !entity.isRepeatable() && !RequestEntityProxy.isEnhanced(entity)) {
            request.setEntity(new RequestEntityProxy(entity));
        }
    }

    static boolean isEnhanced(HttpEntity entity) {
        return entity instanceof RequestEntityProxy;
    }

    static boolean isRepeatable(HttpRequest request) {
        HttpEntity entity;
        if (request instanceof HttpEntityEnclosingRequest && (entity = ((HttpEntityEnclosingRequest)request).getEntity()) != null) {
            RequestEntityProxy proxy;
            if (RequestEntityProxy.isEnhanced(entity) && !(proxy = (RequestEntityProxy)entity).isConsumed()) {
                return true;
            }
            return entity.isRepeatable();
        }
        return true;
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
    public Header getContentType() {
        return this.original.getContentType();
    }

    @Override
    public Header getContentEncoding() {
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
    public void consumeContent() throws IOException {
        this.consumed = true;
        this.original.consumeContent();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RequestEntityProxy{");
        sb.append(this.original);
        sb.append('}');
        return sb.toString();
    }
}

