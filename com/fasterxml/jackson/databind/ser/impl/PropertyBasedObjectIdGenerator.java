/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class PropertyBasedObjectIdGenerator
extends ObjectIdGenerators.PropertyGenerator {
    private static final long serialVersionUID = 1L;
    protected final BeanPropertyWriter _property;

    public PropertyBasedObjectIdGenerator(ObjectIdInfo oid, BeanPropertyWriter prop) {
        this(oid.getScope(), prop);
    }

    protected PropertyBasedObjectIdGenerator(Class<?> scope, BeanPropertyWriter prop) {
        super(scope);
        this._property = prop;
    }

    @Override
    public boolean canUseFor(ObjectIdGenerator<?> gen) {
        PropertyBasedObjectIdGenerator other;
        if (gen.getClass() == this.getClass() && (other = (PropertyBasedObjectIdGenerator)gen).getScope() == this._scope) {
            return other._property == this._property;
        }
        return false;
    }

    @Override
    public Object generateId(Object forPojo) {
        try {
            return this._property.get(forPojo);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Problem accessing property '" + this._property.getName() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public ObjectIdGenerator<Object> forScope(Class<?> scope) {
        return scope == this._scope ? this : new PropertyBasedObjectIdGenerator(scope, this._property);
    }

    @Override
    public ObjectIdGenerator<Object> newForSerialization(Object context) {
        return this;
    }

    @Override
    public ObjectIdGenerator.IdKey key(Object key) {
        if (key == null) {
            return null;
        }
        return new ObjectIdGenerator.IdKey(this.getClass(), this._scope, key);
    }
}

