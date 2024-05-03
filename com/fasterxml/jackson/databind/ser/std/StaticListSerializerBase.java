/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

public abstract class StaticListSerializerBase<T extends Collection<?>>
extends StdSerializer<T>
implements ContextualSerializer {
    protected final Boolean _unwrapSingle;

    protected StaticListSerializerBase(Class<?> cls) {
        super(cls, false);
        this._unwrapSingle = null;
    }

    protected StaticListSerializerBase(StaticListSerializerBase<?> src, Boolean unwrapSingle) {
        super(src);
        this._unwrapSingle = unwrapSingle;
    }

    public abstract JsonSerializer<?> _withResolved(BeanProperty var1, Boolean var2);

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializers, BeanProperty property) throws JsonMappingException {
        JsonFormat.Value format;
        JsonSerializer<Object> ser = null;
        Boolean unwrapSingle = null;
        if (property != null) {
            Object serDef;
            AnnotationIntrospector intr = serializers.getAnnotationIntrospector();
            AnnotatedMember m = property.getMember();
            if (m != null && (serDef = intr.findContentSerializer(m)) != null) {
                ser = serializers.serializerInstance(m, serDef);
            }
        }
        if ((format = this.findFormatOverrides(serializers, property, this.handledType())) != null) {
            unwrapSingle = format.getFeature(JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
        }
        if ((ser = this.findContextualConvertingSerializer(serializers, property, ser)) == null) {
            ser = serializers.findContentValueSerializer(String.class, property);
        }
        if (this.isDefaultSerializer(ser)) {
            if (unwrapSingle == this._unwrapSingle) {
                return this;
            }
            return this._withResolved(property, unwrapSingle);
        }
        return new CollectionSerializer(serializers.constructType((Type)((Object)String.class)), true, null, ser);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, T value) {
        return value == null || value.size() == 0;
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return this.createSchemaNode("array", true).set("items", this.contentSchema());
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
        if (v2 != null) {
            this.acceptContentVisitor(v2);
        }
    }

    protected abstract JsonNode contentSchema();

    protected abstract void acceptContentVisitor(JsonArrayFormatVisitor var1) throws JsonMappingException;

    @Override
    public abstract void serializeWithType(T var1, JsonGenerator var2, SerializerProvider var3, TypeSerializer var4) throws IOException;
}

