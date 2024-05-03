/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.lang.reflect.Type;

@JacksonStdImpl
public class TokenBufferSerializer
extends StdSerializer<TokenBuffer> {
    public TokenBufferSerializer() {
        super(TokenBuffer.class);
    }

    @Override
    public void serialize(TokenBuffer value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        value.serialize(jgen);
    }

    @Override
    public final void serializeWithType(TokenBuffer value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
        this.serialize(value, g, provider);
        typeSer.writeTypeSuffix(g, typeIdDef);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return this.createSchemaNode("any", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        visitor.expectAnyFormat(typeHint);
    }
}

