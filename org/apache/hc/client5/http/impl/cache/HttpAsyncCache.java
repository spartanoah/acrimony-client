/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.cache.Variant;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.util.ByteArrayBuffer;

interface HttpAsyncCache {
    public String generateKey(HttpHost var1, HttpRequest var2, HttpCacheEntry var3);

    public Cancellable flushCacheEntriesFor(HttpHost var1, HttpRequest var2, FutureCallback<Boolean> var3);

    public Cancellable flushCacheEntriesInvalidatedByRequest(HttpHost var1, HttpRequest var2, FutureCallback<Boolean> var3);

    public Cancellable flushCacheEntriesInvalidatedByExchange(HttpHost var1, HttpRequest var2, HttpResponse var3, FutureCallback<Boolean> var4);

    public Cancellable getCacheEntry(HttpHost var1, HttpRequest var2, FutureCallback<HttpCacheEntry> var3);

    public Cancellable getVariantCacheEntriesWithEtags(HttpHost var1, HttpRequest var2, FutureCallback<Map<String, Variant>> var3);

    public Cancellable createCacheEntry(HttpHost var1, HttpRequest var2, HttpResponse var3, ByteArrayBuffer var4, Date var5, Date var6, FutureCallback<HttpCacheEntry> var7);

    public Cancellable updateCacheEntry(HttpHost var1, HttpRequest var2, HttpCacheEntry var3, HttpResponse var4, Date var5, Date var6, FutureCallback<HttpCacheEntry> var7);

    public Cancellable updateVariantCacheEntry(HttpHost var1, HttpRequest var2, HttpResponse var3, Variant var4, Date var5, Date var6, FutureCallback<HttpCacheEntry> var7);

    public Cancellable reuseVariantEntryFor(HttpHost var1, HttpRequest var2, Variant var3, FutureCallback<Boolean> var4);
}

