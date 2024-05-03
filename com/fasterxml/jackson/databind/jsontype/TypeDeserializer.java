/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import java.io.IOException;

public abstract class TypeDeserializer {
    public abstract TypeDeserializer forProperty(BeanProperty var1);

    public abstract JsonTypeInfo.As getTypeInclusion();

    public abstract String getPropertyName();

    public abstract TypeIdResolver getTypeIdResolver();

    public abstract Class<?> getDefaultImpl();

    public abstract Object deserializeTypedFromObject(JsonParser var1, DeserializationContext var2) throws IOException;

    public abstract Object deserializeTypedFromArray(JsonParser var1, DeserializationContext var2) throws IOException;

    public abstract Object deserializeTypedFromScalar(JsonParser var1, DeserializationContext var2) throws IOException;

    public abstract Object deserializeTypedFromAny(JsonParser var1, DeserializationContext var2) throws IOException;

    public static Object deserializeIfNatural(JsonParser p, DeserializationContext ctxt, JavaType baseType) throws IOException {
        return TypeDeserializer.deserializeIfNatural(p, ctxt, baseType.getRawClass());
    }

    public static Object deserializeIfNatural(JsonParser p, DeserializationContext ctxt, Class<?> base) throws IOException {
        JsonToken t = p.currentToken();
        if (t == null) {
            return null;
        }
        switch (t) {
            case VALUE_STRING: {
                if (!base.isAssignableFrom(String.class)) break;
                return p.getText();
            }
            case VALUE_NUMBER_INT: {
                if (!base.isAssignableFrom(Integer.class)) break;
                return p.getIntValue();
            }
            case VALUE_NUMBER_FLOAT: {
                if (!base.isAssignableFrom(Double.class)) break;
                return p.getDoubleValue();
            }
            case VALUE_TRUE: {
                if (!base.isAssignableFrom(Boolean.class)) break;
                return Boolean.TRUE;
            }
            case VALUE_FALSE: {
                if (!base.isAssignableFrom(Boolean.class)) break;
                return Boolean.FALSE;
            }
        }
        return null;
    }
}

