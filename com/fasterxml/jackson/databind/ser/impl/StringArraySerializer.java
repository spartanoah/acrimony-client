/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.ArraySerializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.lang.reflect.Type;

@JacksonStdImpl
public class StringArraySerializer
extends ArraySerializerBase<String[]>
implements ContextualSerializer {
    private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(String.class);
    public static final StringArraySerializer instance = new StringArraySerializer();
    protected final JsonSerializer<Object> _elementSerializer;

    protected StringArraySerializer() {
        super(String[].class);
        this._elementSerializer = null;
    }

    public StringArraySerializer(StringArraySerializer src, BeanProperty prop, JsonSerializer<?> ser, Boolean unwrapSingle) {
        super(src, prop, unwrapSingle);
        this._elementSerializer = ser;
    }

    @Override
    public JsonSerializer<?> _withResolved(BeanProperty prop, Boolean unwrapSingle) {
        return new StringArraySerializer(this, prop, this._elementSerializer, unwrapSingle);
    }

    @Override
    public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
        return this;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> ser = null;
        if (property != null) {
            Object serDef;
            AnnotationIntrospector ai = provider.getAnnotationIntrospector();
            AnnotatedMember m = property.getMember();
            if (m != null && (serDef = ai.findContentSerializer(m)) != null) {
                ser = provider.serializerInstance(m, serDef);
            }
        }
        Boolean unwrapSingle = this.findFormatFeature(provider, property, String[].class, JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        if (ser == null) {
            ser = this._elementSerializer;
        }
        if ((ser = this.findContextualConvertingSerializer(provider, property, ser)) == null) {
            ser = provider.findContentValueSerializer(String.class, property);
        }
        if (this.isDefaultSerializer(ser)) {
            ser = null;
        }
        if (ser == this._elementSerializer && unwrapSingle == this._unwrapSingle) {
            return this;
        }
        return new StringArraySerializer(this, property, ser, unwrapSingle);
    }

    @Override
    public JavaType getContentType() {
        return VALUE_TYPE;
    }

    @Override
    public JsonSerializer<?> getContentSerializer() {
        return this._elementSerializer;
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, String[] value) {
        return value.length == 0;
    }

    @Override
    public boolean hasSingleElement(String[] value) {
        return value.length == 1;
    }

    @Override
    public final void serialize(String[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int len = value.length;
        if (len == 1 && (this._unwrapSingle == null && provider.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED) || this._unwrapSingle == Boolean.TRUE)) {
            this.serializeContents(value, gen, provider);
            return;
        }
        gen.writeStartArray(value, len);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    @Override
    public void serializeContents(String[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int len = value.length;
        if (len == 0) {
            return;
        }
        if (this._elementSerializer != null) {
            this.serializeContentsSlow(value, gen, provider, this._elementSerializer);
            return;
        }
        for (int i = 0; i < len; ++i) {
            String str = value[i];
            if (str == null) {
                gen.writeNull();
                continue;
            }
            gen.writeString(value[i]);
        }
    }

    private void serializeContentsSlow(String[] value, JsonGenerator gen, SerializerProvider provider, JsonSerializer<Object> ser) throws IOException {
        int len = value.length;
        for (int i = 0; i < len; ++i) {
            String str = value[i];
            if (str == null) {
                provider.defaultSerializeNull(gen);
                continue;
            }
            ser.serialize(value[i], gen, provider);
        }
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return this.createSchemaNode("array", true).set("items", this.createSchemaNode("string"));
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        this.visitArrayFormat(visitor, typeHint, JsonFormatTypes.STRING);
    }
}

