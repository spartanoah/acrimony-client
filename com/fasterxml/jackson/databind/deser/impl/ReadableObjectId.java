/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class ReadableObjectId {
    protected Object _item;
    protected final ObjectIdGenerator.IdKey _key;
    protected LinkedList<Referring> _referringProperties;
    protected ObjectIdResolver _resolver;

    public ReadableObjectId(ObjectIdGenerator.IdKey key) {
        this._key = key;
    }

    public void setResolver(ObjectIdResolver resolver) {
        this._resolver = resolver;
    }

    public ObjectIdGenerator.IdKey getKey() {
        return this._key;
    }

    public void appendReferring(Referring currentReferring) {
        if (this._referringProperties == null) {
            this._referringProperties = new LinkedList();
        }
        this._referringProperties.add(currentReferring);
    }

    public void bindItem(Object ob) throws IOException {
        this._resolver.bindItem(this._key, ob);
        this._item = ob;
        Object id = this._key.key;
        if (this._referringProperties != null) {
            Iterator it = this._referringProperties.iterator();
            this._referringProperties = null;
            while (it.hasNext()) {
                ((Referring)it.next()).handleResolvedForwardReference(id, ob);
            }
        }
    }

    public Object resolve() {
        this._item = this._resolver.resolveId(this._key);
        return this._item;
    }

    public boolean hasReferringProperties() {
        return this._referringProperties != null && !this._referringProperties.isEmpty();
    }

    public Iterator<Referring> referringProperties() {
        if (this._referringProperties == null) {
            return Collections.emptyList().iterator();
        }
        return this._referringProperties.iterator();
    }

    public boolean tryToResolveUnresolved(DeserializationContext ctxt) {
        return false;
    }

    public ObjectIdResolver getResolver() {
        return this._resolver;
    }

    public String toString() {
        return String.valueOf(this._key);
    }

    public static abstract class Referring {
        private final UnresolvedForwardReference _reference;
        private final Class<?> _beanType;

        public Referring(UnresolvedForwardReference ref, Class<?> beanType) {
            this._reference = ref;
            this._beanType = beanType;
        }

        public Referring(UnresolvedForwardReference ref, JavaType beanType) {
            this._reference = ref;
            this._beanType = beanType.getRawClass();
        }

        public JsonLocation getLocation() {
            return this._reference.getLocation();
        }

        public Class<?> getBeanType() {
            return this._beanType;
        }

        public abstract void handleResolvedForwardReference(Object var1, Object var2) throws IOException;

        public boolean hasId(Object id) {
            return id.equals(this._reference.getUnresolvedId());
        }
    }
}

