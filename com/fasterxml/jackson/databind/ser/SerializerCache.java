/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap;
import com.fasterxml.jackson.databind.util.TypeKey;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class SerializerCache {
    private final HashMap<TypeKey, JsonSerializer<Object>> _sharedMap = new HashMap(64);
    private final AtomicReference<ReadOnlyClassToSerializerMap> _readOnlyMap = new AtomicReference();

    public ReadOnlyClassToSerializerMap getReadOnlyLookupMap() {
        ReadOnlyClassToSerializerMap m = this._readOnlyMap.get();
        if (m != null) {
            return m;
        }
        return this._makeReadOnlyLookupMap();
    }

    private final synchronized ReadOnlyClassToSerializerMap _makeReadOnlyLookupMap() {
        ReadOnlyClassToSerializerMap m = this._readOnlyMap.get();
        if (m == null) {
            m = ReadOnlyClassToSerializerMap.from(this._sharedMap);
            this._readOnlyMap.set(m);
        }
        return m;
    }

    public synchronized int size() {
        return this._sharedMap.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JsonSerializer<Object> untypedValueSerializer(Class<?> type) {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            return this._sharedMap.get(new TypeKey(type, false));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JsonSerializer<Object> untypedValueSerializer(JavaType type) {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            return this._sharedMap.get(new TypeKey(type, false));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JsonSerializer<Object> typedValueSerializer(JavaType type) {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            return this._sharedMap.get(new TypeKey(type, true));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JsonSerializer<Object> typedValueSerializer(Class<?> cls) {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            return this._sharedMap.get(new TypeKey(cls, true));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addTypedSerializer(JavaType type, JsonSerializer<Object> ser) {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            if (this._sharedMap.put(new TypeKey(type, true), ser) == null) {
                this._readOnlyMap.set(null);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addTypedSerializer(Class<?> cls, JsonSerializer<Object> ser) {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            if (this._sharedMap.put(new TypeKey(cls, true), ser) == null) {
                this._readOnlyMap.set(null);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addAndResolveNonTypedSerializer(Class<?> type, JsonSerializer<Object> ser, SerializerProvider provider) throws JsonMappingException {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            if (this._sharedMap.put(new TypeKey(type, false), ser) == null) {
                this._readOnlyMap.set(null);
            }
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer)((Object)ser)).resolve(provider);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addAndResolveNonTypedSerializer(JavaType type, JsonSerializer<Object> ser, SerializerProvider provider) throws JsonMappingException {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            if (this._sharedMap.put(new TypeKey(type, false), ser) == null) {
                this._readOnlyMap.set(null);
            }
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer)((Object)ser)).resolve(provider);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addAndResolveNonTypedSerializer(Class<?> rawType, JavaType fullType, JsonSerializer<Object> ser, SerializerProvider provider) throws JsonMappingException {
        SerializerCache serializerCache = this;
        synchronized (serializerCache) {
            JsonSerializer<Object> ob1 = this._sharedMap.put(new TypeKey(rawType, false), ser);
            JsonSerializer<Object> ob2 = this._sharedMap.put(new TypeKey(fullType, false), ser);
            if (ob1 == null || ob2 == null) {
                this._readOnlyMap.set(null);
            }
            if (ser instanceof ResolvableSerializer) {
                ((ResolvableSerializer)((Object)ser)).resolve(provider);
            }
        }
    }

    public synchronized void flush() {
        this._sharedMap.clear();
    }
}

