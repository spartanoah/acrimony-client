/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.net.URI;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;

@Internal
public interface HttpAsyncCacheInvalidator {
    public Cancellable flushCacheEntriesInvalidatedByRequest(HttpHost var1, HttpRequest var2, Resolver<URI, String> var3, HttpAsyncCacheStorage var4, FutureCallback<Boolean> var5);

    public Cancellable flushCacheEntriesInvalidatedByExchange(HttpHost var1, HttpRequest var2, HttpResponse var3, Resolver<URI, String> var4, HttpAsyncCacheStorage var5, FutureCallback<Boolean> var6);
}

