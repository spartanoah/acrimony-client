/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;

public class NullNode
extends ValueNode {
    private static final long serialVersionUID = 1L;
    public static final NullNode instance = new NullNode();

    protected NullNode() {
    }

    protected Object readResolve() {
        return instance;
    }

    public static NullNode getInstance() {
        return instance;
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.NULL;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NULL;
    }

    @Override
    public String asText(String defaultValue) {
        return defaultValue;
    }

    @Override
    public String asText() {
        return "null";
    }

    public JsonNode requireNonNull() {
        return (JsonNode)this._reportRequiredViolation("requireNonNull() called on `NullNode`", new Object[0]);
    }

    @Override
    public final void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {
        provider.defaultSerializeNull(g);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof NullNode;
    }

    @Override
    public int hashCode() {
        return JsonNodeType.NULL.ordinal();
    }
}

