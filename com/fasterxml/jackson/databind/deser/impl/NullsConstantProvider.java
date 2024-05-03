/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.util.AccessPattern;
import java.io.Serializable;

public class NullsConstantProvider
implements NullValueProvider,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final NullsConstantProvider SKIPPER = new NullsConstantProvider(null);
    private static final NullsConstantProvider NULLER = new NullsConstantProvider(null);
    protected final Object _nullValue;
    protected final AccessPattern _access;

    protected NullsConstantProvider(Object nvl) {
        this._nullValue = nvl;
        this._access = this._nullValue == null ? AccessPattern.ALWAYS_NULL : AccessPattern.CONSTANT;
    }

    public static NullsConstantProvider skipper() {
        return SKIPPER;
    }

    public static NullsConstantProvider nuller() {
        return NULLER;
    }

    public static NullsConstantProvider forValue(Object nvl) {
        if (nvl == null) {
            return NULLER;
        }
        return new NullsConstantProvider(nvl);
    }

    public static boolean isSkipper(NullValueProvider p) {
        return p == SKIPPER;
    }

    public static boolean isNuller(NullValueProvider p) {
        return p == NULLER;
    }

    @Override
    public AccessPattern getNullAccessPattern() {
        return this._access;
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) {
        return this._nullValue;
    }
}

