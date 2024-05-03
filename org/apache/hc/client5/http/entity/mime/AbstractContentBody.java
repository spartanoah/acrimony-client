/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.nio.charset.Charset;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

public abstract class AbstractContentBody
implements ContentBody {
    private final ContentType contentType;

    public AbstractContentBody(ContentType contentType) {
        Args.notNull(contentType, "Content type");
        this.contentType = contentType;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    @Override
    public String getMimeType() {
        return this.contentType.getMimeType();
    }

    @Override
    public String getMediaType() {
        String mimeType = this.contentType.getMimeType();
        int i = mimeType.indexOf(47);
        if (i != -1) {
            return mimeType.substring(0, i);
        }
        return mimeType;
    }

    @Override
    public String getSubType() {
        String mimeType = this.contentType.getMimeType();
        int i = mimeType.indexOf(47);
        if (i != -1) {
            return mimeType.substring(i + 1);
        }
        return null;
    }

    @Override
    public String getCharset() {
        Charset charset = this.contentType.getCharset();
        return charset != null ? charset.name() : null;
    }
}

