/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Iterator;

public abstract class JsonSerializer<T>
implements JsonFormatVisitable {
    public JsonSerializer<T> unwrappingSerializer(NameTransformer unwrapper) {
        return this;
    }

    public JsonSerializer<T> replaceDelegatee(JsonSerializer<?> delegatee) {
        throw new UnsupportedOperationException();
    }

    public JsonSerializer<?> withFilterId(Object filterId) {
        return this;
    }

    public abstract void serialize(T var1, JsonGenerator var2, SerializerProvider var3) throws IOException;

    public void serializeWithType(T value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        Class<Object> clz = this.handledType();
        if (clz == null) {
            clz = value.getClass();
        }
        serializers.reportBadDefinition(clz, String.format("Type id handling not implemented for type %s (by serializer of type %s)", clz.getName(), this.getClass().getName()));
    }

    public Class<T> handledType() {
        return null;
    }

    @Deprecated
    public boolean isEmpty(T value) {
        return this.isEmpty(null, value);
    }

    public boolean isEmpty(SerializerProvider provider, T value) {
        return value == null;
    }

    public boolean usesObjectId() {
        return false;
    }

    public boolean isUnwrappingSerializer() {
        return false;
    }

    public JsonSerializer<?> getDelegatee() {
        return null;
    }

    public Iterator<PropertyWriter> properties() {
        return ClassUtil.emptyIterator();
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType type) throws JsonMappingException {
        visitor.expectAnyFormat(type);
    }

    public static abstract class None
    extends JsonSerializer<Object> {
    }
}

