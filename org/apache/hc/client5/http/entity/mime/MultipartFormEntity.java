/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.apache.hc.client5.http.entity.mime.AbstractMultipartFormat;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentTooLongException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

class MultipartFormEntity
implements HttpEntity {
    private final AbstractMultipartFormat multipart;
    private final ContentType contentType;
    private final long contentLength;

    MultipartFormEntity(AbstractMultipartFormat multipart, ContentType contentType, long contentLength) {
        this.multipart = multipart;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    AbstractMultipartFormat getMultipart() {
        return this.multipart;
    }

    @Override
    public boolean isRepeatable() {
        return this.contentLength != -1L;
    }

    @Override
    public boolean isChunked() {
        return !this.isRepeatable();
    }

    @Override
    public boolean isStreaming() {
        return !this.isRepeatable();
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public InputStream getContent() throws IOException {
        if (this.contentLength < 0L) {
            throw new ContentTooLongException("Content length is unknown");
        }
        if (this.contentLength > 25600L) {
            throw new ContentTooLongException("Content length is too long: " + this.contentLength);
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        this.writeTo(outStream);
        outStream.flush();
        return new ByteArrayInputStream(outStream.toByteArray());
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        this.multipart.writeTo(outStream);
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }
}

