/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;

class CombinedEntity
implements HttpEntity {
    private final HttpEntity entity;
    private final InputStream combinedStream;

    CombinedEntity(HttpEntity entity, ByteArrayBuffer buf) throws IOException {
        this.entity = entity;
        this.combinedStream = new SequenceInputStream(new ByteArrayInputStream(buf.array(), 0, buf.length()), entity.getContent());
    }

    @Override
    public long getContentLength() {
        return -1L;
    }

    @Override
    public String getContentType() {
        return this.entity.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return this.entity.getContentEncoding();
    }

    @Override
    public boolean isChunked() {
        return true;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return this.combinedStream;
    }

    @Override
    public Set<String> getTrailerNames() {
        return this.entity.getTrailerNames();
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return this.entity.getTrailers();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        try (InputStream inStream = this.getContent();){
            int l;
            byte[] tmp = new byte[2048];
            while ((l = inStream.read(tmp)) != -1) {
                outStream.write(tmp, 0, l);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        try {
            this.combinedStream.close();
        } finally {
            this.entity.close();
        }
    }
}

