/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.net.URI;
import java.util.Iterator;
import org.apache.hc.client5.http.async.methods.ConfigurableHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.util.Args;

public final class SimpleHttpRequest
extends ConfigurableHttpRequest {
    private static final long serialVersionUID = 1L;
    private SimpleBody body;

    public static SimpleHttpRequest copy(HttpRequest original) {
        Args.notNull(original, "HTTP request");
        SimpleHttpRequest copy = new SimpleHttpRequest(original.getMethod(), original.getRequestUri());
        copy.setVersion(original.getVersion());
        Iterator<Header> it = original.headerIterator();
        while (it.hasNext()) {
            copy.addHeader(it.next());
        }
        copy.setScheme(original.getScheme());
        copy.setAuthority(original.getAuthority());
        return copy;
    }

    public SimpleHttpRequest(String method, String path) {
        super(method, path);
    }

    public SimpleHttpRequest(String method, HttpHost host, String path) {
        super(method, host, path);
    }

    public SimpleHttpRequest(String method, URI requestUri) {
        super(method, requestUri);
    }

    SimpleHttpRequest(Method method, URI requestUri) {
        this(method.name(), requestUri);
    }

    SimpleHttpRequest(Method method, HttpHost host, String path) {
        this(method.name(), host, path);
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

