/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.util.Collection;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public final class HttpAsyncCacheStorageAdaptor
implements HttpAsyncCacheStorage {
    private final HttpCacheStorage cacheStorage;

    public HttpAsyncCacheStorageAdaptor(HttpCacheStorage cacheStorage) {
        this.cacheStorage = Args.notNull(cacheStorage, "Cache strorage");
    }

    @Override
    public Cancellable putEntry(String key, HttpCacheEntry entry, FutureCallback<Boolean> callback) {
        Args.notEmpty(key, "Key");
        Args.notNull(entry, "Cache ehtry");
        Args.notNull(callback, "Callback");
        try {
            this.cacheStorage.putEntry(key, entry);
            callback.completed(Boolean.TRUE);
        } catch (Exception ex) {
            callback.failed(ex);
        }
        return Operations.nonCancellable();
    }

    @Override
    public Cancellable getEntry(String key, FutureCallback<HttpCacheEntry> callback) {
        Args.notEmpty(key, "Key");
        Args.notNull(callback, "Callback");
        try {
            HttpCacheEntry entry = this.cacheStorage.getEntry(key);
            callback.completed(entry);
        } catch (Exception ex) {
            callback.failed(ex);
        }
        return Operations.nonCancellable();
    }

    @Override
    public Cancellable removeEntry(String key, FutureCallback<Boolean> callback) {
        Args.notEmpty(key, "Key");
        Args.notNull(callback, "Callback");
        try {
            this.cacheStorage.removeEntry(key);
            callback.completed(Boolean.TRUE);
        } catch (Exception ex) {
            callback.failed(ex);
        }
        return Operations.nonCancellable();
    }

    @Override
    public Cancellable updateEntry(String key, HttpCacheCASOperation casOperation, FutureCallback<Boolean> callback) {
        Args.notEmpty(key, "Key");
        Args.notNull(casOperation, "CAS operation");
        Args.notNull(callback, "Callback");
        try {
            this.cacheStorage.updateEntry(key, casOperation);
            callback.completed(Boolean.TRUE);
        } catch (Exception ex) {
            callback.failed(ex);
        }
        return Operations.nonCancellable();
    }

    @Override
    public Cancellable getEntries(Collection<String> keys, FutureCallback<Map<String, HttpCacheEntry>> callback) {
        Args.notNull(keys, "Key");
        Args.notNull(callback, "Callback");
        try {
            callback.completed(this.cacheStorage.getEntries(keys));
        } catch (Exception ex) {
            callback.failed(ex);
        }
        return Operations.nonCancellable();
    }
}

