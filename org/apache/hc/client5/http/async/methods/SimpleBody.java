/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

public final class SimpleBody {
    private final byte[] bodyAsBytes;
    private final String bodyAsText;
    private final ContentType contentType;

    SimpleBody(byte[] bodyAsBytes, String bodyAsText, ContentType contentType) {
        this.bodyAsBytes = bodyAsBytes;
        this.bodyAsText = bodyAsText;
        this.contentType = contentType;
    }

    static SimpleBody create(String body, ContentType contentType) {
        Args.notNull(body, "Body");
        if (body.length() > 2048) {
            return new SimpleBody(null, body, contentType);
        }
        Charset charset = (contentType != null ? contentType : ContentType.DEFAULT_TEXT).getCharset();
        byte[] bytes = body.getBytes(charset != null ? charset : StandardCharsets.US_ASCII);
        return new SimpleBody(bytes, null, contentType);
    }

    static SimpleBody create(byte[] body, ContentType contentType) {
        Args.notNull(body, "Body");
        return new SimpleBody(body, null, contentType);
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public byte[] getBodyBytes() {
        if (this.bodyAsBytes != null) {
            return this.bodyAsBytes;
        }
        if (this.bodyAsText != null) {
            Charset charset = (this.contentType != null ? this.contentType : ContentType.DEFAULT_TEXT).getCharset();
            return this.bodyAsText.getBytes(charset != null ? charset : StandardCharsets.US_ASCII);
        }
        return null;
    }

    public String getBodyText() {
        if (this.bodyAsBytes != null) {
            Charset charset = (this.contentType != null ? this.contentType : ContentType.DEFAULT_TEXT).getCharset();
            return new String(this.bodyAsBytes, charset != null ? charset : StandardCharsets.US_ASCII);
        }
        if (this.bodyAsText != null) {
            return this.bodyAsText;
        }
        return null;
    }

    public boolean isText() {
        return this.bodyAsText != null;
    }

    public boolean isBytes() {
        return this.bodyAsBytes != null;
    }

    public String toString() {
        return "content length=" + (this.bodyAsBytes != null ? Integer.valueOf(this.bodyAsBytes.length) : "chunked") + ", content type=" + this.contentType;
    }
}

