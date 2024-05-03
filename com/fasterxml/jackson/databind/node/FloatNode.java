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

public class FloatNode
extends NumericNode {
    protected final float _value;

    public FloatNode(float v) {
        this._value = v;
    }

    public static FloatNode valueOf(float v) {
        return new FloatNode(v);
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.VALUE_NUMBER_FLOAT;
    }

    @Override
    public JsonParser.NumberType numberType() {
        return JsonParser.NumberType.FLOAT;
    }

    @Override
    public boolean isFloatingPointNumber() {
        return true;
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public boolean canConvertToInt() {
        return this._value >= -2.14748365E9f && this._value <= 2.14748365E9f;
    }

    @Override
    public boolean canConvertToLong() {
        return this._value >= -9.223372E18f && this._value <= 9.223372E18f;
    }

    @Override
    public Number numberValue() {
        return Float.valueOf(this._value);
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
        return this._value;
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
        return Float.isNaN(this._value) || Float.isInfinite(this._value);
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
        if (o instanceof FloatNode) {
            float otherValue = ((FloatNode)o)._value;
            return Float.compare(this._value, otherValue) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(this._value);
    }
}

