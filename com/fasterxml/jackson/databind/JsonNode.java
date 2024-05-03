/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class JsonNode
extends JsonSerializable.Base
implements TreeNode,
Iterable<JsonNode> {
    protected JsonNode() {
    }

    public abstract <T extends JsonNode> T deepCopy();

    @Override
    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public final boolean isValueNode() {
        switch (this.getNodeType()) {
            case ARRAY: 
            case OBJECT: 
            case MISSING: {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean isContainerNode() {
        JsonNodeType type = this.getNodeType();
        return type == JsonNodeType.OBJECT || type == JsonNodeType.ARRAY;
    }

    @Override
    public boolean isMissingNode() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public abstract JsonNode get(int var1);

    @Override
    public JsonNode get(String fieldName) {
        return null;
    }

    @Override
    public abstract JsonNode path(String var1);

    @Override
    public abstract JsonNode path(int var1);

    @Override
    public Iterator<String> fieldNames() {
        return ClassUtil.emptyIterator();
    }

    @Override
    public final JsonNode at(JsonPointer ptr) {
        if (ptr.matches()) {
            return this;
        }
        JsonNode n = this._at(ptr);
        if (n == null) {
            return MissingNode.getInstance();
        }
        return n.at(ptr.tail());
    }

    @Override
    public final JsonNode at(String jsonPtrExpr) {
        return this.at(JsonPointer.compile(jsonPtrExpr));
    }

    protected abstract JsonNode _at(JsonPointer var1);

    public abstract JsonNodeType getNodeType();

    public final boolean isPojo() {
        return this.getNodeType() == JsonNodeType.POJO;
    }

    public final boolean isNumber() {
        return this.getNodeType() == JsonNodeType.NUMBER;
    }

    public boolean isIntegralNumber() {
        return false;
    }

    public boolean isFloatingPointNumber() {
        return false;
    }

    public boolean isShort() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isLong() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isDouble() {
        return false;
    }

    public boolean isBigDecimal() {
        return false;
    }

    public boolean isBigInteger() {
        return false;
    }

    public final boolean isTextual() {
        return this.getNodeType() == JsonNodeType.STRING;
    }

    public final boolean isBoolean() {
        return this.getNodeType() == JsonNodeType.BOOLEAN;
    }

    public final boolean isNull() {
        return this.getNodeType() == JsonNodeType.NULL;
    }

    public final boolean isBinary() {
        return this.getNodeType() == JsonNodeType.BINARY;
    }

    public boolean canConvertToInt() {
        return false;
    }

    public boolean canConvertToLong() {
        return false;
    }

    public String textValue() {
        return null;
    }

    public byte[] binaryValue() throws IOException {
        return null;
    }

    public boolean booleanValue() {
        return false;
    }

    public Number numberValue() {
        return null;
    }

    public short shortValue() {
        return 0;
    }

    public int intValue() {
        return 0;
    }

    public long longValue() {
        return 0L;
    }

    public float floatValue() {
        return 0.0f;
    }

    public double doubleValue() {
        return 0.0;
    }

    public BigDecimal decimalValue() {
        return BigDecimal.ZERO;
    }

    public BigInteger bigIntegerValue() {
        return BigInteger.ZERO;
    }

    public abstract String asText();

    public String asText(String defaultValue) {
        String str = this.asText();
        return str == null ? defaultValue : str;
    }

    public int asInt() {
        return this.asInt(0);
    }

    public int asInt(int defaultValue) {
        return defaultValue;
    }

    public long asLong() {
        return this.asLong(0L);
    }

    public long asLong(long defaultValue) {
        return defaultValue;
    }

    public double asDouble() {
        return this.asDouble(0.0);
    }

    public double asDouble(double defaultValue) {
        return defaultValue;
    }

    public boolean asBoolean() {
        return this.asBoolean(false);
    }

    public boolean asBoolean(boolean defaultValue) {
        return defaultValue;
    }

    public <T extends JsonNode> T require() throws IllegalArgumentException {
        return this._this();
    }

    public <T extends JsonNode> T requireNonNull() throws IllegalArgumentException {
        return this._this();
    }

    public JsonNode required(String fieldName) throws IllegalArgumentException {
        return (JsonNode)this._reportRequiredViolation("Node of type `%s` has no fields", this.getClass().getName());
    }

    public JsonNode required(int index) throws IllegalArgumentException {
        return (JsonNode)this._reportRequiredViolation("Node of type `%s` has no indexed values", this.getClass().getName());
    }

    public JsonNode requiredAt(String pathExpr) throws IllegalArgumentException {
        return this.requiredAt(JsonPointer.compile(pathExpr));
    }

    public final JsonNode requiredAt(JsonPointer path) throws IllegalArgumentException {
        JsonPointer currentExpr = path;
        JsonNode curr = this;
        while (!currentExpr.matches()) {
            if ((curr = curr._at(currentExpr)) == null) {
                this._reportRequiredViolation("No node at '%s' (unmatched part: '%s')", path, currentExpr);
            }
            currentExpr = currentExpr.tail();
        }
        return curr;
    }

    public boolean has(String fieldName) {
        return this.get(fieldName) != null;
    }

    public boolean has(int index) {
        return this.get(index) != null;
    }

    public boolean hasNonNull(String fieldName) {
        JsonNode n = this.get(fieldName);
        return n != null && !n.isNull();
    }

    public boolean hasNonNull(int index) {
        JsonNode n = this.get(index);
        return n != null && !n.isNull();
    }

    @Override
    public final Iterator<JsonNode> iterator() {
        return this.elements();
    }

    public Iterator<JsonNode> elements() {
        return ClassUtil.emptyIterator();
    }

    public Iterator<Map.Entry<String, JsonNode>> fields() {
        return ClassUtil.emptyIterator();
    }

    public abstract JsonNode findValue(String var1);

    public final List<JsonNode> findValues(String fieldName) {
        List<JsonNode> result = this.findValues(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public final List<String> findValuesAsText(String fieldName) {
        List<String> result = this.findValuesAsText(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public abstract JsonNode findPath(String var1);

    public abstract JsonNode findParent(String var1);

    public final List<JsonNode> findParents(String fieldName) {
        List<JsonNode> result = this.findParents(fieldName, null);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public abstract List<JsonNode> findValues(String var1, List<JsonNode> var2);

    public abstract List<String> findValuesAsText(String var1, List<String> var2);

    public abstract List<JsonNode> findParents(String var1, List<JsonNode> var2);

    public <T extends JsonNode> T with(String propertyName) {
        throw new UnsupportedOperationException("JsonNode not of type ObjectNode (but " + this.getClass().getName() + "), cannot call with() on it");
    }

    public <T extends JsonNode> T withArray(String propertyName) {
        throw new UnsupportedOperationException("JsonNode not of type ObjectNode (but " + this.getClass().getName() + "), cannot call withArray() on it");
    }

    public boolean equals(Comparator<JsonNode> comparator, JsonNode other) {
        return comparator.compare(this, other) == 0;
    }

    public abstract String toString();

    public String toPrettyString() {
        return this.toString();
    }

    public abstract boolean equals(Object var1);

    protected <T extends JsonNode> T _this() {
        return (T)this;
    }

    protected <T> T _reportRequiredViolation(String msgTemplate, Object ... args) {
        throw new IllegalArgumentException(String.format(msgTemplate, args));
    }
}

