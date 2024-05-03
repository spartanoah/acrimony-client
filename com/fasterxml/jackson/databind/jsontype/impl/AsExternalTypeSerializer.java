/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeSerializerBase;
import java.io.IOException;

public class AsExternalTypeSerializer
extends TypeSerializerBase {
    protected final String _typePropertyName;

    public AsExternalTypeSerializer(TypeIdResolver idRes, BeanProperty property, String propName) {
        super(idRes, property);
        this._typePropertyName = propName;
    }

    @Override
    public AsExternalTypeSerializer forProperty(BeanProperty prop) {
        return this._property == prop ? this : new AsExternalTypeSerializer(this._idResolver, prop, this._typePropertyName);
    }

    @Override
    public String getPropertyName() {
        return this._typePropertyName;
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.EXTERNAL_PROPERTY;
    }

    protected final void _writeScalarPrefix(Object value, JsonGenerator g) throws IOException {
    }

    protected final void _writeObjectPrefix(Object value, JsonGenerator g) throws IOException {
        g.writeStartObject();
    }

    protected final void _writeArrayPrefix(Object value, JsonGenerator g) throws IOException {
        g.writeStartArray();
    }

    protected final void _writeScalarSuffix(Object value, JsonGenerator g, String typeId) throws IOException {
        if (typeId != null) {
            g.writeStringField(this._typePropertyName, typeId);
        }
    }

    protected final void _writeObjectSuffix(Object value, JsonGenerator g, String typeId) throws IOException {
        g.writeEndObject();
        if (typeId != null) {
            g.writeStringField(this._typePropertyName, typeId);
        }
    }

    protected final void _writeArraySuffix(Object value, JsonGenerator g, String typeId) throws IOException {
        g.writeEndArray();
        if (typeId != null) {
            g.writeStringField(this._typePropertyName, typeId);
        }
    }
}

