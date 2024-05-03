/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapPopulator {
    public static <K, V> Map<K, V> createMap(Iterable<K> keys, Iterable<V> values) {
        return MapPopulator.populateMap(keys, values, Maps.newLinkedHashMap());
    }

    public static <K, V> Map<K, V> populateMap(Iterable<K> keys, Iterable<V> values, Map<K, V> map) {
        Iterator<V> iterator = values.iterator();
        for (K k : keys) {
            map.put(k, iterator.next());
        }
        if (iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return map;
    }
}

