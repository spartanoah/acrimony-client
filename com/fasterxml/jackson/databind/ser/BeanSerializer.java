/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Set;

public class BeanSerializer
extends BeanSerializerBase {
    private static final long serialVersionUID = 29L;

    public BeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(type, builder, properties, filteredProperties);
    }

    protected BeanSerializer(BeanSerializerBase src) {
        super(src);
    }

    protected BeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);
    }

    protected BeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
        super(src, objectIdWriter, filterId);
    }

    protected BeanSerializer(BeanSerializerBase src, Set<String> toIgnore) {
        super(src, toIgnore);
    }

    protected BeanSerializer(BeanSerializerBase src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super(src, properties, filteredProperties);
    }

    @Deprecated
    public static BeanSerializer createDummy(JavaType forType) {
        return new BeanSerializer(forType, null, NO_PROPS, null);
    }

    public static BeanSerializer createDummy(JavaType forType, BeanSerializerBuilder builder) {
        return new BeanSerializer(forType, builder, NO_PROPS, null);
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer unwrapper) {
        return new UnwrappingBeanSerializer((BeanSerializerBase)this, unwrapper);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new BeanSerializer((BeanSerializerBase)this, objectIdWriter, this._propertyFilterId);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new BeanSerializer((BeanSerializerBase)this, this._objectIdWriter, filterId);
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
        return new BeanSerializer((BeanSerializerBase)this, toIgnore);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        return new BeanSerializer((BeanSerializerBase)this, properties, filteredProperties);
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        if (this._objectIdWriter == null && this._anyGetterWriter == null && this._propertyFilterId == null) {
            return new BeanAsArraySerializer(this);
        }
        return this;
    }

    @Override
    public final void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (this._objectIdWriter != null) {
            gen.setCurrentValue(bean);
            this._serializeWithObjectId(bean, gen, provider, true);
            return;
        }
        gen.writeStartObject(bean);
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, gen, provider);
        } else {
            this.serializeFields(bean, gen, provider);
        }
        gen.writeEndObject();
    }

    public String toString() {
        return "BeanSerializer for " + this.handledType().getName();
    }
}

