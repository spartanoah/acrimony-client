/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.ReferenceTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceDeserializer
extends ReferenceTypeDeserializer<AtomicReference<Object>> {
    private static final long serialVersionUID = 1L;

    public AtomicReferenceDeserializer(JavaType fullType, ValueInstantiator inst, TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
        super(fullType, inst, typeDeser, deser);
    }

    public AtomicReferenceDeserializer withResolved(TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
        return new AtomicReferenceDeserializer(this._fullType, this._valueInstantiator, typeDeser, valueDeser);
    }

    @Override
    public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return new AtomicReference<Object>(this._valueDeserializer.getNullValue(ctxt));
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return this.getNullValue(ctxt);
    }

    @Override
    public AtomicReference<Object> referenceValue(Object contents) {
        return new AtomicReference<Object>(contents);
    }

    @Override
    public Object getReferenced(AtomicReference<Object> reference) {
        return reference.get();
    }

    @Override
    public AtomicReference<Object> updateReference(AtomicReference<Object> reference, Object contents) {
        reference.set(contents);
        return reference;
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.TRUE;
    }
}

