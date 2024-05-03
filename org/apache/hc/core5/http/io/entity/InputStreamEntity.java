/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;

public class InputStreamEntity
extends AbstractHttpEntity {
    private final InputStream content;
    private final long length;

    public InputStreamEntity(InputStream inStream, long length, ContentType contentType, String contentEncoding) {
        super(contentType, contentEncoding);
        this.content = Args.notNull(inStream, "Source input stream");
        this.length = length;
    }

    public InputStreamEntity(InputStream inStream, long length, ContentType contentType) {
        this(inStream, length, contentType, null);
    }

    public InputStreamEntity(InputStream inStream, ContentType contentType) {
        this(inStream, -1L, contentType, null);
    }

    @Override
    public final boolean isRepeatable() {
        return false;
    }

    @Override
    public final long getContentLength() {
        return this.length;
    }

    @Override
    public final InputStream getContent() throws IOException {
        return this.content;
    }

    @Override
    public final void writeTo(OutputStream outStream) throws IOException {
        block16: {
            Args.notNull(outStream, "Output stream");
            try (InputStream inStream = this.content;){
                int readLen;
                byte[] buffer = new byte[4096];
                if (this.length < 0L) {
                    int readLen2;
                    while ((readLen2 = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, readLen2);
                    }
                    break block16;
                }
                for (long remaining = this.length; remaining > 0L; remaining -= (long)readLen) {
                    readLen = inStream.read(buffer, 0, (int)Math.min(4096L, remaining));
                    if (readLen == -1) {
                        break;
                    }
                    outStream.write(buffer, 0, readLen);
                }
            }
        }
    }

    @Override
    public final boolean isStreaming() {
        return true;
    }

    @Override
    public final void close() throws IOException {
        this.content.close();
    }
}

