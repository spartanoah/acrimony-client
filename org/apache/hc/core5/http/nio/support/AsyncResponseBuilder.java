/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.util.Iterator;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.util.Args;

public class AsyncResponseBuilder {
    private int status;
    private ProtocolVersion version;
    private HeaderGroup headerGroup;
    private AsyncEntityProducer entityProducer;

    AsyncResponseBuilder() {
    }

    AsyncResponseBuilder(int status) {
        this.status = status;
    }

    public static AsyncResponseBuilder create(int status) {
        Args.checkRange(status, 100, 599, "HTTP status code");
        return new AsyncResponseBuilder(status);
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public AsyncResponseBuilder setVersion(ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public Header[] getHeaders(String name) {
        return this.headerGroup != null ? this.headerGroup.getHeaders(name) : null;
    }

    public AsyncResponseBuilder setHeaders(Header ... headers) {
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

    public AsyncResponseBuilder addHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(header);
        return this;
    }

    public AsyncResponseBuilder addHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public AsyncResponseBuilder removeHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.removeHeader(header);
        return this;
    }

    public AsyncResponseBuilder removeHeaders(String name) {
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

    public AsyncResponseBuilder setHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(header);
        return this;
    }

    public AsyncResponseBuilder setHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(new BasicHeader(name, value));
        return this;
    }

    public AsyncEntityProducer getEntity() {
        return this.entityProducer;
    }

    public AsyncResponseBuilder setEntity(AsyncEntityProducer entityProducer) {
        this.entityProducer = entityProducer;
        return this;
    }

    public AsyncResponseBuilder setEntity(String content, ContentType contentType) {
        this.entityProducer = new BasicAsyncEntityProducer(content, contentType);
        return this;
    }

    public AsyncResponseBuilder setEntity(String content) {
        this.entityProducer = new BasicAsyncEntityProducer(content);
        return this;
    }

    public AsyncResponseBuilder setEntity(byte[] content, ContentType contentType) {
        this.entityProducer = new BasicAsyncEntityProducer(content, contentType);
        return this;
    }

    public AsyncResponseProducer build() {
        BasicHttpResponse response = new BasicHttpResponse(this.status);
        if (this.headerGroup != null) {
            response.setHeaders(this.headerGroup.getHeaders());
        }
        if (this.version != null) {
            response.setVersion(this.version);
        }
        return new BasicResponseProducer((HttpResponse)response, this.entityProducer);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AsyncResponseBuilder [method=");
        builder.append(this.status);
        builder.append(", status=");
        builder.append(this.status);
        builder.append(", version=");
        builder.append(this.version);
        builder.append(", headerGroup=");
        builder.append(this.headerGroup);
        builder.append(", entity=");
        builder.append(this.entityProducer != null ? this.entityProducer.getClass() : null);
        builder.append("]");
        return builder.toString();
    }
}

