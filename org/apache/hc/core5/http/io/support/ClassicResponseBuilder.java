/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.util.Iterator;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.util.Args;

public class ClassicResponseBuilder {
    private int status;
    private ProtocolVersion version;
    private HeaderGroup headerGroup;
    private HttpEntity entity;

    ClassicResponseBuilder() {
    }

    ClassicResponseBuilder(int status) {
        this.status = status;
    }

    public static ClassicResponseBuilder create(int status) {
        Args.checkRange(status, 100, 599, "HTTP status code");
        return new ClassicResponseBuilder(status);
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public ClassicResponseBuilder setVersion(ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public Header[] getHeaders(String name) {
        return this.headerGroup != null ? this.headerGroup.getHeaders(name) : null;
    }

    public ClassicResponseBuilder setHeaders(Header ... headers) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeaders(headers);
        return this;
    }

    public Header getFirstHeader(String name) {
        return this.headerGroup != null ? this.headerGroup.getFirstHeader(name) : null;
    }

    public Header getLastHeader(String name) {
        return this.headerGroup != null ? this.headerGroup.getLastHeader(name) : null;
    }

    public ClassicResponseBuilder addHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(header);
        return this;
    }

    public ClassicResponseBuilder addHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public ClassicResponseBuilder removeHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.removeHeader(header);
        return this;
    }

    public ClassicResponseBuilder removeHeaders(String name) {
        if (name == null || this.headerGroup == null) {
            return this;
        }
        Iterator<Header> i = this.headerGroup.headerIterator();
        while (i.hasNext()) {
            Header header = i.next();
            if (!name.equalsIgnoreCase(header.getName())) continue;
            i.remove();
        }
        return this;
    }

    public ClassicResponseBuilder setHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(header);
        return this;
    }

    public ClassicResponseBuilder setHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(new BasicHeader(name, value));
        return this;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public ClassicResponseBuilder setEntity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    public ClassicResponseBuilder setEntity(String content, ContentType contentType) {
        this.entity = new StringEntity(content, contentType);
        return this;
    }

    public ClassicResponseBuilder setEntity(String content) {
        this.entity = new StringEntity(content);
        return this;
    }

    public ClassicResponseBuilder setEntity(byte[] content, ContentType contentType) {
        this.entity = new ByteArrayEntity(content, contentType);
        return this;
    }

    public ClassicHttpResponse build() {
        BasicClassicHttpResponse result = new BasicClassicHttpResponse(this.status);
        result.setVersion(this.version != null ? this.version : HttpVersion.HTTP_1_1);
        if (this.headerGroup != null) {
            result.setHeaders(this.headerGroup.getHeaders());
        }
        result.setEntity(this.entity);
        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClassicResponseBuilder [method=");
        builder.append(this.status);
        builder.append(", status=");
        builder.append(this.status);
        builder.append(", version=");
        builder.append(this.version);
        builder.append(", headerGroup=");
        builder.append(this.headerGroup);
        builder.append(", entity=");
        builder.append(this.entity != null ? this.entity.getClass() : null);
        builder.append("]");
        return builder.toString();
    }
}

