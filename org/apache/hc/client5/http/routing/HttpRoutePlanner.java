/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.routing;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface HttpRoutePlanner {
    public HttpRoute determineRoute(HttpHost var1, HttpContext var2) throws HttpException;
}

