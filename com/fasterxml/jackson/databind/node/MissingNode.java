/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;

public final class MissingNode
extends ValueNode {
    private static final long serialVersionUID = 1L;
    private static final MissingNode instance = new MissingNode();

    protected MissingNode() {
    }

    protected Object readResolve() {
        return instance;
    }

    @Override
    public boolean isMissingNode() {
        return true;
    }

    @Override
    public <T extends JsonNode> T deepCopy() {
        return (T)this;
    }

    public static MissingNode getInstance() {
        return instance;
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.MISSING;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.NOT_AVAILABLE;
    }

    @Override
    public String asText() {
        return "";
    }

    @Override
    public String asText(String defaultValue) {
        return defaultValue;
    }

    @Override
    public final void serialize(JsonGenerator g, SerializerProvider provider) throws IOException, JsonProcessingException {
        g.writeNull();
    }

    @Override
    public void serializeWithType(JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
        g.writeNull();
    }

    public JsonNode require() {
        return (JsonNode)this._reportRequiredViolation("require() called on `MissingNode`", new Object[0]);
    }

    public JsonNode requireNonNull() {
        return (JsonNode)this._reportRequiredViolation("requireNonNull() called on `MissingNode`", new Object[0]);
    }

    @Override
    public int hashCode() {
        return JsonNodeType.MISSING.ordinal();
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public String toPrettyString() {
        return "";
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }
}

