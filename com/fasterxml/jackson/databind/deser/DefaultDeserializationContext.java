/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class DefaultDeserializationContext
extends DeserializationContext
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected transient LinkedHashMap<ObjectIdGenerator.IdKey, ReadableObjectId> _objectIds;
    private List<ObjectIdResolver> _objectIdResolvers;

    protected DefaultDeserializationContext(DeserializerFactory df, DeserializerCache cache) {
        super(df, cache);
    }

    protected DefaultDeserializationContext(DefaultDeserializationContext src, DeserializationConfig config, JsonParser jp, InjectableValues values) {
        super(src, config, jp, values);
    }

    protected DefaultDeserializationContext(DefaultDeserializationContext src, DeserializerFactory factory) {
        super(src, factory);
    }

    protected DefaultDeserializationContext(DefaultDeserializationContext src) {
        super(src);
    }

    public DefaultDeserializationContext copy() {
        throw new IllegalStateException("DefaultDeserializationContext sub-class not overriding copy()");
    }

    @Override
    public ReadableObjectId findObjectId(Object id, ObjectIdGenerator<?> gen, ObjectIdResolver resolverType) {
        if (id == null) {
            return null;
        }
        ObjectIdGenerator.IdKey key = gen.key(id);
        if (this._objectIds == null) {
            this._objectIds = new LinkedHashMap();
        } else {
            ReadableObjectId entry = this._objectIds.get(key);
            if (entry != null) {
                return entry;
            }
        }
        ObjectIdResolver resolver = null;
        if (this._objectIdResolvers == null) {
            this._objectIdResolvers = new ArrayList<ObjectIdResolver>(8);
        } else {
            for (ObjectIdResolver res : this._objectIdResolvers) {
                if (!res.canUseFor(resolverType)) continue;
                resolver = res;
                break;
            }
        }
        if (resolver == null) {
            resolver = resolverType.newForDeserialization(this);
            this._objectIdResolvers.add(resolver);
        }
        ReadableObjectId entry = this.createReadableObjectId(key);
        entry.setResolver(resolver);
        this._objectIds.put(key, entry);
        return entry;
    }

    protected ReadableObjectId createReadableObjectId(ObjectIdGenerator.IdKey key) {
        return new ReadableObjectId(key);
    }

    @Override
    public void checkUnresolvedObjectId() throws UnresolvedForwardReference {
        if (this._objectIds == null) {
            return;
        }
        if (!this.isEnabled(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS)) {
            return;
        }
        UnresolvedForwardReference exception = null;
        for (Map.Entry<ObjectIdGenerator.IdKey, ReadableObjectId> entry : this._objectIds.entrySet()) {
            ReadableObjectId roid = entry.getValue();
            if (!roid.hasReferringProperties() || this.tryToResolveUnresolvedObjectId(roid)) continue;
            if (exception == null) {
                exception = new UnresolvedForwardReference(this.getParser(), "Unresolved forward references for: ");
            }
            Object key = roid.getKey().key;
            Iterator<ReadableObjectId.Referring> iterator = roid.referringProperties();
            while (iterator.hasNext()) {
                ReadableObjectId.Referring referring = iterator.next();
                exception.addUnresolvedId(key, referring.getBeanType(), referring.getLocation());
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    protected boolean tryToResolveUnresolvedObjectId(ReadableObjectId roid) {
        return roid.tryToResolveUnresolved(this);
    }

    @Override
    public JsonDeserializer<Object> deserializerInstance(Annotated ann, Object deserDef) throws JsonMappingException {
        JsonDeserializer deser;
        if (deserDef == null) {
            return null;
        }
        if (deserDef instanceof JsonDeserializer) {
            deser = (JsonDeserializer)deserDef;
        } else {
            if (!(deserDef instanceof Class)) {
                throw new IllegalStateException("AnnotationIntrospector returned deserializer definition of type " + deserDef.getClass().getName() + "; expected type JsonDeserializer or Class<JsonDeserializer> instead");
            }
            Class deserClass = (Class)deserDef;
            if (deserClass == JsonDeserializer.None.class || ClassUtil.isBogusClass(deserClass)) {
                return null;
            }
            if (!JsonDeserializer.class.isAssignableFrom(deserClass)) {
                throw new IllegalStateException("AnnotationIntrospector returned Class " + deserClass.getName() + "; expected Class<JsonDeserializer>");
            }
            HandlerInstantiator hi = this._config.getHandlerInstantiator();
            JsonDeserializer jsonDeserializer = deser = hi == null ? null : hi.deserializerInstance(this._config, ann, deserClass);
            if (deser == null) {
                deser = (JsonDeserializer)ClassUtil.createInstance(deserClass, this._config.canOverrideAccessModifiers());
            }
        }
        if (deser instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer)((Object)deser)).resolve(this);
        }
        return deser;
    }

    @Override
    public final KeyDeserializer keyDeserializerInstance(Annotated ann, Object deserDef) throws JsonMappingException {
        KeyDeserializer deser;
        if (deserDef == null) {
            return null;
        }
        if (deserDef instanceof KeyDeserializer) {
            deser = (KeyDeserializer)deserDef;
        } else {
            if (!(deserDef instanceof Class)) {
                throw new IllegalStateException("AnnotationIntrospector returned key deserializer definition of type " + deserDef.getClass().getName() + "; expected type KeyDeserializer or Class<KeyDeserializer> instead");
            }
            Class deserClass = (Class)deserDef;
            if (deserClass == KeyDeserializer.None.class || ClassUtil.isBogusClass(deserClass)) {
                return null;
            }
            if (!KeyDeserializer.class.isAssignableFrom(deserClass)) {
                throw new IllegalStateException("AnnotationIntrospector returned Class " + deserClass.getName() + "; expected Class<KeyDeserializer>");
            }
            HandlerInstantiator hi = this._config.getHandlerInstantiator();
            KeyDeserializer keyDeserializer = deser = hi == null ? null : hi.keyDeserializerInstance(this._config, ann, deserClass);
            if (deser == null) {
                deser = (KeyDeserializer)ClassUtil.createInstance(deserClass, this._config.canOverrideAccessModifiers());
            }
        }
        if (deser instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer)((Object)deser)).resolve(this);
        }
        return deser;
    }

    public abstract DefaultDeserializationContext with(DeserializerFactory var1);

    public abstract DefaultDeserializationContext createInstance(DeserializationConfig var1, JsonParser var2, InjectableValues var3);

    public static final class Impl
    extends DefaultDeserializationContext {
        private static final long serialVersionUID = 1L;

        public Impl(DeserializerFactory df) {
            super(df, null);
        }

        protected Impl(Impl src, DeserializationConfig config, JsonParser jp, InjectableValues values) {
            super(src, config, jp, values);
        }

        protected Impl(Impl src) {
            super(src);
        }

        protected Impl(Impl src, DeserializerFactory factory) {
            super(src, factory);
        }

        @Override
        public DefaultDeserializationContext copy() {
            ClassUtil.verifyMustOverride(Impl.class, this, "copy");
            return new Impl(this);
        }

        @Override
        public DefaultDeserializationContext createInstance(DeserializationConfig config, JsonParser p, InjectableValues values) {
            return new Impl(this, config, p, values);
        }

        @Override
        public DefaultDeserializationContext with(DeserializerFactory factory) {
            return new Impl(this, factory);
        }
    }
}

