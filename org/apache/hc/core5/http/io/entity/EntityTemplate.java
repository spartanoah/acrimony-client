/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.io.IOCallback;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public final class EntityTemplate
extends AbstractHttpEntity {
    private final long contentLength;
    private final IOCallback<OutputStream> callback;

    public EntityTemplate(long contentLength, ContentType contentType, String contentEncoding, IOCallback<OutputStream> callback) {
        super(contentType, contentEncoding);
        this.contentLength = contentLength;
        this.callback = Args.notNull(callback, "I/O callback");
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public InputStream getContent() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.writeTo(buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        this.callback.execute(outStream);
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void close() throws IOException {
    }
}

