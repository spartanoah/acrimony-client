/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public final class ManagedReferenceProperty
extends SettableBeanProperty.Delegating {
    private static final long serialVersionUID = 1L;
    protected final String _referenceName;
    protected final boolean _isContainer;
    protected final SettableBeanProperty _backProperty;

    public ManagedReferenceProperty(SettableBeanProperty forward, String refName, SettableBeanProperty backward, boolean isContainer) {
        super(forward);
        this._referenceName = refName;
        this._backProperty = backward;
        this._isContainer = isContainer;
    }

    @Override
    protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
        throw new IllegalStateException("Should never try to reset delegate");
    }

    @Override
    public void fixAccess(DeserializationConfig config) {
        this.delegate.fixAccess(config);
        this._backProperty.fixAccess(config);
    }

    @Override
    public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
        this.set(instance, this.delegate.deserialize(p, ctxt));
    }

    @Override
    public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
        return this.setAndReturn(instance, this.deserialize(p, ctxt));
    }

    @Override
    public final void set(Object instance, Object value) throws IOException {
        this.setAndReturn(instance, value);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Object setAndReturn(Object instance, Object value) throws IOException {
        if (value == null) return this.delegate.setAndReturn(instance, value);
        if (this._isContainer) {
            if (value instanceof Object[]) {
                for (Object ob : (Object[])value) {
                    if (ob == null) continue;
                    this._backProperty.set(ob, instance);
                }
                return this.delegate.setAndReturn(instance, value);
            } else if (value instanceof Collection) {
                for (Object ob : (Collection)value) {
                    if (ob == null) continue;
                    this._backProperty.set(ob, instance);
                }
                return this.delegate.setAndReturn(instance, value);
            } else {
                if (!(value instanceof Map)) throw new IllegalStateException("Unsupported container type (" + value.getClass().getName() + ") when resolving reference '" + this._referenceName + "'");
                for (Object ob : ((Map)value).values()) {
                    if (ob == null) continue;
                    this._backProperty.set(ob, instance);
                }
            }
            return this.delegate.setAndReturn(instance, value);
        } else {
            this._backProperty.set(value, instance);
        }
        return this.delegate.setAndReturn(instance, value);
    }
}

