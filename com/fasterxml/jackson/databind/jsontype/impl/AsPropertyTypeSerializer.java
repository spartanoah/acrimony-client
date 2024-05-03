/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeSerializer;

public class AsPropertyTypeSerializer
extends AsArrayTypeSerializer {
    protected final String _typePropertyName;

    public AsPropertyTypeSerializer(TypeIdResolver idRes, BeanProperty property, String propName) {
        super(idRes, property);
        this._typePropertyName = propName;
    }

    @Override
    public AsPropertyTypeSerializer forProperty(BeanProperty prop) {
        return this._property == prop ? this : new AsPropertyTypeSerializer(this._idResolver, prop, this._typePropertyName);
    }

    @Override
    public String getPropertyName() {
        return this._typePropertyName;
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.PROPERTY;
    }
}

