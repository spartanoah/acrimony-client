/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.deser.SettableAnyProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import java.io.IOException;

public abstract class PropertyValue {
    public final PropertyValue next;
    public final Object value;

    protected PropertyValue(PropertyValue next, Object value) {
        this.next = next;
        this.value = value;
    }

    public abstract void assign(Object var1) throws IOException, JsonProcessingException;

    static final class Map
    extends PropertyValue {
        final Object _key;

        public Map(PropertyValue next, Object value, Object key) {
            super(next, value);
            this._key = key;
        }

        @Override
        public void assign(Object bean) throws IOException, JsonProcessingException {
            ((java.util.Map)bean).put(this._key, this.value);
        }
    }

    static final class Any
    extends PropertyValue {
        final SettableAnyProperty _property;
        final String _propertyName;

        public Any(PropertyValue next, Object value, SettableAnyProperty prop, String propName) {
            super(next, value);
            this._property = prop;
            this._propertyName = propName;
        }

        @Override
        public void assign(Object bean) throws IOException, JsonProcessingException {
            this._property.set(bean, this._propertyName, this.value);
        }
    }

    static final class Regular
    extends PropertyValue {
        final SettableBeanProperty _property;

        public Regular(PropertyValue next, Object value, SettableBeanProperty prop) {
            super(next, value);
            this._property = prop;
        }

        @Override
        public void assign(Object bean) throws IOException, JsonProcessingException {
            this._property.set(bean, this.value);
        }
    }
}

