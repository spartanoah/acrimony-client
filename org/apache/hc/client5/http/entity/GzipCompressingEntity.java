/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.util.Args;

public class GzipCompressingEntity
extends HttpEntityWrapper {
    private static final String GZIP_CODEC = "gzip";

    public GzipCompressingEntity(HttpEntity entity) {
        super(entity);
    }

    @Override
    public String getContentEncoding() {
        return GZIP_CODEC;
    }

    @Override
    public long getContentLength() {
        return -1L;
    }

    @Override
    public boolean isChunked() {
        return true;
    }

    @Override
    public InputStream getContent() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        GZIPOutputStream gzip = new GZIPOutputStream(outStream);
        super.writeTo(gzip);
        gzip.close();
    }
}

