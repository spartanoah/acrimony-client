/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.util.Iterator;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.util.Args;

public final class SimpleHttpResponse
extends BasicHttpResponse {
    private static final long serialVersionUID = 1L;
    private SimpleBody body;

    public SimpleHttpResponse(int code) {
        super(code);
    }

    public SimpleHttpResponse(int code, String reasonPhrase) {
        super(code, reasonPhrase);
    }

    public static SimpleHttpResponse copy(HttpResponse original) {
        Args.notNull(original, "HTTP response");
        SimpleHttpResponse copy = new SimpleHttpResponse(original.getCode());
        copy.setVersion(original.getVersion());
        Iterator<Header> it = original.headerIterator();
        while (it.hasNext()) {
            copy.addHeader(it.next());
        }
        return copy;
    }

    public static SimpleHttpResponse create(int code) {
        return new SimpleHttpResponse(code);
    }

    public static SimpleHttpResponse create(int code, String content, ContentType contentType) {
        SimpleHttpResponse response = new SimpleHttpResponse(code);
        if (content != null) {
            response.setBody(content, contentType);
        }
        return response;
    }

    public static SimpleHttpResponse create(int code, String content) {
        return SimpleHttpResponse.create(code, content, ContentType.TEXT_PLAIN);
    }

    public static SimpleHttpResponse create(int code, byte[] content, ContentType contentType) {
        SimpleHttpResponse response = new SimpleHttpResponse(code);
        if (content != null) {
            response.setBody(content, contentType);
        }
        return response;
    }

    public static SimpleHttpResponse create(int code, byte[] content) {
        return SimpleHttpResponse.create(code, content, ContentType.TEXT_PLAIN);
    }

    public void setBody(SimpleBody body) {
        this.body = body;
    }

    public void setBody(byte[] bodyBytes, ContentType contentType) {
        this.body = SimpleBody.create(bodyBytes, contentType);
    }

    public void setBody(String bodyText, ContentType contentType) {
        this.body = SimpleBody.create(bodyText, contentType);
    }

    public SimpleBody getBody() {
        return this.body;
    }

    public ContentType getContentType() {
        return this.body != null ? this.body.getContentType() : null;
    }

    public String getBodyText() {
        return this.body != null ? this.body.getBodyText() : null;
    }

    public byte[] getBodyBytes() {
        return this.body != null ? this.body.getBodyBytes() : null;
    }
}

