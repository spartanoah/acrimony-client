/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.NameTransformer;
import java.io.IOException;
import java.util.Collection;

public abstract class JsonDeserializer<T>
implements NullValueProvider {
    public abstract T deserialize(JsonParser var1, DeserializationContext var2) throws IOException, JsonProcessingException;

    public T deserialize(JsonParser p, DeserializationContext ctxt, T intoValue) throws IOException {
        ctxt.handleBadMerge(this);
        return this.deserialize(p, ctxt);
    }

    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return typeDeserializer.deserializeTypedFromAny(p, ctxt);
    }

    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer, T intoValue) throws IOException {
        ctxt.handleBadMerge(this);
        return this.deserializeWithType(p, ctxt, typeDeserializer);
    }

    public JsonDeserializer<T> unwrappingDeserializer(NameTransformer unwrapper) {
        return this;
    }

    public JsonDeserializer<?> replaceDelegatee(JsonDeserializer<?> delegatee) {
        throw new UnsupportedOperationException();
    }

    public Class<?> handledType() {
        return null;
    }

    public boolean isCachable() {
        return false;
    }

    public JsonDeserializer<?> getDelegatee() {
        return null;
    }

    public Collection<Object> getKnownPropertyNames() {
        return null;
    }

    public T getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return this.getNullValue();
    }

    @Override
    public AccessPattern getNullAccessPattern() {
        return AccessPattern.CONSTANT;
    }

    public AccessPattern getEmptyAccessPattern() {
        return AccessPattern.DYNAMIC;
    }

    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return this.getNullValue(ctxt);
    }

    public ObjectIdReader getObjectIdReader() {
        return null;
    }

    public SettableBeanProperty findBackReference(String refName) {
        throw new IllegalArgumentException("Cannot handle managed/back reference '" + refName + "': type: value deserializer of type " + this.getClass().getName() + " does not support them");
    }

    public Boolean supportsUpdate(DeserializationConfig config) {
        return null;
    }

    @Deprecated
    public T getNullValue() {
        return null;
    }

    @Deprecated
    public Object getEmptyValue() {
        return this.getNullValue();
    }

    public static abstract class None
    extends JsonDeserializer<Object> {
        private None() {
        }
    }
}

