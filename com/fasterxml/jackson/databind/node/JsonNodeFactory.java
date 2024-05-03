/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonNodeFactory
implements Serializable,
JsonNodeCreator {
    private static final long serialVersionUID = 1L;
    private final boolean _cfgBigDecimalExact;
    private static final JsonNodeFactory decimalsNormalized = new JsonNodeFactory(false);
    private static final JsonNodeFactory decimalsAsIs = new JsonNodeFactory(true);
    public static final JsonNodeFactory instance = decimalsNormalized;

    public JsonNodeFactory(boolean bigDecimalExact) {
        this._cfgBigDecimalExact = bigDecimalExact;
    }

    protected JsonNodeFactory() {
        this(false);
    }

    public static JsonNodeFactory withExactBigDecimals(boolean bigDecimalExact) {
        return bigDecimalExact ? decimalsAsIs : decimalsNormalized;
    }

    @Override
    public BooleanNode booleanNode(boolean v) {
        return v ? BooleanNode.getTrue() : BooleanNode.getFalse();
    }

    @Override
    public NullNode nullNode() {
        return NullNode.getInstance();
    }

    public JsonNode missingNode() {
        return MissingNode.getInstance();
    }

    @Override
    public NumericNode numberNode(byte v) {
        return IntNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(Byte value) {
        return value == null ? this.nullNode() : IntNode.valueOf(value.intValue());
    }

    @Override
    public NumericNode numberNode(short v) {
        return ShortNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(Short value) {
        return value == null ? this.nullNode() : ShortNode.valueOf(value);
    }

    @Override
    public NumericNode numberNode(int v) {
        return IntNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(Integer value) {
        return value == null ? this.nullNode() : IntNode.valueOf(value);
    }

    @Override
    public NumericNode numberNode(long v) {
        return LongNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(Long v) {
        if (v == null) {
            return this.nullNode();
        }
        return LongNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(BigInteger v) {
        if (v == null) {
            return this.nullNode();
        }
        return BigIntegerNode.valueOf(v);
    }

    @Override
    public NumericNode numberNode(float v) {
        return FloatNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(Float value) {
        return value == null ? this.nullNode() : FloatNode.valueOf(value.floatValue());
    }

    @Override
    public NumericNode numberNode(double v) {
        return DoubleNode.valueOf(v);
    }

    @Override
    public ValueNode numberNode(Double value) {
        return value == null ? this.nullNode() : DoubleNode.valueOf(value);
    }

    @Override
    public ValueNode numberNode(BigDecimal v) {
        if (v == null) {
            return this.nullNode();
        }
        if (this._cfgBigDecimalExact) {
            return DecimalNode.valueOf(v);
        }
        return v.compareTo(BigDecimal.ZERO) == 0 ? DecimalNode.ZERO : DecimalNode.valueOf(v.stripTrailingZeros());
    }

    @Override
    public TextNode textNode(String text) {
        return TextNode.valueOf(text);
    }

    @Override
    public BinaryNode binaryNode(byte[] data) {
        return BinaryNode.valueOf(data);
    }

    @Override
    public BinaryNode binaryNode(byte[] data, int offset, int length) {
        return BinaryNode.valueOf(data, offset, length);
    }

    @Override
    public ArrayNode arrayNode() {
        return new ArrayNode(this);
    }

    @Override
    public ArrayNode arrayNode(int capacity) {
        return new ArrayNode(this, capacity);
    }

    @Override
    public ObjectNode objectNode() {
        return new ObjectNode(this);
    }

    @Override
    public ValueNode pojoNode(Object pojo) {
        return new POJONode(pojo);
    }

    @Override
    public ValueNode rawValueNode(RawValue value) {
        return new POJONode(value);
    }

    protected boolean _inIntRange(long l) {
        int i = (int)l;
        long l2 = i;
        return l2 == l;
    }
}

