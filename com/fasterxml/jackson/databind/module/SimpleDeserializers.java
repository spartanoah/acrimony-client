/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.module;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimpleDeserializers
extends Deserializers.Base
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected HashMap<ClassKey, JsonDeserializer<?>> _classMappings = null;
    protected boolean _hasEnumDeserializer = false;

    public SimpleDeserializers() {
    }

    public SimpleDeserializers(Map<Class<?>, JsonDeserializer<?>> desers) {
        this.addDeserializers(desers);
    }

    public <T> void addDeserializer(Class<T> forClass, JsonDeserializer<? extends T> deser) {
        ClassKey key = new ClassKey(forClass);
        if (this._classMappings == null) {
            this._classMappings = new HashMap();
        }
        this._classMappings.put(key, deser);
        if (forClass == Enum.class) {
            this._hasEnumDeserializer = true;
        }
    }

    public void addDeserializers(Map<Class<?>, JsonDeserializer<?>> desers) {
        for (Map.Entry<Class<?>, JsonDeserializer<?>> entry : desers.entrySet()) {
            Class<?> cls = entry.getKey();
            JsonDeserializer<?> deser = entry.getValue();
            this.addDeserializer(cls, deser);
        }
    }

    @Override
    public JsonDeserializer<?> findArrayDeserializer(ArrayType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return this._find(type);
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        return this._find(type);
    }

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return this._find(type);
    }

    @Override
    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return this._find(type);
    }

    @Override
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        if (this._classMappings == null) {
            return null;
        }
        JsonDeserializer<?> deser = this._classMappings.get(new ClassKey(type));
        if (deser == null && this._hasEnumDeserializer && type.isEnum()) {
            deser = this._classMappings.get(new ClassKey(Enum.class));
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        if (this._classMappings == null) {
            return null;
        }
        return this._classMappings.get(new ClassKey(nodeType));
    }

    @Override
    public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
        return this._find(refType);
    }

    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return this._find(type);
    }

    @Override
    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        return this._find(type);
    }

    @Override
    public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
        return this._classMappings != null && this._classMappings.containsKey(new ClassKey(valueType));
    }

    private final JsonDeserializer<?> _find(JavaType type) {
        if (this._classMappings == null) {
            return null;
        }
        return this._classMappings.get(new ClassKey(type.getRawClass()));
    }
}

