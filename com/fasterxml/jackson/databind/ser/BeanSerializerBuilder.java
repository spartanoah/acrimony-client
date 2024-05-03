/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.AnyGetterWriter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import java.util.Collections;
import java.util.List;

public class BeanSerializerBuilder {
    private static final BeanPropertyWriter[] NO_PROPERTIES = new BeanPropertyWriter[0];
    protected final BeanDescription _beanDesc;
    protected SerializationConfig _config;
    protected List<BeanPropertyWriter> _properties = Collections.emptyList();
    protected BeanPropertyWriter[] _filteredProperties;
    protected AnyGetterWriter _anyGetter;
    protected Object _filterId;
    protected AnnotatedMember _typeId;
    protected ObjectIdWriter _objectIdWriter;

    public BeanSerializerBuilder(BeanDescription beanDesc) {
        this._beanDesc = beanDesc;
    }

    protected BeanSerializerBuilder(BeanSerializerBuilder src) {
        this._beanDesc = src._beanDesc;
        this._properties = src._properties;
        this._filteredProperties = src._filteredProperties;
        this._anyGetter = src._anyGetter;
        this._filterId = src._filterId;
    }

    protected void setConfig(SerializationConfig config) {
        this._config = config;
    }

    public void setProperties(List<BeanPropertyWriter> properties) {
        this._properties = properties;
    }

    public void setFilteredProperties(BeanPropertyWriter[] properties) {
        if (properties != null && properties.length != this._properties.size()) {
            throw new IllegalArgumentException(String.format("Trying to set %d filtered properties; must match length of non-filtered `properties` (%d)", properties.length, this._properties.size()));
        }
        this._filteredProperties = properties;
    }

    public void setAnyGetter(AnyGetterWriter anyGetter) {
        this._anyGetter = anyGetter;
    }

    public void setFilterId(Object filterId) {
        this._filterId = filterId;
    }

    public void setTypeId(AnnotatedMember idProp) {
        if (this._typeId != null) {
            throw new IllegalArgumentException("Multiple type ids specified with " + this._typeId + " and " + idProp);
        }
        this._typeId = idProp;
    }

    public void setObjectIdWriter(ObjectIdWriter w) {
        this._objectIdWriter = w;
    }

    public AnnotatedClass getClassInfo() {
        return this._beanDesc.getClassInfo();
    }

    public BeanDescription getBeanDescription() {
        return this._beanDesc;
    }

    public List<BeanPropertyWriter> getProperties() {
        return this._properties;
    }

    public boolean hasProperties() {
        return this._properties != null && this._properties.size() > 0;
    }

    public BeanPropertyWriter[] getFilteredProperties() {
        return this._filteredProperties;
    }

    public AnyGetterWriter getAnyGetter() {
        return this._anyGetter;
    }

    public Object getFilterId() {
        return this._filterId;
    }

    public AnnotatedMember getTypeId() {
        return this._typeId;
    }

    public ObjectIdWriter getObjectIdWriter() {
        return this._objectIdWriter;
    }

    public JsonSerializer<?> build() {
        BeanPropertyWriter[] properties;
        if (this._typeId != null && this._config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
            this._typeId.fixAccess(this._config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
        }
        if (this._anyGetter != null) {
            this._anyGetter.fixAccess(this._config);
        }
        if (this._properties == null || this._properties.isEmpty()) {
            if (this._anyGetter == null && this._objectIdWriter == null) {
                return null;
            }
            properties = NO_PROPERTIES;
        } else {
            properties = this._properties.toArray(new BeanPropertyWriter[this._properties.size()]);
            if (this._config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) {
                int end = properties.length;
                for (int i = 0; i < end; ++i) {
                    properties[i].fixAccess(this._config);
                }
            }
        }
        if (this._filteredProperties != null && this._filteredProperties.length != this._properties.size()) {
            throw new IllegalStateException(String.format("Mismatch between `properties` size (%d), `filteredProperties` (%s): should have as many (or `null` for latter)", this._properties.size(), this._filteredProperties.length));
        }
        return new BeanSerializer(this._beanDesc.getType(), this, properties, this._filteredProperties);
    }

    public BeanSerializer createDummy() {
        return BeanSerializer.createDummy(this._beanDesc.getType(), this);
    }
}

