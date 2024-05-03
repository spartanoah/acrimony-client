/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.net.URI;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public interface HttpCacheInvalidator {
    public void flushCacheEntriesInvalidatedByRequest(HttpHost var1, HttpRequest var2, Resolver<URI, String> var3, HttpCacheStorage var4);

    public void flushCacheEntriesInvalidatedByExchange(HttpHost var1, HttpRequest var2, HttpResponse var3, Resolver<URI, String> var4, HttpCacheStorage var5);
}

