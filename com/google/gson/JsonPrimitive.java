/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.gson;

import com.google.gson.JsonElement;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.LazilyParsedNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class JsonPrimitive
extends JsonElement {
    private static final Class<?>[] PRIMITIVE_TYPES = new Class[]{Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE, Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class};
    private Object value;

    public JsonPrimitive(Boolean bool) {
        this.setValue(bool);
    }

    public JsonPrimitive(Number number) {
        this.setValue(number);
    }

    public JsonPrimitive(String string) {
        this.setValue(string);
    }

    public JsonPrimitive(Character c) {
        this.setValue(c);
    }

    JsonPrimitive(Object primitive) {
        this.setValue(primitive);
    }

    JsonPrimitive deepCopy() {
        return this;
    }

    void setValue(Object primitive) {
        if (primitive instanceof Character) {
            char c = ((Character)primitive).charValue();
            this.value = String.valueOf(c);
        } else {
            $Gson$Preconditions.checkArgument(primitive instanceof Number || JsonPrimitive.isPrimitiveOrString(primitive));
            this.value = primitive;
        }
    }

    public boolean isBoolean() {
        return this.value instanceof Boolean;
    }

    Boolean getAsBooleanWrapper() {
        return (Boolean)this.value;
    }

    public boolean getAsBoolean() {
        if (this.isBoolean()) {
            return this.getAsBooleanWrapper();
        }
        return Boolean.parseBoolean(this.getAsString());
    }

    public boolean isNumber() {
        return this.value instanceof Number;
    }

    public Number getAsNumber() {
        return this.value instanceof String ? new LazilyParsedNumber((String)this.value) : (Number)this.value;
    }

    public boolean isString() {
        return this.value instanceof String;
    }

    public String getAsString() {
        if (this.isNumber()) {
            return this.getAsNumber().toString();
        }
        if (this.isBoolean()) {
            return this.getAsBooleanWrapper().toString();
        }
        return (String)this.value;
    }

    public double getAsDouble() {
        return this.isNumber() ? this.getAsNumber().doubleValue() : Double.parseDouble(this.getAsString());
    }

    public BigDecimal getAsBigDecimal() {
        return this.value instanceof BigDecimal ? (BigDecimal)this.value : new BigDecimal(this.value.toString());
    }

    public BigInteger getAsBigInteger() {
        return this.value instanceof BigInteger ? (BigInteger)this.value : new BigInteger(this.value.toString());
    }

    public float getAsFloat() {
        return this.isNumber() ? this.getAsNumber().floatValue() : Float.parseFloat(this.getAsString());
    }

    public long getAsLong() {
        return this.isNumber() ? this.getAsNumber().longValue() : Long.parseLong(this.getAsString());
    }

    public short getAsShort() {
        return this.isNumber() ? this.getAsNumber().shortValue() : Short.parseShort(this.getAsString());
    }

    public int getAsInt() {
        return this.isNumber() ? this.getAsNumber().intValue() : Integer.parseInt(this.getAsString());
    }

    public byte getAsByte() {
        return this.isNumber() ? this.getAsNumber().byteValue() : Byte.parseByte(this.getAsString());
    }

    public char getAsCharacter() {
        return this.getAsString().charAt(0);
    }

    private static boolean isPrimitiveOrString(Object target) {
        if (target instanceof String) {
            return true;
        }
        Class<?> classOfPrimitive = target.getClass();
        for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
            if (!standardPrimitive.isAssignableFrom(classOfPrimitive)) continue;
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (this.value == null) {
            return 31;
        }
        if (JsonPrimitive.isIntegral(this)) {
            long value = this.getAsNumber().longValue();
            return (int)(value ^ value >>> 32);
        }
        if (this.value instanceof Number) {
            long value = Double.doubleToLongBits(this.getAsNumber().doubleValue());
            return (int)(value ^ value >>> 32);
        }
        return this.value.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        JsonPrimitive other = (JsonPrimitive)obj;
        if (this.value == null) {
            return other.value == null;
        }
        if (JsonPrimitive.isIntegral(this) && JsonPrimitive.isIntegral(other)) {
            return this.getAsNumber().longValue() == other.getAsNumber().longValue();
        }
        if (this.value instanceof Number && other.value instanceof Number) {
            double b;
            double a = this.getAsNumber().doubleValue();
            return a == (b = other.getAsNumber().doubleValue()) || Double.isNaN(a) && Double.isNaN(b);
        }
        return this.value.equals(other.value);
    }

    private static boolean isIntegral(JsonPrimitive primitive) {
        if (primitive.value instanceof Number) {
            Number number = (Number)primitive.value;
            return number instanceof BigInteger || number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte;
        }
        return false;
    }
}

