/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class LRUMap<K, V>
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final transient int _maxEntries;
    protected final transient ConcurrentHashMap<K, V> _map;
    protected transient int _jdkSerializeMaxEntries;

    public LRUMap(int initialEntries, int maxEntries) {
        this._map = new ConcurrentHashMap(initialEntries, 0.8f, 4);
        this._maxEntries = maxEntries;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V put(K key, V value) {
        if (this._map.size() >= this._maxEntries) {
            LRUMap lRUMap = this;
            synchronized (lRUMap) {
                if (this._map.size() >= this._maxEntries) {
                    this.clear();
                }
            }
        }
        return this._map.put(key, value);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V putIfAbsent(K key, V value) {
        if (this._map.size() >= this._maxEntries) {
            LRUMap lRUMap = this;
            synchronized (lRUMap) {
                if (this._map.size() >= this._maxEntries) {
                    this.clear();
                }
            }
        }
        return this._map.putIfAbsent(key, value);
    }

    public V get(Object key) {
        return this._map.get(key);
    }

    public void clear() {
        this._map.clear();
    }

    public int size() {
        return this._map.size();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this._jdkSerializeMaxEntries = in.readInt();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this._jdkSerializeMaxEntries);
    }

    protected Object readResolve() {
        return new LRUMap<K, V>(this._jdkSerializeMaxEntries, this._jdkSerializeMaxEntries);
    }
}

