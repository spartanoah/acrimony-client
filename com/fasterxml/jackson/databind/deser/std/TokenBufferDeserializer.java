/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;

@JacksonStdImpl
public class TokenBufferDeserializer
extends StdScalarDeserializer<TokenBuffer> {
    private static final long serialVersionUID = 1L;

    public TokenBufferDeserializer() {
        super(TokenBuffer.class);
    }

    @Override
    public TokenBuffer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this.createBufferInstance(p).deserialize(p, ctxt);
    }

    protected TokenBuffer createBufferInstance(JsonParser p) {
        return new TokenBuffer(p);
    }
}

