/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import java.util.Map;

public class AnyGetterWriter {
    protected final BeanProperty _property;
    protected final AnnotatedMember _accessor;
    protected JsonSerializer<Object> _serializer;
    protected MapSerializer _mapSerializer;

    public AnyGetterWriter(BeanProperty property, AnnotatedMember accessor, JsonSerializer<?> serializer) {
        this._accessor = accessor;
        this._property = property;
        this._serializer = serializer;
        if (serializer instanceof MapSerializer) {
            this._mapSerializer = (MapSerializer)serializer;
        }
    }

    public void fixAccess(SerializationConfig config) {
        this._accessor.fixAccess(config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
    }

    public void getAndSerialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws Exception {
        Object value = this._accessor.getValue(bean);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map)) {
            provider.reportBadDefinition(this._property.getType(), String.format("Value returned by 'any-getter' %s() not java.util.Map but %s", this._accessor.getName(), value.getClass().getName()));
        }
        if (this._mapSerializer != null) {
            this._mapSerializer.serializeWithoutTypeInfo((Map)value, gen, provider);
            return;
        }
        this._serializer.serialize(value, gen, provider);
    }

    public void getAndFilter(Object bean, JsonGenerator gen, SerializerProvider provider, PropertyFilter filter) throws Exception {
        Object value = this._accessor.getValue(bean);
        if (value == null) {
            return;
        }
        if (!(value instanceof Map)) {
            provider.reportBadDefinition(this._property.getType(), String.format("Value returned by 'any-getter' (%s()) not java.util.Map but %s", this._accessor.getName(), value.getClass().getName()));
        }
        if (this._mapSerializer != null) {
            this._mapSerializer.serializeFilteredAnyProperties(provider, gen, bean, (Map)value, filter, null);
            return;
        }
        this._serializer.serialize(value, gen, provider);
    }

    public void resolve(SerializerProvider provider) throws JsonMappingException {
        if (this._serializer instanceof ContextualSerializer) {
            JsonSerializer<?> ser = provider.handlePrimaryContextualization(this._serializer, this._property);
            this._serializer = ser;
            if (ser instanceof MapSerializer) {
                this._mapSerializer = (MapSerializer)ser;
            }
        }
    }
}

