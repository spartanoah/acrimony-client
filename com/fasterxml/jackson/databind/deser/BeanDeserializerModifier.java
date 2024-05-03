/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import java.util.List;

public abstract class BeanDeserializerModifier {
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
        return propDefs;
    }

    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
        return builder;
    }

    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyReferenceDeserializer(DeserializationConfig config, ReferenceType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config, CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyCollectionLikeDeserializer(DeserializationConfig config, CollectionLikeType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public JsonDeserializer<?> modifyMapLikeDeserializer(DeserializationConfig config, MapLikeType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        return deserializer;
    }

    public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config, JavaType type, KeyDeserializer deserializer) {
        return deserializer;
    }
}

