/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SimpleBeanPropertyFilter
implements BeanPropertyFilter,
PropertyFilter {
    protected SimpleBeanPropertyFilter() {
    }

    public static SimpleBeanPropertyFilter serializeAll() {
        return SerializeExceptFilter.INCLUDE_ALL;
    }

    @Deprecated
    public static SimpleBeanPropertyFilter serializeAll(Set<String> properties) {
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter filterOutAllExcept(Set<String> properties) {
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter filterOutAllExcept(String ... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(Set<String> properties) {
        return new SerializeExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(String ... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new SerializeExceptFilter(properties);
    }

    public static PropertyFilter from(final BeanPropertyFilter src) {
        return new PropertyFilter(){

            @Override
            public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider prov, PropertyWriter writer) throws Exception {
                src.serializeAsField(pojo, jgen, prov, (BeanPropertyWriter)writer);
            }

            @Override
            public void depositSchemaProperty(PropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException {
                src.depositSchemaProperty((BeanPropertyWriter)writer, propertiesNode, provider);
            }

            @Override
            public void depositSchemaProperty(PropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws JsonMappingException {
                src.depositSchemaProperty((BeanPropertyWriter)writer, objectVisitor, provider);
            }

            @Override
            public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider prov, PropertyWriter writer) throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected boolean include(BeanPropertyWriter writer) {
        return true;
    }

    protected boolean include(PropertyWriter writer) {
        return true;
    }

    protected boolean includeElement(Object elementValue) {
        return true;
    }

    @Override
    @Deprecated
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer) throws Exception {
        if (this.include(writer)) {
            writer.serializeAsField(bean, jgen, provider);
        } else if (!jgen.canOmitFields()) {
            writer.serializeAsOmittedField(bean, jgen, provider);
        }
    }

    @Override
    @Deprecated
    public void depositSchemaProperty(BeanPropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException {
        if (this.include(writer)) {
            writer.depositSchemaProperty(propertiesNode, provider);
        }
    }

    @Override
    @Deprecated
    public void depositSchemaProperty(BeanPropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws JsonMappingException {
        if (this.include(writer)) {
            writer.depositSchemaProperty(objectVisitor, provider);
        }
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (this.include(writer)) {
            writer.serializeAsField(pojo, jgen, provider);
        } else if (!jgen.canOmitFields()) {
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }

    @Override
    public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (this.includeElement(elementValue)) {
            writer.serializeAsElement(elementValue, jgen, provider);
        }
    }

    @Override
    @Deprecated
    public void depositSchemaProperty(PropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException {
        if (this.include(writer)) {
            writer.depositSchemaProperty(propertiesNode, provider);
        }
    }

    @Override
    public void depositSchemaProperty(PropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws JsonMappingException {
        if (this.include(writer)) {
            writer.depositSchemaProperty(objectVisitor, provider);
        }
    }

    public static class SerializeExceptFilter
    extends SimpleBeanPropertyFilter
    implements Serializable {
        private static final long serialVersionUID = 1L;
        static final SerializeExceptFilter INCLUDE_ALL = new SerializeExceptFilter();
        protected final Set<String> _propertiesToExclude;

        SerializeExceptFilter() {
            this._propertiesToExclude = Collections.emptySet();
        }

        public SerializeExceptFilter(Set<String> properties) {
            this._propertiesToExclude = properties;
        }

        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return !this._propertiesToExclude.contains(writer.getName());
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            return !this._propertiesToExclude.contains(writer.getName());
        }
    }

    public static class FilterExceptFilter
    extends SimpleBeanPropertyFilter
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final Set<String> _propertiesToInclude;

        public FilterExceptFilter(Set<String> properties) {
            this._propertiesToInclude = properties;
        }

        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return this._propertiesToInclude.contains(writer.getName());
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            return this._propertiesToInclude.contains(writer.getName());
        }
    }
}

