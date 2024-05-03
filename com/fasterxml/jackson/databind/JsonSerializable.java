/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;

public interface JsonSerializable {
    public void serialize(JsonGenerator var1, SerializerProvider var2) throws IOException;

    public void serializeWithType(JsonGenerator var1, SerializerProvider var2, TypeSerializer var3) throws IOException;

    public static abstract class Base
    implements JsonSerializable {
        public boolean isEmpty(SerializerProvider serializers) {
            return false;
        }
    }
}

