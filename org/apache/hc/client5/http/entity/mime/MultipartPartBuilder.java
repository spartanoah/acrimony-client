/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.util.List;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.Header;
import org.apache.hc.client5.http.entity.mime.MimeField;
import org.apache.hc.client5.http.entity.mime.MultipartPart;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public class MultipartPartBuilder {
    private ContentBody body;
    private final Header header = new Header();

    public static MultipartPartBuilder create(ContentBody body) {
        return new MultipartPartBuilder(body);
    }

    public static MultipartPartBuilder create() {
        return new MultipartPartBuilder();
    }

    MultipartPartBuilder(ContentBody body) {
        this();
        this.body = body;
    }

    MultipartPartBuilder() {
    }

    public MultipartPartBuilder setBody(ContentBody body) {
        this.body = body;
        return this;
    }

    public MultipartPartBuilder addHeader(String name, String value, List<NameValuePair> parameters) {
        Args.notNull(name, "Header name");
        this.header.addField(new MimeField(name, value, parameters));
        return this;
    }

    public MultipartPartBuilder addHeader(String name, String value) {
        Args.notNull(name, "Header name");
        this.header.addField(new MimeField(name, value));
        return this;
    }

    public MultipartPartBuilder setHeader(String name, String value) {
        Args.notNull(name, "Header name");
        this.header.setField(new MimeField(name, value));
        return this;
    }

    public MultipartPartBuilder removeHeaders(String name) {
        Args.notNull(name, "Header name");
        this.header.removeFields(name);
        return this;
    }

    public MultipartPart build() {
        Asserts.notNull(this.body, "Content body");
        Header headerCopy = new Header();
        List<MimeField> fields = this.header.getFields();
        for (MimeField field : fields) {
            headerCopy.addField(field);
        }
        if (headerCopy.getField("Content-Type") == null) {
            ContentType contentType = this.body instanceof AbstractContentBody ? ((AbstractContentBody)this.body).getContentType() : null;
            if (contentType != null) {
                headerCopy.addField(new MimeField("Content-Type", contentType.toString()));
            } else {
                StringBuilder buffer = new StringBuilder();
                buffer.append(this.body.getMimeType());
                if (this.body.getCharset() != null) {
                    buffer.append("; charset=");
                    buffer.append(this.body.getCharset());
                }
                headerCopy.addField(new MimeField("Content-Type", buffer.toString()));
            }
        }
        return new MultipartPart(this.body, headerCopy);
    }
}

