/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.net.URI;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHttpRequest;

public class BasicClassicHttpRequest
extends BasicHttpRequest
implements ClassicHttpRequest {
    private static final long serialVersionUID = 1L;
    private HttpEntity entity;

    public BasicClassicHttpRequest(String method, String path) {
        super(method, path);
    }

    public BasicClassicHttpRequest(String method, HttpHost host, String path) {
        super(method, host, path);
    }

    public BasicClassicHttpRequest(String method, URI requestUri) {
        super(method, requestUri);
    }

    public BasicClassicHttpRequest(Method method, String path) {
        super(method, path);
    }

    public BasicClassicHttpRequest(Method method, HttpHost host, String path) {
        super(method, host, path);
    }

    public BasicClassicHttpRequest(Method method, URI requestUri) {
        super(method, requestUri);
    }

    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }
}

