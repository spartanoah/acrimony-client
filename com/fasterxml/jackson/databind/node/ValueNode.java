/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;

public abstract class ValueNode
extends BaseJsonNode {
    private static final long serialVersionUID = 1L;

    protected ValueNode() {
    }

    @Override
    protected JsonNode _at(JsonPointer ptr) {
        return MissingNode.getInstance();
    }

    @Override
    public <T extends JsonNode> T deepCopy() {
        return (T)this;
    }

    @Override
    public abstract JsonToken asToken();

    @Override
    public void serializeWithType(JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(this, this.asToken()));
        this.serialize(g, provider);
        typeSer.writeTypeSuffix(g, typeIdDef);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public final JsonNode get(int index) {
        return null;
    }

    @Override
    public final JsonNode path(int index) {
        return MissingNode.getInstance();
    }

    @Override
    public final boolean has(int index) {
        return false;
    }

    @Override
    public final boolean hasNonNull(int index) {
        return false;
    }

    @Override
    public final JsonNode get(String fieldName) {
        return null;
    }

    @Override
    public final JsonNode path(String fieldName) {
        return MissingNode.getInstance();
    }

    @Override
    public final boolean has(String fieldName) {
        return false;
    }

    @Override
    public final boolean hasNonNull(String fieldName) {
        return false;
    }

    @Override
    public final JsonNode findValue(String fieldName) {
        return null;
    }

    @Override
    public final ObjectNode findParent(String fieldName) {
        return null;
    }

    @Override
    public final List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
        return foundSoFar;
    }

    @Override
    public final List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
        return foundSoFar;
    }

    @Override
    public final List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
        return foundSoFar;
    }
}

