/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheEntrySerializer;
import org.apache.hc.client5.http.cache.HttpCacheStorageEntry;
import org.apache.hc.client5.http.cache.HttpCacheUpdateException;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.ComplexCancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.util.Args;

public abstract class AbstractSerializingAsyncCacheStorage<T, CAS>
implements HttpAsyncCacheStorage {
    private final int maxUpdateRetries;
    private final HttpCacheEntrySerializer<T> serializer;

    public AbstractSerializingAsyncCacheStorage(int maxUpdateRetries, HttpCacheEntrySerializer<T> serializer) {
        this.maxUpdateRetries = Args.notNegative(maxUpdateRetries, "Max retries");
        this.serializer = Args.notNull(serializer, "Cache entry serializer");
    }

    protected abstract String digestToStorageKey(String var1);

    protected abstract T getStorageObject(CAS var1) throws ResourceIOException;

    protected abstract Cancellable store(String var1, T var2, FutureCallback<Boolean> var3);

    protected abstract Cancellable restore(String var1, FutureCallback<T> var2);

    protected abstract Cancellable getForUpdateCAS(String var1, FutureCallback<CAS> var2);

    protected abstract Cancellable updateCAS(String var1, CAS var2, T var3, FutureCallback<Boolean> var4);

    protected abstract Cancellable delete(String var1, FutureCallback<Boolean> var2);

    protected abstract Cancellable bulkRestore(Collection<String> var1, FutureCallback<Map<String, T>> var2);

    @Override
    public final Cancellable putEntry(String key, HttpCacheEntry entry, FutureCallback<Boolean> callback) {
        Args.notNull(key, "Storage key");
        Args.notNull(callback, "Callback");
        try {
            String storageKey = this.digestToStorageKey(key);
            T storageObject = this.serializer.serialize(new HttpCacheStorageEntry(key, entry));
            return this.store(storageKey, storageObject, callback);
        } catch (Exception ex) {
            callback.failed(ex);
            return Operations.nonCancellable();
        }
    }

    @Override
    public final Cancellable getEntry(final String key, final FutureCallback<HttpCacheEntry> callback) {
        Args.notNull(key, "Storage key");
        Args.notNull(callback, "Callback");
        try {
            String storageKey = this.digestToStorageKey(key);
            return this.restore(storageKey, new FutureCallback<T>(){

                @Override
                public void completed(T storageObject) {
                    try {
                        if (storageObject != null) {
                            HttpCacheStorageEntry entry = AbstractSerializingAsyncCacheStorage.this.serializer.deserialize(storageObject);
                            if (key.equals(entry.getKey())) {
                                callback.completed(entry.getContent());
                            } else {
                                callback.completed(null);
                            }
                        } else {
                            callback.completed(null);
                        }
                    } catch (Exception ex) {
                        callback.failed(ex);
                    }
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
        } catch (Exception ex) {
            callback.failed(ex);
            return Operations.nonCancellable();
        }
    }

    @Override
    public final Cancellable removeEntry(String key, FutureCallback<Boolean> callback) {
        Args.notNull(key, "Storage key");
        Args.notNull(callback, "Callback");
        try {
            String storageKey = this.digestToStorageKey(key);
            return this.delete(storageKey, callback);
        } catch (Exception ex) {
            callback.failed(ex);
            return Operations.nonCancellable();
        }
    }

    @Override
    public final Cancellable updateEntry(String key, HttpCacheCASOperation casOperation, FutureCallback<Boolean> callback) {
        Args.notNull(key, "Storage key");
        Args.notNull(casOperation, "CAS operation");
        Args.notNull(callback, "Callback");
        ComplexCancellable complexCancellable = new ComplexCancellable();
        AtomicInteger count = new AtomicInteger(0);
        this.atemmptUpdateEntry(key, casOperation, complexCancellable, count, callback);
        return complexCancellable;
    }

    private void atemmptUpdateEntry(final String key, final HttpCacheCASOperation casOperation, final ComplexCancellable complexCancellable, final AtomicInteger count, final FutureCallback<Boolean> callback) {
        try {
            final String storageKey = this.digestToStorageKey(key);
            complexCancellable.setDependency(this.getForUpdateCAS(storageKey, new FutureCallback<CAS>(){

                @Override
                public void completed(CAS cas) {
                    try {
                        HttpCacheStorageEntry storageEntry;
                        HttpCacheStorageEntry httpCacheStorageEntry = storageEntry = cas != null ? AbstractSerializingAsyncCacheStorage.this.serializer.deserialize(AbstractSerializingAsyncCacheStorage.this.getStorageObject(cas)) : null;
                        if (storageEntry != null && !key.equals(storageEntry.getKey())) {
                            storageEntry = null;
                        }
                        HttpCacheEntry existingEntry = storageEntry != null ? storageEntry.getContent() : null;
                        HttpCacheEntry updatedEntry = casOperation.execute(existingEntry);
                        if (existingEntry == null) {
                            AbstractSerializingAsyncCacheStorage.this.putEntry(key, updatedEntry, callback);
                        } else {
                            Object storageObject = AbstractSerializingAsyncCacheStorage.this.serializer.serialize(new HttpCacheStorageEntry(key, updatedEntry));
                            complexCancellable.setDependency(AbstractSerializingAsyncCacheStorage.this.updateCAS(storageKey, cas, storageObject, new FutureCallback<Boolean>(){

                                @Override
                                public void completed(Boolean result) {
                                    if (result.booleanValue()) {
                                        callback.completed(result);
                                    } else if (!complexCancellable.isCancelled()) {
                                        int numRetries = count.incrementAndGet();
                                        if (numRetries >= AbstractSerializingAsyncCacheStorage.this.maxUpdateRetries) {
                                            callback.failed(new HttpCacheUpdateException("Cache update failed after " + numRetries + " retries"));
                                        } else {
                                            AbstractSerializingAsyncCacheStorage.this.atemmptUpdateEntry(key, casOperation, complexCancellable, count, callback);
                                        }
                                    }
                                }

                                @Override
                                public void failed(Exception ex) {
                                    callback.failed(ex);
                                }

                                @Override
                                public void cancelled() {
                                    callback.cancelled();
                                }
                            }));
                        }
                    } catch (Exception ex) {
                        callback.failed(ex);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    callback.failed(ex);
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                }
            }));
        } catch (Exception ex) {
            callback.failed(ex);
        }
    }

    @Override
    public final Cancellable getEntries(final Collection<String> keys, final FutureCallback<Map<String, HttpCacheEntry>> callback) {
        Args.notNull(keys, "Storage keys");
        Args.notNull(callback, "Callback");
        try {
            ArrayList<String> storageKeys = new ArrayList<String>(keys.size());
            for (String key : keys) {
                storageKeys.add(this.digestToStorageKey(key));
            }
            return this.bulkRestore(storageKeys, new FutureCallback<Map<String, T>>(){

                @Override
                public void completed(Map<String, T> storageObjectMap) {
                    try {
                        HashMap<String, HttpCacheEntry> resultMap = new HashMap<String, HttpCacheEntry>();
                        for (String key : keys) {
                            HttpCacheStorageEntry entry;
                            String storageKey = AbstractSerializingAsyncCacheStorage.this.digestToStorageKey(key);
                            Object storageObject = storageObjectMap.get(storageKey);
                            if (storageObject == null || !key.equals((entry = AbstractSerializingAsyncCacheStorage.this.serializer.deserialize(storageObject)).getKey())) continue;
                            resultMap.put(key, entry.getContent());
                        }
                        callback.completed(resultMap);
                    } catch (Exception ex) {
                        callback.failed(ex);
                    }
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
        } catch (Exception ex) {
            callback.failed(ex);
            return Operations.nonCancellable();
        }
    }
}

