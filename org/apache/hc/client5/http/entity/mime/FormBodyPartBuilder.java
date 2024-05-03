/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.FormBodyPart;
import org.apache.hc.client5.http.entity.mime.Header;
import org.apache.hc.client5.http.entity.mime.MimeField;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;

public class FormBodyPartBuilder {
    private String name;
    private ContentBody body;
    private final Header header = new Header();

    public static FormBodyPartBuilder create(String name, ContentBody body) {
        return new FormBodyPartBuilder(name, body);
    }

    public static FormBodyPartBuilder create() {
        return new FormBodyPartBuilder();
    }

    FormBodyPartBuilder(String name, ContentBody body) {
        this();
        this.name = name;
        this.body = body;
    }

    FormBodyPartBuilder() {
    }

    public FormBodyPartBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public FormBodyPartBuilder setBody(ContentBody body) {
        this.body = body;
        return this;
    }

    public FormBodyPartBuilder addField(String name, String value, List<NameValuePair> parameters) {
        Args.notNull(name, "Field name");
        this.header.addField(new MimeField(name, value, parameters));
        return this;
    }

    public FormBodyPartBuilder addField(String name, String value) {
        Args.notNull(name, "Field name");
        this.header.addField(new MimeField(name, value));
        return this;
    }

    public FormBodyPartBuilder setField(String name, String value) {
        Args.notNull(name, "Field name");
        this.header.setField(new MimeField(name, value));
        return this;
    }

    public FormBodyPartBuilder removeFields(String name) {
        Args.notNull(name, "Field name");
        this.header.removeFields(name);
        return this;
    }

    public FormBodyPart build() {
        Asserts.notBlank(this.name, "Name");
        Asserts.notNull(this.body, "Content body");
        Header headerCopy = new Header();
        List<MimeField> fields = this.header.getFields();
        for (MimeField field : fields) {
            headerCopy.addField(field);
        }
        if (headerCopy.getField("Content-Disposition") == null) {
            ArrayList<NameValuePair> fieldParameters = new ArrayList<NameValuePair>();
            fieldParameters.add(new BasicNameValuePair("name", this.name));
            if (this.body.getFilename() != null) {
                fieldParameters.add(new BasicNameValuePair("filename", this.body.getFilename()));
            }
            headerCopy.addField(new MimeField("Content-Disposition", "form-data", fieldParameters));
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
        return new FormBodyPart(this.name, this.body, headerCopy);
    }
}

