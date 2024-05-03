/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;

@Deprecated
public abstract class NonTypedScalarSerializerBase<T>
extends StdScalarSerializer<T> {
    protected NonTypedScalarSerializerBase(Class<T> t) {
        super(t);
    }

    protected NonTypedScalarSerializerBase(Class<?> t, boolean bogus) {
        super(t, bogus);
    }

    @Override
    public final void serializeWithType(T value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        this.serialize(value, gen, provider);
    }
}

