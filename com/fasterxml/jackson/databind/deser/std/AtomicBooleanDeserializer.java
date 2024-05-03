/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicBooleanDeserializer
extends StdScalarDeserializer<AtomicBoolean> {
    private static final long serialVersionUID = 1L;

    public AtomicBooleanDeserializer() {
        super(AtomicBoolean.class);
    }

    @Override
    public AtomicBoolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return new AtomicBoolean(this._parseBooleanPrimitive(ctxt, p, AtomicBoolean.class));
    }
}

