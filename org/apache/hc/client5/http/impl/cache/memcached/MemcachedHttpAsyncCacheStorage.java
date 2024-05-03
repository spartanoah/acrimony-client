/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.spy.memcached.CASResponse
 *  net.spy.memcached.CASValue
 *  net.spy.memcached.MemcachedClient
 *  net.spy.memcached.internal.BulkFuture
 *  net.spy.memcached.internal.BulkGetCompletionListener
 *  net.spy.memcached.internal.BulkGetFuture
 *  net.spy.memcached.internal.GetCompletionListener
 *  net.spy.memcached.internal.GetFuture
 *  net.spy.memcached.internal.OperationCompletionListener
 *  net.spy.memcached.internal.OperationFuture
 */
package org.apache.hc.client5.http.impl.cache.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.BulkGetCompletionListener;
import net.spy.memcached.internal.BulkGetFuture;
import net.spy.memcached.internal.GetCompletionListener;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.client5.http.impl.cache.AbstractBinaryAsyncCacheStorage;
import org.apache.hc.client5.http.impl.cache.ByteArrayCacheEntrySerializer;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.memcached.KeyHashingScheme;
import org.apache.hc.client5.http.impl.cache.memcached.SHA256KeyHashingScheme;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.util.Args;

public class MemcachedHttpAsyncCacheStorage
extends AbstractBinaryAsyncCacheStorage<CASValue<Object>> {
    private final MemcachedClient client;
    private final KeyHashingScheme keyHashingScheme;

    public MemcachedHttpAsyncCacheStorage(InetSocketAddress address) throws IOException {
        this(new MemcachedClient(new InetSocketAddress[]{address}));
    }

    public MemcachedHttpAsyncCacheStorage(MemcachedClient cache) {
        this(cache, CacheConfig.DEFAULT, ByteArrayCacheEntrySerializer.INSTANCE, SHA256KeyHashingScheme.INSTANCE);
    }

    public MemcachedHttpAsyncCacheStorage(MemcachedClient client, CacheConfig config, HttpCacheEntrySerializer<byte[]> serializer, KeyHashingScheme keyHashingScheme) {
        super((config != null ? config : CacheConfig.DEFAULT).getMaxUpdateRetries(), serializer != null ? serializer : ByteArrayCacheEntrySerializer.INSTANCE);
        this.client = Args.notNull(client, "Memcached client");
        this.keyHashingScheme = keyHashingScheme;
    }

    @Override
    protected String digestToStorageKey(String key) {
        return this.keyHashingScheme.hash(key);
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
    protected byte[] getStorageObject(CASValue<Object> casValue) throws ResourceIOException {
        return this.castAsByteArray(casValue.getValue());
    }

    private <T> Cancellable operation(final OperationFuture<T> operationFuture, final FutureCallback<T> callback) {
        operationFuture.addListener(new OperationCompletionListener(){

            public void onComplete(OperationFuture<?> future) throws Exception {
                try {
                    callback.completed(operationFuture.get());
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof Exception) {
                        callback.failed((Exception)ex.getCause());
                    }
                    callback.failed(ex);
                }
            }
        });
        return Operations.cancellable(operationFuture);
    }

    @Override
    protected Cancellable store(String storageKey, byte[] storageObject, FutureCallback<Boolean> callback) {
        return this.operation(this.client.set(storageKey, 0, (Object)storageObject), callback);
    }

    @Override
    protected Cancellable restore(String storageKey, final FutureCallback<byte[]> callback) {
        final GetFuture getFuture = this.client.asyncGet(storageKey);
        getFuture.addListener(new GetCompletionListener(){

            public void onComplete(GetFuture<?> future) throws Exception {
                try {
                    callback.completed(MemcachedHttpAsyncCacheStorage.this.castAsByteArray(getFuture.get()));
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof Exception) {
                        callback.failed((Exception)ex.getCause());
                    }
                    callback.failed(ex);
                }
            }
        });
        return Operations.cancellable(getFuture);
    }

    @Override
    protected Cancellable getForUpdateCAS(String storageKey, FutureCallback<CASValue<Object>> callback) {
        return this.operation(this.client.asyncGets(storageKey), callback);
    }

    @Override
    protected Cancellable updateCAS(String storageKey, CASValue<Object> casValue, byte[] storageObject, final FutureCallback<Boolean> callback) {
        return this.operation(this.client.asyncCAS(storageKey, casValue.getCas(), (Object)storageObject), new FutureCallback<CASResponse>(){

            @Override
            public void completed(CASResponse result) {
                callback.completed(result == CASResponse.OK);
            }

            @Override
            public void failed(Exception ex) {
                callback.failed(ex);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        });
    }

    @Override
    protected Cancellable delete(String storageKey, FutureCallback<Boolean> callback) {
        return this.operation(this.client.delete(storageKey), callback);
    }

    @Override
    protected Cancellable bulkRestore(Collection<String> storageKeys, final FutureCallback<Map<String, byte[]>> callback) {
        BulkFuture future = this.client.asyncGetBulk(storageKeys);
        future.addListener(new BulkGetCompletionListener(){

            public void onComplete(BulkGetFuture<?> future) throws Exception {
                Map storageObjectMap = future.get();
                HashMap resultMap = new HashMap(storageObjectMap.size());
                for (Map.Entry resultEntry : storageObjectMap.entrySet()) {
                    resultMap.put(resultEntry.getKey(), MemcachedHttpAsyncCacheStorage.this.castAsByteArray(resultEntry.getValue()));
                }
                callback.completed(resultMap);
            }
        });
        return Operations.cancellable(future);
    }
}

