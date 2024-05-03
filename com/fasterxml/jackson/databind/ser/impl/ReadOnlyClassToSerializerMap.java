/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.util.TypeKey;
import java.util.HashMap;
import java.util.Map;

public final class ReadOnlyClassToSerializerMap {
    private final Bucket[] _buckets;
    private final int _size;
    private final int _mask;

    public ReadOnlyClassToSerializerMap(Map<TypeKey, JsonSerializer<Object>> serializers) {
        int size;
        this._size = size = ReadOnlyClassToSerializerMap.findSize(serializers.size());
        this._mask = size - 1;
        Bucket[] buckets = new Bucket[size];
        for (Map.Entry<TypeKey, JsonSerializer<Object>> entry : serializers.entrySet()) {
            TypeKey key = entry.getKey();
            int index = key.hashCode() & this._mask;
            buckets[index] = new Bucket(buckets[index], key, entry.getValue());
        }
        this._buckets = buckets;
    }

    private static final int findSize(int size) {
        int result;
        int needed = size <= 64 ? size + size : size + (size >> 2);
        for (result = 8; result < needed; result += result) {
        }
        return result;
    }

    public static ReadOnlyClassToSerializerMap from(HashMap<TypeKey, JsonSerializer<Object>> src) {
        return new ReadOnlyClassToSerializerMap(src);
    }

    public int size() {
        return this._size;
    }

    public JsonSerializer<Object> typedValueSerializer(JavaType type) {
        Bucket bucket = this._buckets[TypeKey.typedHash(type) & this._mask];
        if (bucket == null) {
            return null;
        }
        if (bucket.matchesTyped(type)) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (!bucket.matchesTyped(type)) continue;
            return bucket.value;
        }
        return null;
    }

    public JsonSerializer<Object> typedValueSerializer(Class<?> type) {
        Bucket bucket = this._buckets[TypeKey.typedHash(type) & this._mask];
        if (bucket == null) {
            return null;
        }
        if (bucket.matchesTyped(type)) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (!bucket.matchesTyped(type)) continue;
            return bucket.value;
        }
        return null;
    }

    public JsonSerializer<Object> untypedValueSerializer(JavaType type) {
        Bucket bucket = this._buckets[TypeKey.untypedHash(type) & this._mask];
        if (bucket == null) {
            return null;
        }
        if (bucket.matchesUntyped(type)) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (!bucket.matchesUntyped(type)) continue;
            return bucket.value;
        }
        return null;
    }

    public JsonSerializer<Object> untypedValueSerializer(Class<?> type) {
        Bucket bucket = this._buckets[TypeKey.untypedHash(type) & this._mask];
        if (bucket == null) {
            return null;
        }
        if (bucket.matchesUntyped(type)) {
            return bucket.value;
        }
        while ((bucket = bucket.next) != null) {
            if (!bucket.matchesUntyped(type)) continue;
            return bucket.value;
        }
        return null;
    }

    private static final class Bucket {
        public final JsonSerializer<Object> value;
        public final Bucket next;
        protected final Class<?> _class;
        protected final JavaType _type;
        protected final boolean _isTyped;

        public Bucket(Bucket next, TypeKey key, JsonSerializer<Object> value) {
            this.next = next;
            this.value = value;
            this._isTyped = key.isTyped();
            this._class = key.getRawType();
            this._type = key.getType();
        }

        public boolean matchesTyped(Class<?> key) {
            return this._class == key && this._isTyped;
        }

        public boolean matchesUntyped(Class<?> key) {
            return this._class == key && !this._isTyped;
        }

        public boolean matchesTyped(JavaType key) {
            return this._isTyped && key.equals(this._type);
        }

        public boolean matchesUntyped(JavaType key) {
            return !this._isTyped && key.equals(this._type);
        }
    }
}

