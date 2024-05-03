/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

public class StringBody
extends AbstractContentBody {
    private final byte[] content;

    public StringBody(String text, ContentType contentType) {
        super(contentType);
        Args.notNull(text, "Text");
        Charset charset = contentType.getCharset();
        this.content = text.getBytes(charset != null ? charset : StandardCharsets.US_ASCII);
    }

    public Reader getReader() {
        Charset charset = this.getContentType().getCharset();
        return new InputStreamReader((InputStream)new ByteArrayInputStream(this.content), charset != null ? charset : StandardCharsets.US_ASCII);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        int l;
        Args.notNull(out, "Output stream");
        ByteArrayInputStream in = new ByteArrayInputStream(this.content);
        byte[] tmp = new byte[4096];
        while ((l = in.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }
        out.flush();
    }

    @Override
    public long getContentLength() {
        return this.content.length;
    }

    @Override
    public String getFilename() {
        return null;
    }
}

