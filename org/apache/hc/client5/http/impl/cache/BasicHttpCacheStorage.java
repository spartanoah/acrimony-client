/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheMap;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public class BasicHttpCacheStorage
implements HttpCacheStorage {
    private final CacheMap entries;

    public BasicHttpCacheStorage(CacheConfig config) {
        this.entries = new CacheMap(config.getMaxCacheEntries());
    }

    @Override
    public synchronized void putEntry(String url, HttpCacheEntry entry) throws ResourceIOException {
        this.entries.put(url, entry);
    }

    @Override
    public synchronized HttpCacheEntry getEntry(String url) throws ResourceIOException {
        return (HttpCacheEntry)this.entries.get(url);
    }

    @Override
    public synchronized void removeEntry(String url) throws ResourceIOException {
        this.entries.remove(url);
    }

    @Override
    public synchronized void updateEntry(String url, HttpCacheCASOperation casOperation) throws ResourceIOException {
        HttpCacheEntry existingEntry = (HttpCacheEntry)this.entries.get(url);
        this.entries.put(url, casOperation.execute(existingEntry));
    }

    @Override
    public Map<String, HttpCacheEntry> getEntries(Collection<String> keys) throws ResourceIOException {
        Args.notNull(keys, "Key");
        HashMap<String, HttpCacheEntry> resultMap = new HashMap<String, HttpCacheEntry>(keys.size());
        for (String key : keys) {
            HttpCacheEntry entry = this.getEntry(key);
            if (entry == null) continue;
            resultMap.put(key, entry);
        }
        return resultMap;
    }
}

