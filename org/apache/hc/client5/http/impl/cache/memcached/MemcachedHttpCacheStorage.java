/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.spy.memcached.CASResponse
 *  net.spy.memcached.CASValue
 *  net.spy.memcached.MemcachedClient
 *  net.spy.memcached.OperationTimeoutException
 */
package org.apache.hc.client5.http.impl.cache.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.AbstractBinaryCacheStorage;
import org.apache.hc.client5.http.impl.cache.ByteArrayCacheEntrySerializer;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.memcached.KeyHashingScheme;
import org.apache.hc.client5.http.impl.cache.memcached.MemcachedOperationTimeoutException;
import org.apache.hc.client5.http.impl.cache.memcached.SHA256KeyHashingScheme;
import org.apache.hc.core5.util.Args;

public class MemcachedHttpCacheStorage
extends AbstractBinaryCacheStorage<CASValue<Object>> {
    private final MemcachedClient client;
    private final KeyHashingScheme keyHashingScheme;

    public MemcachedHttpCacheStorage(InetSocketAddress address) throws IOException {
        this(new MemcachedClient(new InetSocketAddress[]{address}));
    }

    public MemcachedHttpCacheStorage(MemcachedClient cache) {
        this(cache, CacheConfig.DEFAULT, ByteArrayCacheEntrySerializer.INSTANCE, SHA256KeyHashingScheme.INSTANCE);
    }

    public MemcachedHttpCacheStorage(MemcachedClient client, CacheConfig config, HttpCacheEntrySerializer<byte[]> serializer, KeyHashingScheme keyHashingScheme) {
        super((config != null ? config : CacheConfig.DEFAULT).getMaxUpdateRetries(), serializer != null ? serializer : ByteArrayCacheEntrySerializer.INSTANCE);
        this.client = Args.notNull(client, "Memcached client");
        this.keyHashingScheme = keyHashingScheme;
    }

    @Override
    protected String digestToStorageKey(String key) {
        return this.keyHashingScheme.hash(key);
    }

    @Override
    protected void store(String storageKey, byte[] storageObject) throws ResourceIOException {
        this.client.set(storageKey, 0, (Object)storageObject);
    }

    private byte[] castAsByteArray(Object storageObject) throws ResourceIOException {
        if (storageObject == null) {
            return null;
        }
        if (storageObject instanceof byte[]) {
            return (byte[])storageObject;
        }
        throw new ResourceIOException("Unexpected cache content: " + storageObject.getClass());
    }

    @Override
    protected byte[] restore(String storageKey) throws ResourceIOException {
        try {
            return this.castAsByteArray(this.client.get(storageKey));
        } catch (OperationTimeoutException ex) {
            throw new MemcachedOperationTimeoutException(ex);
        }
    }

    @Override
    protected CASValue<Object> getForUpdateCAS(String storageKey) throws ResourceIOException {
        try {
            return this.client.gets(storageKey);
        } catch (OperationTimeoutException ex) {
            throw new MemcachedOperationTimeoutException(ex);
        }
    }

    @Override
    protected byte[] getStorageObject(CASValue<Object> casValue) throws ResourceIOException {
        return this.castAsByteArray(casValue.getValue());
    }

    @Override
    protected boolean updateCAS(String storageKey, CASValue<Object> casValue, byte[] storageObject) throws ResourceIOException {
        CASResponse casResult = this.client.cas(storageKey, casValue.getCas(), (Object)storageObject);
        return casResult == CASResponse.OK;
    }

    @Override
    protected void delete(String storageKey) throws ResourceIOException {
        this.client.delete(storageKey);
    }

    @Override
    protected Map<String, byte[]> bulkRestore(Collection<String> storageKeys) throws ResourceIOException {
        Map storageObjectMap = this.client.getBulk(storageKeys);
        HashMap<String, byte[]> resultMap = new HashMap<String, byte[]>(storageObjectMap.size());
        for (Map.Entry resultEntry : storageObjectMap.entrySet()) {
            resultMap.put((String)resultEntry.getKey(), this.castAsByteArray(resultEntry.getValue()));
        }
        return resultMap;
    }
}

