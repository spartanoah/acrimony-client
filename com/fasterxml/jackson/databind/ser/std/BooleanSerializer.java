/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.lang.reflect.Type;

@JacksonStdImpl
public final class BooleanSerializer
extends StdScalarSerializer<Object>
implements ContextualSerializer {
    private static final long serialVersionUID = 1L;
    protected final boolean _forPrimitive;

    public BooleanSerializer(boolean forPrimitive) {
        super(forPrimitive ? Boolean.TYPE : Boolean.class, false);
        this._forPrimitive = forPrimitive;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
        JsonFormat.Shape shape;
        JsonFormat.Value format = this.findFormatOverrides(serializers, property, Boolean.class);
        if (format != null && (shape = format.getShape()).isNumeric()) {
            return new AsNumber(this._forPrimitive);
        }
        return this;
    }

    @Override
    public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeBoolean(Boolean.TRUE.equals(value));
    }

    @Override
    public final void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        g.writeBoolean(Boolean.TRUE.equals(value));
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return this.createSchemaNode("boolean", !this._forPrimitive);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        visitor.expectBooleanFormat(typeHint);
    }

    static final class AsNumber
    extends StdScalarSerializer<Object>
    implements ContextualSerializer {
        private static final long serialVersionUID = 1L;
        protected final boolean _forPrimitive;

        public AsNumber(boolean forPrimitive) {
            super(forPrimitive ? Boolean.TYPE : Boolean.class, false);
            this._forPrimitive = forPrimitive;
        }

        @Override
        public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
            g.writeNumber(Boolean.FALSE.equals(value) ? 0 : 1);
        }

        @Override
        public final void serializeWithType(Object value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
            g.writeBoolean(Boolean.TRUE.equals(value));
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
            this.visitIntFormat(visitor, typeHint, JsonParser.NumberType.INT);
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
            JsonFormat.Shape shape;
            JsonFormat.Value format = this.findFormatOverrides(serializers, property, Boolean.class);
            if (format != null && !(shape = format.getShape()).isNumeric()) {
                return new BooleanSerializer(this._forPrimitive);
            }
            return this;
        }
    }
}

