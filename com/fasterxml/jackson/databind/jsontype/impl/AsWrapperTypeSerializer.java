/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeSerializerBase;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;

public class AsWrapperTypeSerializer
extends TypeSerializerBase {
    public AsWrapperTypeSerializer(TypeIdResolver idRes, BeanProperty property) {
        super(idRes, property);
    }

    @Override
    public AsWrapperTypeSerializer forProperty(BeanProperty prop) {
        return this._property == prop ? this : new AsWrapperTypeSerializer(this._idResolver, prop);
    }

    @Override
    public JsonTypeInfo.As getTypeInclusion() {
        return JsonTypeInfo.As.WRAPPER_OBJECT;
    }

    protected String _validTypeId(String typeId) {
        return ClassUtil.nonNullString(typeId);
    }

    protected final void _writeTypeId(JsonGenerator g, String typeId) throws IOException {
        if (typeId != null) {
            g.writeTypeId(typeId);
        }
    }
}

