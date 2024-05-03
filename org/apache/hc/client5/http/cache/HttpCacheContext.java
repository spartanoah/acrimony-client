/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;

public class HttpCacheContext
extends HttpClientContext {
    public static final String CACHE_RESPONSE_STATUS = "http.cache.response.status";

    public static HttpCacheContext adapt(HttpContext context) {
        if (context instanceof HttpCacheContext) {
            return (HttpCacheContext)context;
        }
        return new HttpCacheContext(context);
    }

    public static HttpCacheContext create() {
        return new HttpCacheContext(new BasicHttpContext());
    }

    public HttpCacheContext(HttpContext context) {
        super(context);
    }

    public HttpCacheContext() {
    }

    public CacheResponseStatus getCacheResponseStatus() {
        return this.getAttribute(CACHE_RESPONSE_STATUS, CacheResponseStatus.class);
    }
}

