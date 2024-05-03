/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.AbstractMessageWrapper;
import org.apache.hc.core5.net.URIAuthority;

public class HttpRequestWrapper
extends AbstractMessageWrapper
implements HttpRequest {
    private final HttpRequest message;

    public HttpRequestWrapper(HttpRequest message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMethod() {
        return this.message.getMethod();
    }

    @Override
    public String getPath() {
        return this.message.getPath();
    }

    @Override
    public void setPath(String path) {
        this.message.setPath(path);
    }

    @Override
    public String getScheme() {
        return this.message.getScheme();
    }

    @Override
    public void setScheme(String scheme) {
        this.message.setScheme(scheme);
    }

    @Override
    public URIAuthority getAuthority() {
        return this.message.getAuthority();
    }

    @Override
    public void setAuthority(URIAuthority authority) {
        this.message.setAuthority(authority);
    }

    @Override
    public String getRequestUri() {
        return this.message.getRequestUri();
    }

    @Override
    public URI getUri() throws URISyntaxException {
        return this.message.getUri();
    }

    @Override
    public void setUri(URI requestUri) {
        this.message.setUri(requestUri);
    }
}

