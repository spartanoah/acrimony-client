/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nullable;

abstract class AbstractNavigableMap<K, V>
extends AbstractMap<K, V>
implements NavigableMap<K, V> {
    AbstractNavigableMap() {
    }

    @Override
    @Nullable
    public abstract V get(@Nullable Object var1);

    @Override
    @Nullable
    public Map.Entry<K, V> firstEntry() {
        return Iterators.getNext(this.entryIterator(), null);
    }

    @Override
    @Nullable
    public Map.Entry<K, V> lastEntry() {
        return Iterators.getNext(this.descendingEntryIterator(), null);
    }

    @Override
    @Nullable
    public Map.Entry<K, V> pollFirstEntry() {
        return Iterators.pollNext(this.entryIterator());
    }

    @Override
    @Nullable
    public Map.Entry<K, V> pollLastEntry() {
        return Iterators.pollNext(this.descendingEntryIterator());
    }

    @Override
    public K firstKey() {
        Map.Entry<K, V> entry = this.firstEntry();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry.getKey();
    }

    @Override
    public K lastKey() {
        Map.Entry<K, V> entry = this.lastEntry();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry.getKey();
    }

    @Override
    @Nullable
    public Map.Entry<K, V> lowerEntry(K key) {
        return this.headMap(key, false).lastEntry();
    }

    @Override
    @Nullable
    public Map.Entry<K, V> floorEntry(K key) {
        return this.headMap(key, true).lastEntry();
    }

    @Override
    @Nullable
    public Map.Entry<K, V> ceilingEntry(K key) {
        return this.tailMap(key, true).firstEntry();
    }

    @Override
    @Nullable
    public Map.Entry<K, V> higherEntry(K key) {
        return this.tailMap(key, false).firstEntry();
    }

    @Override
    public K lowerKey(K key) {
        return Maps.keyOrNull(this.lowerEntry(key));
    }

    @Override
    public K floorKey(K key) {
        return Maps.keyOrNull(this.floorEntry(key));
    }

    @Override
    public K ceilingKey(K key) {
        return Maps.keyOrNull(this.ceilingEntry(key));
    }

    @Override
    public K higherKey(K key) {
        return Maps.keyOrNull(this.higherEntry(key));
    }

    abstract Iterator<Map.Entry<K, V>> entryIterator();

    abstract Iterator<Map.Entry<K, V>> descendingEntryIterator();

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return this.subMap(fromKey, true, toKey, false);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return this.headMap(toKey, false);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return this.tailMap(fromKey, true);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new Maps.NavigableKeySet(this);
    }

    @Override
    public Set<K> keySet() {
        return this.navigableKeySet();
    }

    @Override
    public abstract int size();

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new Maps.EntrySet<K, V>(){

            @Override
            Map<K, V> map() {
                return AbstractNavigableMap.this;
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return AbstractNavigableMap.this.entryIterator();
            }
        };
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return this.descendingMap().navigableKeySet();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new DescendingMap();
    }

    private final class DescendingMap
    extends Maps.DescendingMap<K, V> {
        private DescendingMap() {
        }

        @Override
        NavigableMap<K, V> forward() {
            return AbstractNavigableMap.this;
        }

        @Override
        Iterator<Map.Entry<K, V>> entryIterator() {
            return AbstractNavigableMap.this.descendingEntryIterator();
        }
    }
}

