/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;

public class AsExternalTypeDeserializer
extends AsArrayTypeDeserializer {
    private static final long serialVersionUID = 1L;

    public AsExternalTypeDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
        super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl);
    }

    public AsExternalTypeDeserializer(AsExternalTypeDeserializer src, BeanProperty property) {
        super(src, property);
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop) {
        if (prop == this._property) {
            return this;
        }
        return new AsExternalTypeDeserializer(this, prop);
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.EXTERNAL_PROPERTY;
    }

    @Override
    protected boolean _usesExternalId() {
        return true;
    }
}

