/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

public class PropertyBasedObjectIdGenerator
extends ObjectIdGenerators.PropertyGenerator {
    private static final long serialVersionUID = 1L;

    public PropertyBasedObjectIdGenerator(Class<?> scope) {
        super(scope);
    }

    @Override
    public Object generateId(Object forPojo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectIdGenerator<Object> forScope(Class<?> scope) {
        return scope == this._scope ? this : new PropertyBasedObjectIdGenerator(scope);
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

