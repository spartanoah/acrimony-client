/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import java.io.IOException;
import java.lang.reflect.Type;

public class StdDelegatingSerializer
extends StdSerializer<Object>
implements ContextualSerializer,
ResolvableSerializer,
JsonFormatVisitable,
SchemaAware {
    protected final Converter<Object, ?> _converter;
    protected final JavaType _delegateType;
    protected final JsonSerializer<Object> _delegateSerializer;

    public StdDelegatingSerializer(Converter<?, ?> converter) {
        super(Object.class);
        this._converter = converter;
        this._delegateType = null;
        this._delegateSerializer = null;
    }

    public <T> StdDelegatingSerializer(Class<T> cls, Converter<T, ?> converter) {
        super(cls, false);
        this._converter = converter;
        this._delegateType = null;
        this._delegateSerializer = null;
    }

    public StdDelegatingSerializer(Converter<Object, ?> converter, JavaType delegateType, JsonSerializer<?> delegateSerializer) {
        super(delegateType);
        this._converter = converter;
        this._delegateType = delegateType;
        this._delegateSerializer = delegateSerializer;
    }

    protected StdDelegatingSerializer withDelegate(Converter<Object, ?> converter, JavaType delegateType, JsonSerializer<?> delegateSerializer) {
        ClassUtil.verifyMustOverride(StdDelegatingSerializer.class, this, "withDelegate");
        return new StdDelegatingSerializer(converter, delegateType, delegateSerializer);
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        if (this._delegateSerializer != null && this._delegateSerializer instanceof ResolvableSerializer) {
            ((ResolvableSerializer)((Object)this._delegateSerializer)).resolve(provider);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        JsonSerializer<Object> delSer = this._delegateSerializer;
        JavaType delegateType = this._delegateType;
        if (delSer == null) {
            if (delegateType == null) {
                delegateType = this._converter.getOutputType(provider.getTypeFactory());
            }
            if (!delegateType.isJavaLangObject()) {
                delSer = provider.findValueSerializer(delegateType);
            }
        }
        if (delSer instanceof ContextualSerializer) {
            delSer = provider.handleSecondaryContextualization(delSer, property);
        }
        if (delSer == this._delegateSerializer && delegateType == this._delegateType) {
            return this;
        }
        return this.withDelegate(this._converter, delegateType, delSer);
    }

    protected Converter<Object, ?> getConverter() {
        return this._converter;
    }

    @Override
    public JsonSerializer<?> getDelegatee() {
        return this._delegateSerializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Object delegateValue = this.convertValue(value);
        if (delegateValue == null) {
            provider.defaultSerializeNull(gen);
            return;
        }
        JsonSerializer<Object> ser = this._delegateSerializer;
        if (ser == null) {
            ser = this._findSerializer(delegateValue, provider);
        }
        ser.serialize(delegateValue, gen, provider);
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        Object delegateValue = this.convertValue(value);
        JsonSerializer<Object> ser = this._delegateSerializer;
        if (ser == null) {
            ser = this._findSerializer(value, provider);
        }
        ser.serializeWithType(delegateValue, gen, provider, typeSer);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
        Object delegateValue = this.convertValue(value);
        if (delegateValue == null) {
            return true;
        }
        if (this._delegateSerializer == null) {
            return value == null;
        }
        return this._delegateSerializer.isEmpty(prov, delegateValue);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
        if (this._delegateSerializer instanceof SchemaAware) {
            return ((SchemaAware)((Object)this._delegateSerializer)).getSchema(provider, typeHint);
        }
        return super.getSchema(provider, typeHint);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint, boolean isOptional) throws JsonMappingException {
        if (this._delegateSerializer instanceof SchemaAware) {
            return ((SchemaAware)((Object)this._delegateSerializer)).getSchema(provider, typeHint, isOptional);
        }
        return super.getSchema(provider, typeHint);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        if (this._delegateSerializer != null) {
            this._delegateSerializer.acceptJsonFormatVisitor(visitor, typeHint);
        }
    }

    protected Object convertValue(Object value) {
        return this._converter.convert(value);
    }

    protected JsonSerializer<Object> _findSerializer(Object value, SerializerProvider serializers) throws JsonMappingException {
        return serializers.findValueSerializer(value.getClass());
    }
}

