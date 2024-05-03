/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;

@JacksonStdImpl
public class StringDeserializer
extends StdScalarDeserializer<String> {
    private static final long serialVersionUID = 1L;
    public static final StringDeserializer instance = new StringDeserializer();

    public StringDeserializer() {
        super(String.class);
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return "";
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text;
        if (p.hasToken(JsonToken.VALUE_STRING)) {
            return p.getText();
        }
        JsonToken t = p.currentToken();
        if (t == JsonToken.START_ARRAY) {
            return (String)this._deserializeFromArray(p, ctxt);
        }
        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = p.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (ob instanceof byte[]) {
                return ctxt.getBase64Variant().encode((byte[])ob, false);
            }
            return ob.toString();
        }
        if (t.isScalarValue() && (text = p.getValueAsString()) != null) {
            return text;
        }
        return (String)ctxt.handleUnexpectedToken(this._valueClass, p);
    }

    @Override
    public String deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return this.deserialize(p, ctxt);
    }
}

