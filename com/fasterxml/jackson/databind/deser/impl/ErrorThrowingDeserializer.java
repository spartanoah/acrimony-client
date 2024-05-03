/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class ErrorThrowingDeserializer
extends JsonDeserializer<Object> {
    private final Error _cause;

    public ErrorThrowingDeserializer(NoClassDefFoundError cause) {
        this._cause = cause;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        throw this._cause;
    }
}

