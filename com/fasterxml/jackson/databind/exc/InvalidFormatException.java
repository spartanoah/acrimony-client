/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.exc;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class InvalidFormatException
extends MismatchedInputException {
    private static final long serialVersionUID = 1L;
    protected final Object _value;

    @Deprecated
    public InvalidFormatException(String msg, Object value, Class<?> targetType) {
        super(null, msg);
        this._value = value;
        this._targetType = targetType;
    }

    @Deprecated
    public InvalidFormatException(String msg, JsonLocation loc, Object value, Class<?> targetType) {
        super(null, msg, loc);
        this._value = value;
        this._targetType = targetType;
    }

    public InvalidFormatException(JsonParser p, String msg, Object value, Class<?> targetType) {
        super(p, msg, targetType);
        this._value = value;
    }

    public static InvalidFormatException from(JsonParser p, String msg, Object value, Class<?> targetType) {
        return new InvalidFormatException(p, msg, value, targetType);
    }

    public Object getValue() {
        return this._value;
    }
}

