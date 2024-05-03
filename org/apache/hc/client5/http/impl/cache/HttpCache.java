/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.cache.Variant;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.util.ByteArrayBuffer;

interface HttpCache {
    public String generateKey(HttpHost var1, HttpRequest var2, HttpCacheEntry var3);

    public void flushCacheEntriesFor(HttpHost var1, HttpRequest var2);

    public void flushCacheEntriesInvalidatedByRequest(HttpHost var1, HttpRequest var2);

    public void flushCacheEntriesInvalidatedByExchange(HttpHost var1, HttpRequest var2, HttpResponse var3);

    public HttpCacheEntry getCacheEntry(HttpHost var1, HttpRequest var2);

    public Map<String, Variant> getVariantCacheEntriesWithEtags(HttpHost var1, HttpRequest var2);

    public HttpCacheEntry createCacheEntry(HttpHost var1, HttpRequest var2, HttpResponse var3, ByteArrayBuffer var4, Date var5, Date var6);

    public HttpCacheEntry updateCacheEntry(HttpHost var1, HttpRequest var2, HttpCacheEntry var3, HttpResponse var4, Date var5, Date var6);

    public HttpCacheEntry updateVariantCacheEntry(HttpHost var1, HttpRequest var2, HttpResponse var3, Variant var4, Date var5, Date var6);

    public void reuseVariantEntryFor(HttpHost var1, HttpRequest var2, Variant var3);
}

