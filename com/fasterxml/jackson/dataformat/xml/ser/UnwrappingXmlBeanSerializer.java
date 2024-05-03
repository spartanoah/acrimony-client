/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializerBase;
import java.io.IOException;
import java.util.Set;

public class UnwrappingXmlBeanSerializer
extends XmlBeanSerializerBase {
    private static final long serialVersionUID = 1L;
    protected final NameTransformer _nameTransformer;

    public UnwrappingXmlBeanSerializer(XmlBeanSerializerBase src, NameTransformer transformer) {
        super(src, transformer);
        this._nameTransformer = transformer;
    }

    public UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src, ObjectIdWriter objectIdWriter) {
        super((XmlBeanSerializerBase)src, objectIdWriter);
        this._nameTransformer = src._nameTransformer;
    }

    public UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId) {
        super((XmlBeanSerializerBase)src, objectIdWriter, filterId);
        this._nameTransformer = src._nameTransformer;
    }

    protected UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src, Set<String> toIgnore, Set<String> toInclude) {
        super((XmlBeanSerializerBase)src, toIgnore, toInclude);
        this._nameTransformer = src._nameTransformer;
    }

    protected UnwrappingXmlBeanSerializer(UnwrappingXmlBeanSerializer src, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        super((XmlBeanSerializerBase)src, properties, filteredProperties);
        this._nameTransformer = src._nameTransformer;
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(NameTransformer transformer) {
        return new UnwrappingXmlBeanSerializer((XmlBeanSerializerBase)this, transformer);
    }

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new UnwrappingXmlBeanSerializer(this, objectIdWriter);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new UnwrappingXmlBeanSerializer(this, this._objectIdWriter, filterId);
    }

    protected BeanSerializerBase withByNameInclusion(Set<String> toIgnore, Set<String> toInclude) {
        return new UnwrappingXmlBeanSerializer(this, toIgnore, toInclude);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
        return new UnwrappingXmlBeanSerializer(this, properties, filteredProperties);
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        return this;
    }

    @Override
    public final void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        if (this._objectIdWriter != null) {
            this._serializeWithObjectId(bean, jgen, provider, false);
            return;
        }
        if (this._propertyFilterId != null) {
            this.serializeFieldsFiltered(bean, jgen, provider);
        } else {
            this.serializeFields(bean, jgen, provider);
        }
    }

    public String toString() {
        return "UnwrappingXmlBeanSerializer for " + this.handledType().getName();
    }
}

