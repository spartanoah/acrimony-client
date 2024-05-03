/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Args;

public abstract class AbstractHttpEntity
implements HttpEntity {
    static final int OUTPUT_BUFFER_SIZE = 4096;
    private final String contentType;
    private final String contentEncoding;
    private final boolean chunked;

    protected AbstractHttpEntity(String contentType, String contentEncoding, boolean chunked) {
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
        this.chunked = chunked;
    }

    protected AbstractHttpEntity(ContentType contentType, String contentEncoding, boolean chunked) {
        this.contentType = contentType != null ? contentType.toString() : null;
        this.contentEncoding = contentEncoding;
        this.chunked = chunked;
    }

    protected AbstractHttpEntity(String contentType, String contentEncoding) {
        this(contentType, contentEncoding, false);
    }

    protected AbstractHttpEntity(ContentType contentType, String contentEncoding) {
        this(contentType, contentEncoding, false);
    }

    public static void writeTo(HttpEntity entity, OutputStream outStream) throws IOException {
        Args.notNull(entity, "Entity");
        Args.notNull(outStream, "Output stream");
        try (InputStream inStream = entity.getContent();){
            if (inStream != null) {
                int count;
                byte[] tmp = new byte[4096];
                while ((count = inStream.read(tmp)) != -1) {
                    outStream.write(tmp, 0, count);
                }
            }
        }
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        AbstractHttpEntity.writeTo(this, outStream);
    }

    @Override
    public final String getContentType() {
        return this.contentType;
    }

    @Override
    public final String getContentEncoding() {
        return this.contentEncoding;
    }

    @Override
    public final boolean isChunked() {
        return this.chunked;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return Collections.emptySet();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Entity-Class: ");
        sb.append(this.getClass().getSimpleName());
        sb.append(", Content-Type: ");
        sb.append(this.contentType);
        sb.append(", Content-Encoding: ");
        sb.append(this.contentEncoding);
        sb.append(", chunked: ");
        sb.append(this.chunked);
        sb.append(']');
        return sb.toString();
    }
}

