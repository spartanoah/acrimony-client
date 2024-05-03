/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.mutable;

import org.apache.commons.lang3.mutable.Mutable;

public class MutableShort
extends Number
implements Comparable<MutableShort>,
Mutable<Number> {
    private static final long serialVersionUID = -2135791679L;
    private short value;

    public MutableShort() {
    }

    public MutableShort(short value) {
        this.value = value;
    }

    public MutableShort(Number value) {
        this.value = value.shortValue();
    }

    public MutableShort(String value) throws NumberFormatException {
        this.value = Short.parseShort(value);
    }

    @Override
    public Short getValue() {
        return this.value;
    }

    @Override
    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value.shortValue();
    }

    public void increment() {
        this.value = (short)(this.value + 1);
    }

    public void decrement() {
        this.value = (short)(this.value - 1);
    }

    public void add(short operand) {
        this.value = (short)(this.value + operand);
    }

    public void add(Number operand) {
        this.value = (short)(this.value + operand.shortValue());
    }

    public void subtract(short operand) {
        this.value = (short)(this.value - operand);
    }

    public void subtract(Number operand) {
        this.value = (short)(this.value - operand.shortValue());
    }

    @Override
    public short shortValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    public Short toShort() {
        return this.shortValue();
    }

    public boolean equals(Object obj) {
        if (obj instanceof MutableShort) {
            return this.value == ((MutableShort)obj).shortValue();
        }
        return false;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public int compareTo(MutableShort other) {
        short anotherVal = other.value;
        return this.value < anotherVal ? -1 : (this.value == anotherVal ? 0 : 1);
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}

