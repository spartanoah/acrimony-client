/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import java.util.List;

public abstract class BeanSerializerModifier {
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        return beanProperties;
    }

    public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        return beanProperties;
    }

    public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
        return builder;
    }

    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyArraySerializer(SerializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config, CollectionType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyCollectionLikeSerializer(SerializationConfig config, CollectionLikeType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyMapLikeSerializer(SerializationConfig config, MapLikeType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyEnumSerializer(SerializationConfig config, JavaType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }

    public JsonSerializer<?> modifyKeySerializer(SerializationConfig config, JavaType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return serializer;
    }
}

