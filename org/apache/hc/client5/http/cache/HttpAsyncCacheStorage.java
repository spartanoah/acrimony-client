/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.util.Collection;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;

@Contract(threading=ThreadingBehavior.SAFE)
public interface HttpAsyncCacheStorage {
    public Cancellable putEntry(String var1, HttpCacheEntry var2, FutureCallback<Boolean> var3);

    public Cancellable getEntry(String var1, FutureCallback<HttpCacheEntry> var2);

    public Cancellable removeEntry(String var1, FutureCallback<Boolean> var2);

    public Cancellable updateEntry(String var1, HttpCacheCASOperation var2, FutureCallback<Boolean> var3);

    public Cancellable getEntries(Collection<String> var1, FutureCallback<Map<String, HttpCacheEntry>> var2);
}

