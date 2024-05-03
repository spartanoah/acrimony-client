/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.impl;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;

public final class ObjectIdWriter {
    public final JavaType idType;
    public final SerializableString propertyName;
    public final ObjectIdGenerator<?> generator;
    public final JsonSerializer<Object> serializer;
    public final boolean alwaysAsId;

    protected ObjectIdWriter(JavaType t, SerializableString propName, ObjectIdGenerator<?> gen, JsonSerializer<?> ser, boolean alwaysAsId) {
        this.idType = t;
        this.propertyName = propName;
        this.generator = gen;
        this.serializer = ser;
        this.alwaysAsId = alwaysAsId;
    }

    public static ObjectIdWriter construct(JavaType idType, PropertyName propName, ObjectIdGenerator<?> generator, boolean alwaysAsId) {
        String simpleName = propName == null ? null : propName.getSimpleName();
        SerializedString serName = simpleName == null ? null : new SerializedString(simpleName);
        return new ObjectIdWriter(idType, serName, generator, null, alwaysAsId);
    }

    public ObjectIdWriter withSerializer(JsonSerializer<?> ser) {
        return new ObjectIdWriter(this.idType, this.propertyName, this.generator, ser, this.alwaysAsId);
    }

    public ObjectIdWriter withAlwaysAsId(boolean newState) {
        if (newState == this.alwaysAsId) {
            return this;
        }
        return new ObjectIdWriter(this.idType, this.propertyName, this.generator, this.serializer, newState);
    }
}

