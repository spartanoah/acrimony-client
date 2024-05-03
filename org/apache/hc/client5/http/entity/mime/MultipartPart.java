/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.Header;
import org.apache.hc.client5.http.entity.mime.MimeField;

public class MultipartPart {
    private final Header header;
    private final ContentBody body;

    MultipartPart(ContentBody body, Header header) {
        this.body = body;
        this.header = header != null ? header : new Header();
    }

    public ContentBody getBody() {
        return this.body;
    }

    public Header getHeader() {
        return this.header;
    }

    void addField(String name, String value) {
        this.addField(new MimeField(name, value));
    }

    void addField(MimeField field) {
        this.header.addField(field);
    }
}

