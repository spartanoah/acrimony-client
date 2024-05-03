/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class NumericNode
extends ValueNode {
    private static final long serialVersionUID = 1L;

    protected NumericNode() {
    }

    @Override
    public final JsonNodeType getNodeType() {
        return JsonNodeType.NUMBER;
    }

    @Override
    public abstract JsonParser.NumberType numberType();

    @Override
    public abstract Number numberValue();

    @Override
    public abstract int intValue();

    @Override
    public abstract long longValue();

    @Override
    public abstract double doubleValue();

    @Override
    public abstract BigDecimal decimalValue();

    @Override
    public abstract BigInteger bigIntegerValue();

    @Override
    public abstract boolean canConvertToInt();

    @Override
    public abstract boolean canConvertToLong();

    @Override
    public abstract String asText();

    @Override
    public final int asInt() {
        return this.intValue();
    }

    @Override
    public final int asInt(int defaultValue) {
        return this.intValue();
    }

    @Override
    public final long asLong() {
        return this.longValue();
    }

    @Override
    public final long asLong(long defaultValue) {
        return this.longValue();
    }

    @Override
    public final double asDouble() {
        return this.doubleValue();
    }

    @Override
    public final double asDouble(double defaultValue) {
        return this.doubleValue();
    }

    public boolean isNaN() {
        return false;
    }
}

