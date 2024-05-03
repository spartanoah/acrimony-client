/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class StringEntity
extends AbstractHttpEntity {
    private final byte[] content;

    public StringEntity(String string, ContentType contentType, String contentEncoding, boolean chunked) {
        super(contentType, contentEncoding, chunked);
        Charset charset;
        Args.notNull(string, "Source string");
        Charset charset2 = charset = contentType != null ? contentType.getCharset() : null;
        if (charset == null) {
            charset = StandardCharsets.ISO_8859_1;
        }
        this.content = string.getBytes(charset);
    }

    public StringEntity(String string, ContentType contentType, boolean chunked) {
        this(string, contentType, null, chunked);
    }

    public StringEntity(String string, ContentType contentType) {
        this(string, contentType, null, false);
    }

    public StringEntity(String string, Charset charset) {
        this(string, ContentType.TEXT_PLAIN.withCharset(charset));
    }

    public StringEntity(String string, Charset charset, boolean chunked) {
        this(string, ContentType.TEXT_PLAIN.withCharset(charset), chunked);
    }

    public StringEntity(String string) {
        this(string, ContentType.DEFAULT_TEXT);
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        return this.content.length;
    }

    @Override
    public final InputStream getContent() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public final void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        outStream.write(this.content);
        outStream.flush();
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {
    }
}

