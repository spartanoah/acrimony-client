/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.exc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.Closeable;

public class ValueInstantiationException
extends JsonMappingException {
    protected final JavaType _type;

    protected ValueInstantiationException(JsonParser p, String msg, JavaType type, Throwable cause) {
        super((Closeable)p, msg, cause);
        this._type = type;
    }

    protected ValueInstantiationException(JsonParser p, String msg, JavaType type) {
        super(p, msg);
        this._type = type;
    }

    public static ValueInstantiationException from(JsonParser p, String msg, JavaType type) {
        return new ValueInstantiationException(p, msg, type);
    }

    public static ValueInstantiationException from(JsonParser p, String msg, JavaType type, Throwable cause) {
        return new ValueInstantiationException(p, msg, type, cause);
    }

    public JavaType getType() {
        return this._type;
    }
}

