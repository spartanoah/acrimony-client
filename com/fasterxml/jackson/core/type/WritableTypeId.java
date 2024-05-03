/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.type;

import com.fasterxml.jackson.core.JsonToken;

public class WritableTypeId {
    public Object forValue;
    public Class<?> forValueType;
    public Object id;
    public String asProperty;
    public Inclusion include;
    public JsonToken valueShape;
    public boolean wrapperWritten;
    public Object extra;

    public WritableTypeId() {
    }

    public WritableTypeId(Object value, JsonToken valueShape0) {
        this(value, valueShape0, null);
    }

    public WritableTypeId(Object value, Class<?> valueType0, JsonToken valueShape0) {
        this(value, valueShape0, null);
        this.forValueType = valueType0;
    }

    public WritableTypeId(Object value, JsonToken valueShape0, Object id0) {
        this.forValue = value;
        this.id = id0;
        this.valueShape = valueShape0;
    }

    public static enum Inclusion {
        WRAPPER_ARRAY,
        WRAPPER_OBJECT,
        METADATA_PROPERTY,
        PAYLOAD_PROPERTY,
        PARENT_PROPERTY;


        public boolean requiresObjectContext() {
            return this == METADATA_PROPERTY || this == PAYLOAD_PROPERTY;
        }
    }
}

