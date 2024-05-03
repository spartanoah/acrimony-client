/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.mutable;

import org.apache.commons.lang3.mutable.Mutable;

public class MutableLong
extends Number
implements Comparable<MutableLong>,
Mutable<Number> {
    private static final long serialVersionUID = 62986528375L;
    private long value;

    public MutableLong() {
    }

    public MutableLong(long value) {
        this.value = value;
    }

    public MutableLong(Number value) {
        this.value = value.longValue();
    }

    public MutableLong(String value) throws NumberFormatException {
        this.value = Long.parseLong(value);
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    @Override
    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value.longValue();
    }

    public void increment() {
        ++this.value;
    }

    public void decrement() {
        --this.value;
    }

    public void add(long operand) {
        this.value += operand;
    }

    public void add(Number operand) {
        this.value += operand.longValue();
    }

    public void subtract(long operand) {
        this.value -= operand;
    }

    public void subtract(Number operand) {
        this.value -= operand.longValue();
    }

    @Override
    public int intValue() {
        return (int)this.value;
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

    public Long toLong() {
        return this.longValue();
    }

    public boolean equals(Object obj) {
        if (obj instanceof MutableLong) {
            return this.value == ((MutableLong)obj).longValue();
        }
        return false;
    }

    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }

    @Override
    public int compareTo(MutableLong other) {
        long anotherVal = other.value;
        return this.value < anotherVal ? -1 : (this.value == anotherVal ? 0 : 1);
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}

