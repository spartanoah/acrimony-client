/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.Closeable;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheMap;
import org.apache.hc.client5.http.impl.cache.ResourceReference;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public class ManagedHttpCacheStorage
implements HttpCacheStorage,
Closeable {
    private final CacheMap entries;
    private final ReferenceQueue<HttpCacheEntry> morque;
    private final Set<ResourceReference> resources;
    private final AtomicBoolean active;

    public ManagedHttpCacheStorage(CacheConfig config) {
        this.entries = new CacheMap(config.getMaxCacheEntries());
        this.morque = new ReferenceQueue();
        this.resources = new HashSet<ResourceReference>();
        this.active = new AtomicBoolean(true);
    }

    private void ensureValidState() {
        if (!this.active.get()) {
            throw new IllegalStateException("Cache has been shut down");
        }
    }

    private void keepResourceReference(HttpCacheEntry entry) {
        Resource resource = entry.getResource();
        if (resource != null) {
            ResourceReference ref = new ResourceReference(entry, this.morque);
            this.resources.add(ref);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void putEntry(String url, HttpCacheEntry entry) throws ResourceIOException {
        Args.notNull(url, "URL");
        Args.notNull(entry, "Cache entry");
        this.ensureValidState();
        ManagedHttpCacheStorage managedHttpCacheStorage = this;
        synchronized (managedHttpCacheStorage) {
            this.entries.put(url, entry);
            this.keepResourceReference(entry);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public HttpCacheEntry getEntry(String url) throws ResourceIOException {
        Args.notNull(url, "URL");
        this.ensureValidState();
        ManagedHttpCacheStorage managedHttpCacheStorage = this;
        synchronized (managedHttpCacheStorage) {
            return (HttpCacheEntry)this.entries.get(url);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeEntry(String url) throws ResourceIOException {
        Args.notNull(url, "URL");
        this.ensureValidState();
        ManagedHttpCacheStorage managedHttpCacheStorage = this;
        synchronized (managedHttpCacheStorage) {
            this.entries.remove(url);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateEntry(String url, HttpCacheCASOperation casOperation) throws ResourceIOException {
        Args.notNull(url, "URL");
        Args.notNull(casOperation, "CAS operation");
        this.ensureValidState();
        ManagedHttpCacheStorage managedHttpCacheStorage = this;
        synchronized (managedHttpCacheStorage) {
            HttpCacheEntry existing = (HttpCacheEntry)this.entries.get(url);
            HttpCacheEntry updated = casOperation.execute(existing);
            this.entries.put(url, updated);
            if (existing != updated) {
                this.keepResourceReference(updated);
            }
        }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cleanResources() {
        if (this.active.get()) {
            ResourceReference ref;
            while ((ref = (ResourceReference)this.morque.poll()) != null) {
                ManagedHttpCacheStorage managedHttpCacheStorage = this;
                synchronized (managedHttpCacheStorage) {
                    this.resources.remove(ref);
                }
                ref.getResource().dispose();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void shutdown() {
        if (this.active.compareAndSet(true, false)) {
            ManagedHttpCacheStorage managedHttpCacheStorage = this;
            synchronized (managedHttpCacheStorage) {
                this.entries.clear();
                for (ResourceReference ref : this.resources) {
                    ref.getResource().dispose();
                }
                this.resources.clear();
                while (this.morque.poll() != null) {
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        if (this.active.compareAndSet(true, false)) {
            ManagedHttpCacheStorage managedHttpCacheStorage = this;
            synchronized (managedHttpCacheStorage) {
                ResourceReference ref;
                while ((ref = (ResourceReference)this.morque.poll()) != null) {
                    this.resources.remove(ref);
                    ref.getResource().dispose();
                }
            }
        }
    }
}

