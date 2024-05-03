/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hc.client5.http.entity.InputStreamFactory;
import org.apache.hc.client5.http.entity.LazyDecompressingInputStream;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.util.Args;

public class DecompressingEntity
extends HttpEntityWrapper {
    private static final int BUFFER_SIZE = 2048;
    private final InputStreamFactory inputStreamFactory;
    private InputStream content;

    public DecompressingEntity(HttpEntity wrapped, InputStreamFactory inputStreamFactory) {
        super(wrapped);
        this.inputStreamFactory = inputStreamFactory;
    }

    private InputStream getDecompressingStream() throws IOException {
        return new LazyDecompressingInputStream(super.getContent(), this.inputStreamFactory);
    }

    @Override
    public InputStream getContent() throws IOException {
        if (super.isStreaming()) {
            if (this.content == null) {
                this.content = this.getDecompressingStream();
            }
            return this.content;
        }
        return this.getDecompressingStream();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        try (InputStream inStream = this.getContent();){
            int l;
            byte[] buffer = new byte[2048];
            while ((l = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, l);
            }
        }
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public long getContentLength() {
        return -1L;
    }
}

