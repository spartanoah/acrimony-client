/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SoftCache<K, V>
implements Map<K, V> {
    private final Map<K, CacheReference<K, V>> map;
    private final ReferenceQueue<V> queue = new ReferenceQueue();

    public SoftCache() {
        this.map = new HashMap<K, CacheReference<K, V>>();
    }

    public SoftCache(int initialCapacity) {
        this.map = new HashMap<K, CacheReference<K, V>>(initialCapacity);
    }

    @Override
    public int size() {
        this.expungeStaleEntries();
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        this.expungeStaleEntries();
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        this.expungeStaleEntries();
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
        this.expungeStaleEntries();
        return this.getRef(this.map.get(key));
    }

    @Override
    public V put(K key, V value) {
        this.expungeStaleEntries();
        return this.getRef(this.map.put(key, new CacheReference<K, V>(key, value, this.queue)));
    }

    @Override
    public V remove(Object key) {
        this.expungeStaleEntries();
        return this.getRef(this.map.remove(key));
    }

    private V getRef(CacheReference<K, V> ref) {
        return ref != null ? (V)ref.get() : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.expungeStaleEntries();
        for (Map.Entry<K, V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        this.map.clear();
        this.expungeStaleEntries();
    }

    @Override
    public Set<K> keySet() {
        this.expungeStaleEntries();
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    private void expungeStaleEntries() {
        Reference<V> reference;
        while ((reference = this.queue.poll()) != null) {
            this.map.remove(((CacheReference)reference).key);
        }
    }

    private static class CacheReference<K, V>
    extends SoftReference<V> {
        final K key;

        CacheReference(K key, V value, ReferenceQueue<? super V> queue) {
            super(value, queue);
            this.key = key;
        }
    }
}

