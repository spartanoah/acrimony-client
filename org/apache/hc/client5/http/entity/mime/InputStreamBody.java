/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

public class InputStreamBody
extends AbstractContentBody {
    private final InputStream in;
    private final String filename;
    private final long contentLength;

    public InputStreamBody(InputStream in, String filename) {
        this(in, ContentType.DEFAULT_BINARY, filename);
    }

    public InputStreamBody(InputStream in, ContentType contentType, String filename) {
        this(in, contentType, filename, -1L);
    }

    public InputStreamBody(InputStream in, ContentType contentType, String filename, long contentLength) {
        super(contentType);
        Args.notNull(in, "Input stream");
        this.in = in;
        this.filename = filename;
        this.contentLength = contentLength >= 0L ? contentLength : -1L;
    }

    public InputStreamBody(InputStream in, ContentType contentType) {
        this(in, contentType, null);
    }

    public InputStream getInputStream() {
        return this.in;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void writeTo(OutputStream out) throws IOException {
        Args.notNull(out, "Output stream");
        try {
            int l;
            byte[] tmp = new byte[4096];
            while ((l = this.in.read(tmp)) != -1) {
                out.write(tmp, 0, l);
            }
            out.flush();
        } finally {
            this.in.close();
        }
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }
}

