/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.NumberOutput;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.NumericNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class DoubleNode
extends NumericNode {
    protected final double _value;

    public DoubleNode(double v) {
        this._value = v;
    }

    public static DoubleNode valueOf(double v) {
        return new DoubleNode(v);
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_FLOAT;
    }

    @Override
    public JsonParser.NumberType numberType() {
        return JsonParser.NumberType.DOUBLE;
    }

    @Override
    public boolean isFloatingPointNumber() {
        return true;
    }

    @Override
    public boolean isDouble() {
        return true;
    }

    @Override
    public boolean canConvertToInt() {
        return this._value >= -2.147483648E9 && this._value <= 2.147483647E9;
    }

    @Override
    public boolean canConvertToLong() {
        return this._value >= -9.223372036854776E18 && this._value <= 9.223372036854776E18;
    }

    @Override
    public Number numberValue() {
        return this._value;
    }

    @Override
    public short shortValue() {
        return (short)this._value;
    }

    @Override
    public int intValue() {
        return (int)this._value;
    }

    @Override
    public long longValue() {
        return (long)this._value;
    }

    @Override
    public float floatValue() {
        return (float)this._value;
    }

    @Override
    public double doubleValue() {
        return this._value;
    }

    @Override
    public BigDecimal decimalValue() {
        return BigDecimal.valueOf(this._value);
    }

    @Override
    public BigInteger bigIntegerValue() {
        return this.decimalValue().toBigInteger();
    }

    @Override
    public String asText() {
        return NumberOutput.toString(this._value);
    }

    @Override
    public boolean isNaN() {
        return Double.isNaN(this._value) || Double.isInfinite(this._value);
    }

    @Override
    public final void serialize(JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeNumber(this._value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof DoubleNode) {
            double otherValue = ((DoubleNode)o)._value;
            return Double.compare(this._value, otherValue) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        long l = Double.doubleToLongBits(this._value);
        return (int)l ^ (int)(l >> 32);
    }
}

