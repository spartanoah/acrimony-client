/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.HttpCacheUpdateException;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.util.Args;

public abstract class AbstractSerializingCacheStorage<T, CAS>
implements HttpCacheStorage {
    private final int maxUpdateRetries;
    private final HttpCacheEntrySerializer<T> serializer;

    public AbstractSerializingCacheStorage(int maxUpdateRetries, HttpCacheEntrySerializer<T> serializer) {
        this.maxUpdateRetries = Args.notNegative(maxUpdateRetries, "Max retries");
        this.serializer = Args.notNull(serializer, "Cache entry serializer");
    }

    protected abstract String digestToStorageKey(String var1);

    protected abstract void store(String var1, T var2) throws ResourceIOException;

    protected abstract T restore(String var1) throws ResourceIOException;

    protected abstract CAS getForUpdateCAS(String var1) throws ResourceIOException;

    protected abstract T getStorageObject(CAS var1) throws ResourceIOException;

    protected abstract boolean updateCAS(String var1, CAS var2, T var3) throws ResourceIOException;

    protected abstract void delete(String var1) throws ResourceIOException;

    protected abstract Map<String, T> bulkRestore(Collection<String> var1) throws ResourceIOException;

    @Override
    public final void putEntry(String key, HttpCacheEntry entry) throws ResourceIOException {
        String storageKey = this.digestToStorageKey(key);
        T storageObject = this.serializer.serialize(new HttpCacheStorageEntry(key, entry));
        this.store(storageKey, storageObject);
    }

    @Override
    public final HttpCacheEntry getEntry(String key) throws ResourceIOException {
        String storageKey = this.digestToStorageKey(key);
        T storageObject = this.restore(storageKey);
        if (storageObject == null) {
            return null;
        }
        HttpCacheStorageEntry entry = this.serializer.deserialize(storageObject);
        if (key.equals(entry.getKey())) {
            return entry.getContent();
        }
        return null;
    }

    @Override
    public final void removeEntry(String key) throws ResourceIOException {
        String storageKey = this.digestToStorageKey(key);
        this.delete(storageKey);
    }

    @Override
    public final void updateEntry(String key, HttpCacheCASOperation casOperation) throws HttpCacheUpdateException, ResourceIOException {
        block3: {
            int numRetries = 0;
            String storageKey = this.digestToStorageKey(key);
            do {
                CAS cas;
                HttpCacheStorageEntry storageEntry;
                HttpCacheStorageEntry httpCacheStorageEntry = storageEntry = (cas = this.getForUpdateCAS(storageKey)) != null ? this.serializer.deserialize(this.getStorageObject(cas)) : null;
                if (storageEntry != null && !key.equals(storageEntry.getKey())) {
                    storageEntry = null;
                }
                HttpCacheEntry existingEntry = storageEntry != null ? storageEntry.getContent() : null;
                HttpCacheEntry updatedEntry = casOperation.execute(existingEntry);
                if (existingEntry == null) {
                    this.putEntry(key, updatedEntry);
                    return;
                }
                T storageObject = this.serializer.serialize(new HttpCacheStorageEntry(key, updatedEntry));
                if (this.updateCAS(storageKey, cas, storageObject)) break block3;
            } while (++numRetries < this.maxUpdateRetries);
            throw new HttpCacheUpdateException("Cache update failed after " + numRetries + " retries");
        }
    }

    @Override
    public final Map<String, HttpCacheEntry> getEntries(Collection<String> keys) throws ResourceIOException {
        Args.notNull(keys, "Storage keys");
        ArrayList<String> storageKeys = new ArrayList<String>(keys.size());
        for (String key : keys) {
            storageKeys.add(this.digestToStorageKey(key));
        }
        Map<String, T> storageObjectMap = this.bulkRestore(storageKeys);
        HashMap<String, HttpCacheEntry> resultMap = new HashMap<String, HttpCacheEntry>();
        for (String key : keys) {
            HttpCacheStorageEntry entry;
            String storageKey = this.digestToStorageKey(key);
            T storageObject = storageObjectMap.get(storageKey);
            if (storageObject == null || !key.equals((entry = this.serializer.deserialize(storageObject)).getKey())) continue;
            resultMap.put(key, entry.getContent());
        }
        return resultMap;
    }
}

