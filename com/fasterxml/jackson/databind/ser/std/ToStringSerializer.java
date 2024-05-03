/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;

@JacksonStdImpl
public class ToStringSerializer
extends ToStringSerializerBase {
    public static final ToStringSerializer instance = new ToStringSerializer();

    public ToStringSerializer() {
        super(Object.class);
    }

    public ToStringSerializer(Class<?> handledType) {
        super(handledType);
    }

    @Override
    public final String valueToString(Object value) {
        return value.toString();
    }
}

