/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.exc;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.util.ClassUtil;

public class InvalidNullException
extends MismatchedInputException {
    private static final long serialVersionUID = 1L;
    protected final PropertyName _propertyName;

    protected InvalidNullException(DeserializationContext ctxt, String msg, PropertyName pname) {
        super(ctxt.getParser(), msg);
        this._propertyName = pname;
    }

    public static InvalidNullException from(DeserializationContext ctxt, PropertyName name, JavaType type) {
        String msg = String.format("Invalid `null` value encountered for property %s", ClassUtil.quotedOr(name, "<UNKNOWN>"));
        InvalidNullException exc = new InvalidNullException(ctxt, msg, name);
        if (type != null) {
            exc.setTargetType(type);
        }
        return exc;
    }

    public PropertyName getPropertyName() {
        return this._propertyName;
    }
}

