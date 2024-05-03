/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

@JacksonStdImpl
public class SerializableSerializer
extends StdSerializer<JsonSerializable> {
    public static final SerializableSerializer instance = new SerializableSerializer();

    protected SerializableSerializer() {
        super(JsonSerializable.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider serializers, JsonSerializable value) {
        if (value instanceof JsonSerializable.Base) {
            return ((JsonSerializable.Base)value).isEmpty(serializers);
        }
        return false;
    }

    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        value.serialize(gen, serializers);
    }

    @Override
    public final void serializeWithType(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        value.serializeWithType(gen, serializers, typeSer);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        visitor.expectAnyFormat(typeHint);
    }
}

