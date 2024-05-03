/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.util.Iterator;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.support.BasicPushProducer;
import org.apache.hc.core5.util.Args;

public class AsyncPushBuilder {
    private int status;
    private HeaderGroup headerGroup;
    private AsyncEntityProducer entityProducer;

    AsyncPushBuilder() {
    }

    AsyncPushBuilder(int status) {
        this.status = status;
    }

    public static AsyncPushBuilder create(int status) {
        Args.checkRange(status, 100, 599, "HTTP status code");
        return new AsyncPushBuilder(status);
    }

    public Header[] getHeaders(String name) {
        return this.headerGroup != null ? this.headerGroup.getHeaders(name) : null;
    }

    public AsyncPushBuilder setHeaders(Header ... headers) {
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

    public AsyncPushBuilder addHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(header);
        return this;
    }

    public AsyncPushBuilder addHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public AsyncPushBuilder removeHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.removeHeader(header);
        return this;
    }

    public AsyncPushBuilder removeHeaders(String name) {
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

    public AsyncPushBuilder setHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(header);
        return this;
    }

    public AsyncPushBuilder setHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(new BasicHeader(name, value));
        return this;
    }

    public AsyncEntityProducer getEntity() {
        return this.entityProducer;
    }

    public AsyncPushBuilder setEntity(AsyncEntityProducer entityProducer) {
        this.entityProducer = entityProducer;
        return this;
    }

    public AsyncPushBuilder setEntity(String content, ContentType contentType) {
        this.entityProducer = new BasicAsyncEntityProducer(content, contentType);
        return this;
    }

    public AsyncPushBuilder setEntity(String content) {
        this.entityProducer = new BasicAsyncEntityProducer(content);
        return this;
    }

    public AsyncPushBuilder setEntity(byte[] content, ContentType contentType) {
        this.entityProducer = new BasicAsyncEntityProducer(content, contentType);
        return this;
    }

    public AsyncPushProducer build() {
        BasicHttpResponse response = new BasicHttpResponse(this.status);
        if (this.headerGroup != null) {
            response.setHeaders(this.headerGroup.getHeaders());
        }
        return new BasicPushProducer(response, this.entityProducer);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AsyncPushProducer [method=");
        builder.append(this.status);
        builder.append(", status=");
        builder.append(this.status);
        builder.append(", headerGroup=");
        builder.append(this.headerGroup);
        builder.append(", entity=");
        builder.append(this.entityProducer != null ? this.entityProducer.getClass() : null);
        builder.append("]");
        return builder.toString();
    }
}

