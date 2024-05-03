/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.LRUMap;
import java.io.Serializable;
import java.util.HashMap;

public final class DeserializerCache
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final LRUMap<JavaType, JsonDeserializer<Object>> _cachedDeserializers;
    protected final HashMap<JavaType, JsonDeserializer<Object>> _incompleteDeserializers = new HashMap(8);

    public DeserializerCache() {
        this(2000);
    }

    public DeserializerCache(int maxSize) {
        int initial = Math.min(64, maxSize >> 2);
        this._cachedDeserializers = new LRUMap(initial, maxSize);
    }

    Object writeReplace() {
        this._incompleteDeserializers.clear();
        return this;
    }

    public int cachedDeserializersCount() {
        return this._cachedDeserializers.size();
    }

    public void flushCachedDeserializers() {
        this._cachedDeserializers.clear();
    }

    public JsonDeserializer<Object> findValueDeserializer(DeserializationContext ctxt, DeserializerFactory factory, JavaType propertyType) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._findCachedDeserializer(propertyType);
        if (deser == null && (deser = this._createAndCacheValueDeserializer(ctxt, factory, propertyType)) == null) {
            deser = this._handleUnknownValueDeserializer(ctxt, propertyType);
        }
        return deser;
    }

    public KeyDeserializer findKeyDeserializer(DeserializationContext ctxt, DeserializerFactory factory, JavaType type) throws JsonMappingException {
        KeyDeserializer kd = factory.createKeyDeserializer(ctxt, type);
        if (kd == null) {
            return this._handleUnknownKeyDeserializer(ctxt, type);
        }
        if (kd instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer)((Object)kd)).resolve(ctxt);
        }
        return kd;
    }

    public boolean hasValueDeserializerFor(DeserializationContext ctxt, DeserializerFactory factory, JavaType type) throws JsonMappingException {
        JsonDeserializer<Object> deser = this._findCachedDeserializer(type);
        if (deser == null) {
            deser = this._createAndCacheValueDeserializer(ctxt, factory, type);
        }
        return deser != null;
    }

    protected JsonDeserializer<Object> _findCachedDeserializer(JavaType type) {
        if (type == null) {
            throw new IllegalArgumentException("Null JavaType passed");
        }
        if (this._hasCustomHandlers(type)) {
            return null;
        }
        return this._cachedDeserializers.get(type);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected JsonDeserializer<Object> _createAndCacheValueDeserializer(DeserializationContext ctxt, DeserializerFactory factory, JavaType type) throws JsonMappingException {
        HashMap<JavaType, JsonDeserializer<Object>> hashMap = this._incompleteDeserializers;
        synchronized (hashMap) {
            JsonDeserializer<Object> jsonDeserializer;
            block9: {
                JsonDeserializer<Object> deser = this._findCachedDeserializer(type);
                if (deser != null) {
                    return deser;
                }
                int count = this._incompleteDeserializers.size();
                if (count > 0 && (deser = this._incompleteDeserializers.get(type)) != null) {
                    return deser;
                }
                try {
                    jsonDeserializer = this._createAndCache2(ctxt, factory, type);
                    if (count != 0 || this._incompleteDeserializers.size() <= 0) break block9;
                    this._incompleteDeserializers.clear();
                } catch (Throwable throwable) {
                    if (count == 0 && this._incompleteDeserializers.size() > 0) {
                        this._incompleteDeserializers.clear();
                    }
                    throw throwable;
                }
            }
            return jsonDeserializer;
        }
    }

    protected JsonDeserializer<Object> _createAndCache2(DeserializationContext ctxt, DeserializerFactory factory, JavaType type) throws JsonMappingException {
        boolean addToCache;
        JsonDeserializer<Object> deser;
        try {
            deser = this._createDeserializer(ctxt, factory, type);
        } catch (IllegalArgumentException iae) {
            throw JsonMappingException.from(ctxt, ClassUtil.exceptionMessage(iae), (Throwable)iae);
        }
        if (deser == null) {
            return null;
        }
        boolean bl = addToCache = !this._hasCustomHandlers(type) && deser.isCachable();
        if (deser instanceof ResolvableDeserializer) {
            this._incompleteDeserializers.put(type, deser);
            ((ResolvableDeserializer)((Object)deser)).resolve(ctxt);
            this._incompleteDeserializers.remove(type);
        }
        if (addToCache) {
            this._cachedDeserializers.put(type, deser);
        }
        return deser;
    }

    protected JsonDeserializer<Object> _createDeserializer(DeserializationContext ctxt, DeserializerFactory factory, JavaType type) throws JsonMappingException {
        Class<?> builder;
        Object beanDesc;
        JsonDeserializer<Object> deser;
        DeserializationConfig config = ctxt.getConfig();
        if (type.isAbstract() || type.isMapLikeType() || type.isCollectionLikeType()) {
            type = factory.mapAbstractType(config, type);
        }
        if ((deser = this.findDeserializerFromAnnotation(ctxt, ((BeanDescription)(beanDesc = config.introspect(type))).getClassInfo())) != null) {
            return deser;
        }
        JavaType newType = this.modifyTypeByAnnotation(ctxt, ((BeanDescription)beanDesc).getClassInfo(), type);
        if (newType != type) {
            type = newType;
            beanDesc = config.introspect(newType);
        }
        if ((builder = ((BeanDescription)beanDesc).findPOJOBuilder()) != null) {
            return factory.createBuilderBasedDeserializer(ctxt, type, (BeanDescription)beanDesc, builder);
        }
        Converter<Object, Object> conv = ((BeanDescription)beanDesc).findDeserializationConverter();
        if (conv == null) {
            return this._createDeserializer2(ctxt, factory, type, (BeanDescription)beanDesc);
        }
        JavaType delegateType = conv.getInputType(ctxt.getTypeFactory());
        if (!delegateType.hasRawClass(type.getRawClass())) {
            beanDesc = config.introspect(delegateType);
        }
        return new StdDelegatingDeserializer<Object>(conv, delegateType, this._createDeserializer2(ctxt, factory, delegateType, (BeanDescription)beanDesc));
    }

    protected JsonDeserializer<?> _createDeserializer2(DeserializationContext ctxt, DeserializerFactory factory, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        DeserializationConfig config = ctxt.getConfig();
        if (type.isEnumType()) {
            return factory.createEnumDeserializer(ctxt, type, beanDesc);
        }
        if (type.isContainerType()) {
            JsonFormat.Value format;
            if (type.isArrayType()) {
                return factory.createArrayDeserializer(ctxt, (ArrayType)type, beanDesc);
            }
            if (type.isMapLikeType() && ((format = beanDesc.findExpectedFormat(null)) == null || format.getShape() != JsonFormat.Shape.OBJECT)) {
                MapLikeType mlt = (MapLikeType)type;
                if (mlt.isTrueMapType()) {
                    return factory.createMapDeserializer(ctxt, (MapType)mlt, beanDesc);
                }
                return factory.createMapLikeDeserializer(ctxt, mlt, beanDesc);
            }
            if (type.isCollectionLikeType() && ((format = beanDesc.findExpectedFormat(null)) == null || format.getShape() != JsonFormat.Shape.OBJECT)) {
                CollectionLikeType clt = (CollectionLikeType)type;
                if (clt.isTrueCollectionType()) {
                    return factory.createCollectionDeserializer(ctxt, (CollectionType)clt, beanDesc);
                }
                return factory.createCollectionLikeDeserializer(ctxt, clt, beanDesc);
            }
        }
        if (type.isReferenceType()) {
            return factory.createReferenceDeserializer(ctxt, (ReferenceType)type, beanDesc);
        }
        if (JsonNode.class.isAssignableFrom(type.getRawClass())) {
            return factory.createTreeDeserializer(config, type, beanDesc);
        }
        return factory.createBeanDeserializer(ctxt, type, beanDesc);
    }

    protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef = ctxt.getAnnotationIntrospector().findDeserializer(ann);
        if (deserDef == null) {
            return null;
        }
        JsonDeserializer<Object> deser = ctxt.deserializerInstance(ann, deserDef);
        return this.findConvertingDeserializer(ctxt, ann, deser);
    }

    protected JsonDeserializer<Object> findConvertingDeserializer(DeserializationContext ctxt, Annotated a, JsonDeserializer<Object> deser) throws JsonMappingException {
        Converter<Object, Object> conv = this.findConverter(ctxt, a);
        if (conv == null) {
            return deser;
        }
        JavaType delegateType = conv.getInputType(ctxt.getTypeFactory());
        return new StdDelegatingDeserializer<Object>(conv, delegateType, deser);
    }

    protected Converter<Object, Object> findConverter(DeserializationContext ctxt, Annotated a) throws JsonMappingException {
        Object convDef = ctxt.getAnnotationIntrospector().findDeserializationConverter(a);
        if (convDef == null) {
            return null;
        }
        return ctxt.converterInstance(a, convDef);
    }

    private JavaType modifyTypeByAnnotation(DeserializationContext ctxt, Annotated a, JavaType type) throws JsonMappingException {
        Object cdDef;
        JavaType contentType;
        KeyDeserializer kd;
        Object kdDef;
        JavaType keyType;
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr == null) {
            return type;
        }
        if (type.isMapLikeType() && (keyType = type.getKeyType()) != null && keyType.getValueHandler() == null && (kdDef = intr.findKeyDeserializer(a)) != null && (kd = ctxt.keyDeserializerInstance(a, kdDef)) != null) {
            type = ((MapLikeType)type).withKeyValueHandler(kd);
            keyType = type.getKeyType();
        }
        if ((contentType = type.getContentType()) != null && contentType.getValueHandler() == null && (cdDef = intr.findContentDeserializer(a)) != null) {
            JsonDeserializer<Object> cd = null;
            if (cdDef instanceof JsonDeserializer) {
                cdDef = (JsonDeserializer)cdDef;
            } else {
                Class<?> cdClass = this._verifyAsClass(cdDef, "findContentDeserializer", JsonDeserializer.None.class);
                if (cdClass != null) {
                    cd = ctxt.deserializerInstance(a, cdClass);
                }
            }
            if (cd != null) {
                type = type.withContentValueHandler(cd);
            }
        }
        type = intr.refineDeserializationType(ctxt.getConfig(), a, type);
        return type;
    }

    private boolean _hasCustomHandlers(JavaType t) {
        if (t.isContainerType()) {
            JavaType kt;
            JavaType ct = t.getContentType();
            if (ct != null && (ct.getValueHandler() != null || ct.getTypeHandler() != null)) {
                return true;
            }
            if (t.isMapLikeType() && (kt = t.getKeyType()).getValueHandler() != null) {
                return true;
            }
        }
        return false;
    }

    private Class<?> _verifyAsClass(Object src, String methodName, Class<?> noneClass) {
        if (src == null) {
            return null;
        }
        if (!(src instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector." + methodName + "() returned value of type " + src.getClass().getName() + ": expected type JsonSerializer or Class<JsonSerializer> instead");
        }
        Class cls = (Class)src;
        if (cls == noneClass || ClassUtil.isBogusClass(cls)) {
            return null;
        }
        return cls;
    }

    protected JsonDeserializer<Object> _handleUnknownValueDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        Class<?> rawClass = type.getRawClass();
        if (!ClassUtil.isConcrete(rawClass)) {
            return (JsonDeserializer)ctxt.reportBadDefinition(type, "Cannot find a Value deserializer for abstract type " + type);
        }
        return (JsonDeserializer)ctxt.reportBadDefinition(type, "Cannot find a Value deserializer for type " + type);
    }

    protected KeyDeserializer _handleUnknownKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        return (KeyDeserializer)ctxt.reportBadDefinition(type, "Cannot find a (Map) Key deserializer for type " + type);
    }
}

