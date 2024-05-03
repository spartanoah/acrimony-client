/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonGenerationException
extends JsonProcessingException {
    private static final long serialVersionUID = 123L;
    protected transient JsonGenerator _processor;

    @Deprecated
    public JsonGenerationException(Throwable rootCause) {
        super(rootCause);
    }

    @Deprecated
    public JsonGenerationException(String msg) {
        super(msg, (JsonLocation)null);
    }

    @Deprecated
    public JsonGenerationException(String msg, Throwable rootCause) {
        super(msg, null, rootCause);
    }

    public JsonGenerationException(Throwable rootCause, JsonGenerator g) {
        super(rootCause);
        this._processor = g;
    }

    public JsonGenerationException(String msg, JsonGenerator g) {
        super(msg, (JsonLocation)null);
        this._processor = g;
    }

    public JsonGenerationException(String msg, Throwable rootCause, JsonGenerator g) {
        super(msg, null, rootCause);
        this._processor = g;
    }

    public JsonGenerationException withGenerator(JsonGenerator g) {
        this._processor = g;
        return this;
    }

    @Override
    public JsonGenerator getProcessor() {
        return this._processor;
    }
}

