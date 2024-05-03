/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.module;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SimpleSerializers
extends Serializers.Base
implements Serializable {
    private static final long serialVersionUID = 3L;
    protected HashMap<ClassKey, JsonSerializer<?>> _classMappings = null;
    protected HashMap<ClassKey, JsonSerializer<?>> _interfaceMappings = null;
    protected boolean _hasEnumSerializer = false;

    public SimpleSerializers() {
    }

    public SimpleSerializers(List<JsonSerializer<?>> sers) {
        this.addSerializers(sers);
    }

    public void addSerializer(JsonSerializer<?> ser) {
        Class<?> cls = ser.handledType();
        if (cls == null || cls == Object.class) {
            throw new IllegalArgumentException("JsonSerializer of type " + ser.getClass().getName() + " does not define valid handledType() -- must either register with method that takes type argument  or make serializer extend 'com.fasterxml.jackson.databind.ser.std.StdSerializer'");
        }
        this._addSerializer(cls, ser);
    }

    public <T> void addSerializer(Class<? extends T> type, JsonSerializer<T> ser) {
        this._addSerializer(type, ser);
    }

    public void addSerializers(List<JsonSerializer<?>> sers) {
        for (JsonSerializer<?> ser : sers) {
            this.addSerializer(ser);
        }
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> cls = type.getRawClass();
        ClassKey key = new ClassKey(cls);
        JsonSerializer<?> ser = null;
        if (cls.isInterface()) {
            if (this._interfaceMappings != null && (ser = this._interfaceMappings.get(key)) != null) {
                return ser;
            }
        } else if (this._classMappings != null) {
            ser = this._classMappings.get(key);
            if (ser != null) {
                return ser;
            }
            if (this._hasEnumSerializer && type.isEnumType()) {
                key.reset(Enum.class);
                ser = this._classMappings.get(key);
                if (ser != null) {
                    return ser;
                }
            }
            for (Class<?> curr = cls; curr != null; curr = curr.getSuperclass()) {
                key.reset(curr);
                ser = this._classMappings.get(key);
                if (ser == null) continue;
                return ser;
            }
        }
        if (this._interfaceMappings != null) {
            ser = this._findInterfaceMapping(cls, key);
            if (ser != null) {
                return ser;
            }
            if (!cls.isInterface()) {
                while ((cls = cls.getSuperclass()) != null) {
                    ser = this._findInterfaceMapping(cls, key);
                    if (ser == null) continue;
                    return ser;
                }
            }
        }
        return null;
    }

    @Override
    public JsonSerializer<?> findArraySerializer(SerializationConfig config, ArrayType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc);
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc);
    }

    @Override
    public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc);
    }

    @Override
    public JsonSerializer<?> findMapSerializer(SerializationConfig config, MapType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc);
    }

    @Override
    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config, MapLikeType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
        return this.findSerializer(config, type, beanDesc);
    }

    protected JsonSerializer<?> _findInterfaceMapping(Class<?> cls, ClassKey key) {
        for (Class<?> iface : cls.getInterfaces()) {
            key.reset(iface);
            JsonSerializer<?> ser = this._interfaceMappings.get(key);
            if (ser != null) {
                return ser;
            }
            ser = this._findInterfaceMapping(iface, key);
            if (ser == null) continue;
            return ser;
        }
        return null;
    }

    protected void _addSerializer(Class<?> cls, JsonSerializer<?> ser) {
        ClassKey key = new ClassKey(cls);
        if (cls.isInterface()) {
            if (this._interfaceMappings == null) {
                this._interfaceMappings = new HashMap();
            }
            this._interfaceMappings.put(key, ser);
        } else {
            if (this._classMappings == null) {
                this._classMappings = new HashMap();
            }
            this._classMappings.put(key, ser);
            if (cls == Enum.class) {
                this._hasEnumSerializer = true;
            }
        }
    }
}

