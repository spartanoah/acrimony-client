/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.routing;

import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultProxyRoutePlanner
extends DefaultRoutePlanner {
    private final HttpHost proxy;

    public DefaultProxyRoutePlanner(HttpHost proxy, SchemePortResolver schemePortResolver) {
        super(schemePortResolver);
        this.proxy = Args.notNull(proxy, "Proxy host");
    }

    public DefaultProxyRoutePlanner(HttpHost proxy) {
        this(proxy, null);
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpContext context) throws HttpException {
        return this.proxy;
    }
}

