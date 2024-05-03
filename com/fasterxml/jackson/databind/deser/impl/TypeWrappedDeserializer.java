/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

public final class TypeWrappedDeserializer
extends JsonDeserializer<Object>
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final TypeDeserializer _typeDeserializer;
    protected final JsonDeserializer<Object> _deserializer;

    public TypeWrappedDeserializer(TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
        this._typeDeserializer = typeDeser;
        this._deserializer = deser;
    }

    @Override
    public Class<?> handledType() {
        return this._deserializer.handledType();
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return this._deserializer.supportsUpdate(config);
    }

    @Override
    public JsonDeserializer<?> getDelegatee() {
        return this._deserializer.getDelegatee();
    }

    @Override
    public Collection<Object> getKnownPropertyNames() {
        return this._deserializer.getKnownPropertyNames();
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return this._deserializer.getNullValue(ctxt);
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return this._deserializer.getEmptyValue(ctxt);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this._deserializer.deserializeWithType(p, ctxt, this._typeDeserializer);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        throw new IllegalStateException("Type-wrapped deserializer's deserializeWithType should never get called");
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        return this._deserializer.deserialize(p, ctxt, intoValue);
    }
}

