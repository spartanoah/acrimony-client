/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class UnwrappingBeanSerializer
extends BeanSerializerBase
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final NameTransformer _nameTransformer;

    public UnwrappingBeanSerializer(BeanSerializerBase src, NameTransformer transformer) {
        super(src, transformer);
        this._nameTransformer = transformer;
    }

    public UnwrappingBeanSerializer(UnwrappingBeanSerializer src, ObjectIdWriter objectIdWriter) {
        super((BeanSerializerBase)src, objectIdWriter);
        this._nameTransformer = src._nameTransformer;
    }

    public UnwrappingBeanSerializer(UnwrappingBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId) {
        super((BeanSerializerBase)src, objectIdWriter, filterId);
        this._nameTransformer = src._nameTransformer;
    }

    protected UnwrappingBeanSerializer(UnwrappingBeanSerializer src, Set<String> toIgnore) {
        super((BeanSerializerBase)src, toIgnore);
        this._nameTransformer = src._nameTransformer;
    }

    protected UnwrappingBeanSerializer(UnwrappingBeanSerializer src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super((BeanSerializerBase)src, properties, filteredProperties);
        this._nameTransformer = src._nameTransformer;
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer) {
        return new UnwrappingBeanSerializer((BeanSerializerBase)this, transformer);
    }

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new UnwrappingBeanSerializer(this, objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new UnwrappingBeanSerializer(this, this._objectIdWriter, filterId);
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
        return new UnwrappingBeanSerializer(this, toIgnore);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        return new UnwrappingBeanSerializer(this, properties, filteredProperties);
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        return this;
    }

    @Override
    public final void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.setCurrentValue(bean);
        if (this._objectIdWriter != null) {
            this._serializeWithObjectId(bean, gen, provider, false);
            return;
        }
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, gen, provider);
        } else {
            this.serializeFields(bean, gen, provider);
        }
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        if (provider.isEnabled(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS)) {
            provider.reportBadDefinition(this.handledType(), "Unwrapped property requires use of type information: cannot serialize without disabling `SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS`");
        }
        gen.setCurrentValue(bean);
        if (this._objectIdWriter != null) {
            this._serializeWithObjectId(bean, gen, provider, typeSer);
            return;
        }
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, gen, provider);
        } else {
            this.serializeFields(bean, gen, provider);
        }
    }

    public String toString() {
        return "UnwrappingBeanSerializer for " + this.handledType().getName();
    }
}

