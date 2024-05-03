/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.ehcache.Cache
 */
package org.apache.hc.client5.http.impl.cache.ehcache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.AbstractSerializingCacheStorage;
import org.apache.hc.client5.http.impl.cache.ByteArrayCacheEntrySerializer;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.NoopCacheEntrySerializer;
import org.apache.hc.core5.util.Args;
import org.ehcache.Cache;

public class EhcacheHttpCacheStorage<T>
extends AbstractSerializingCacheStorage<T, T> {
    private final Cache<String, T> cache;

    public static EhcacheHttpCacheStorage<HttpCacheStorageEntry> createObjectCache(Cache<String, HttpCacheStorageEntry> cache, CacheConfig config) {
        return new EhcacheHttpCacheStorage<HttpCacheStorageEntry>(cache, config, NoopCacheEntrySerializer.INSTANCE);
    }

    public static EhcacheHttpCacheStorage<byte[]> createSerializedCache(Cache<String, byte[]> cache, CacheConfig config) {
        return new EhcacheHttpCacheStorage<byte[]>(cache, config, ByteArrayCacheEntrySerializer.INSTANCE);
    }

    public EhcacheHttpCacheStorage(Cache<String, T> cache, CacheConfig config, HttpCacheEntrySerializer<T> serializer) {
        super((config != null ? config : CacheConfig.DEFAULT).getMaxUpdateRetries(), serializer);
        this.cache = Args.notNull(cache, "Ehcache");
    }

    @Override
    protected String digestToStorageKey(String key) {
        return key;
    }

    @Override
    protected void store(String storageKey, T storageObject) throws ResourceIOException {
        this.cache.put((Object)storageKey, storageObject);
    }

    @Override
    protected T restore(String storageKey) throws ResourceIOException {
        return (T)this.cache.get((Object)storageKey);
    }

    @Override
    protected T getForUpdateCAS(String storageKey) throws ResourceIOException {
        return (T)this.cache.get((Object)storageKey);
    }

    @Override
    protected T getStorageObject(T element) throws ResourceIOException {
        return element;
    }

    @Override
    protected boolean updateCAS(String storageKey, T oldStorageObject, T storageObject) throws ResourceIOException {
        return this.cache.replace((Object)storageKey, oldStorageObject, storageObject);
    }

    @Override
    protected void delete(String storageKey) throws ResourceIOException {
        this.cache.remove((Object)storageKey);
    }

    @Override
    protected Map<String, T> bulkRestore(Collection<String> storageKeys) throws ResourceIOException {
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        for (String storageKey : storageKeys) {
            Object storageObject = this.cache.get((Object)storageKey);
            if (storageObject == null) continue;
            resultMap.put(storageKey, storageObject);
        }
        return resultMap;
    }
}

