/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;

public final class TypeWrappedSerializer
extends JsonSerializer<Object>
implements ContextualSerializer {
    protected final TypeSerializer _typeSerializer;
    protected final JsonSerializer<Object> _serializer;

    public TypeWrappedSerializer(TypeSerializer typeSer, JsonSerializer<?> ser) {
        this._typeSerializer = typeSer;
        this._serializer = ser;
    }

    @Override
    public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
        this._serializer.serializeWithType(value, g, provider, this._typeSerializer);
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        this._serializer.serializeWithType(value, g, provider, typeSer);
    }

    @Override
    public Class<Object> handledType() {
        return Object.class;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = this._serializer;
        if (ser instanceof ContextualSerializer) {
            ser = provider.handleSecondaryContextualization(ser, property);
        }
        if (ser == this._serializer) {
            return this;
        }
        return new TypeWrappedSerializer(this._typeSerializer, ser);
    }

    public JsonSerializer<Object> valueSerializer() {
        return this._serializer;
    }

    public TypeSerializer typeSerializer() {
        return this._typeSerializer;
    }
}

