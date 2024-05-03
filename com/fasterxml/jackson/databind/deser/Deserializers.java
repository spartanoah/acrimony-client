/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;

public interface Deserializers {
    public JsonDeserializer<?> findEnumDeserializer(Class<?> var1, DeserializationConfig var2, BeanDescription var3) throws JsonMappingException;

    public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> var1, DeserializationConfig var2, BeanDescription var3) throws JsonMappingException;

    public JsonDeserializer<?> findBeanDeserializer(JavaType var1, DeserializationConfig var2, BeanDescription var3) throws JsonMappingException;

    public JsonDeserializer<?> findReferenceDeserializer(ReferenceType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5) throws JsonMappingException;

    public JsonDeserializer<?> findArrayDeserializer(ArrayType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5) throws JsonMappingException;

    public JsonDeserializer<?> findCollectionDeserializer(CollectionType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5) throws JsonMappingException;

    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5) throws JsonMappingException;

    public JsonDeserializer<?> findMapDeserializer(MapType var1, DeserializationConfig var2, BeanDescription var3, KeyDeserializer var4, TypeDeserializer var5, JsonDeserializer<?> var6) throws JsonMappingException;

    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType var1, DeserializationConfig var2, BeanDescription var3, KeyDeserializer var4, TypeDeserializer var5, JsonDeserializer<?> var6) throws JsonMappingException;

    public static abstract class Base
    implements Deserializers {
        @Override
        public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findArrayDeserializer(ArrayType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
            return null;
        }

        public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
            return false;
        }
    }
}

