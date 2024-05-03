/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import java.io.IOException;
import java.util.Collection;

public abstract class DelegatingDeserializer
extends StdDeserializer<Object>
implements ContextualDeserializer,
ResolvableDeserializer {
    private static final long serialVersionUID = 1L;
    protected final JsonDeserializer<?> _delegatee;

    public DelegatingDeserializer(JsonDeserializer<?> d) {
        super(d.handledType());
        this._delegatee = d;
    }

    protected abstract JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> var1);

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        if (this._delegatee instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer)((Object)this._delegatee)).resolve(ctxt);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType vt = ctxt.constructType(this._delegatee.handledType());
        JsonDeserializer<?> del = ctxt.handleSecondaryContextualization(this._delegatee, property, vt);
        if (del == this._delegatee) {
            return this;
        }
        return this.newDelegatingInstance(del);
    }

    @Override
    public JsonDeserializer<?> replaceDelegatee(JsonDeserializer<?> delegatee) {
        if (delegatee == this._delegatee) {
            return this;
        }
        return this.newDelegatingInstance(delegatee);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this._delegatee.deserialize(p, ctxt);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) throws IOException {
        return this._delegatee.deserialize(p, ctxt, intoValue);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return this._delegatee.deserializeWithType(p, ctxt, typeDeserializer);
    }

    @Override
    public boolean isCachable() {
        return this._delegatee.isCachable();
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return this._delegatee.supportsUpdate(config);
    }

    @Override
    public JsonDeserializer<?> getDelegatee() {
        return this._delegatee;
    }

    @Override
    public SettableBeanProperty findBackReference(String logicalName) {
        return this._delegatee.findBackReference(logicalName);
    }

    @Override
    public AccessPattern getNullAccessPattern() {
        return this._delegatee.getNullAccessPattern();
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return this._delegatee.getNullValue(ctxt);
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return this._delegatee.getEmptyValue(ctxt);
    }

    @Override
    public Collection<Object> getKnownPropertyNames() {
        return this._delegatee.getKnownPropertyNames();
    }

    @Override
    public ObjectIdReader getObjectIdReader() {
        return this._delegatee.getObjectIdReader();
    }
}

