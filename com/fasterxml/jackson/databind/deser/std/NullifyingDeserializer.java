/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;

public class NullifyingDeserializer
extends StdDeserializer<Object> {
    private static final long serialVersionUID = 1L;
    public static final NullifyingDeserializer instance = new NullifyingDeserializer();

    public NullifyingDeserializer() {
        super(Object.class);
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        return Boolean.FALSE;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.hasToken(JsonToken.FIELD_NAME)) {
            JsonToken t;
            while ((t = p.nextToken()) != null && t != JsonToken.END_OBJECT) {
                p.skipChildren();
            }
        } else {
            p.skipChildren();
        }
        return null;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        switch (p.currentTokenId()) {
            case 1: 
            case 3: 
            case 5: {
                return typeDeserializer.deserializeTypedFromAny(p, ctxt);
            }
        }
        return null;
    }
}

