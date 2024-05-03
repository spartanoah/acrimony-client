/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import java.io.IOException;
import java.lang.annotation.Annotation;

public final class ObjectIdValueProperty
extends SettableBeanProperty {
    private static final long serialVersionUID = 1L;
    protected final ObjectIdReader _objectIdReader;

    public ObjectIdValueProperty(ObjectIdReader objectIdReader, PropertyMetadata metadata) {
        super(objectIdReader.propertyName, objectIdReader.getIdType(), metadata, objectIdReader.getDeserializer());
        this._objectIdReader = objectIdReader;
    }

    protected ObjectIdValueProperty(ObjectIdValueProperty src, JsonDeserializer<?> deser, NullValueProvider nva) {
        super(src, deser, nva);
        this._objectIdReader = src._objectIdReader;
    }

    protected ObjectIdValueProperty(ObjectIdValueProperty src, PropertyName newName) {
        super(src, newName);
        this._objectIdReader = src._objectIdReader;
    }

    @Override
    public SettableBeanProperty withName(PropertyName newName) {
        return new ObjectIdValueProperty(this, newName);
    }

    @Override
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (this._valueDeserializer == deser) {
            return this;
        }
        NullValueProvider nvp = this._valueDeserializer == this._nullProvider ? deser : this._nullProvider;
        return new ObjectIdValueProperty(this, deser, nvp);
    }

    @Override
    public SettableBeanProperty withNullProvider(NullValueProvider nva) {
        return new ObjectIdValueProperty(this, this._valueDeserializer, nva);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> acls) {
        return null;
    }

    @Override
    public AnnotatedMember getMember() {
        return null;
    }

    @Override
    public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
        this.deserializeSetAndReturn(p, ctxt, instance);
    }

    @Override
    public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        Object id = this._valueDeserializer.deserialize(p, ctxt);
        ReadableObjectId roid = ctxt.findObjectId(id, this._objectIdReader.generator, this._objectIdReader.resolver);
        roid.bindItem(instance);
        SettableBeanProperty idProp = this._objectIdReader.idProperty;
        if (idProp != null) {
            return idProp.setAndReturn(instance, id);
        }
        return instance;
    }

    @Override
    public void set(Object instance, Object value) throws IOException {
        this.setAndReturn(instance, value);
    }

    @Override
    public Object setAndReturn(Object instance, Object value) throws IOException {
        SettableBeanProperty idProp = this._objectIdReader.idProperty;
        if (idProp == null) {
            throw new UnsupportedOperationException("Should not call set() on ObjectIdProperty that has no SettableBeanProperty");
        }
        return idProp.setAndReturn(instance, value);
    }
}

