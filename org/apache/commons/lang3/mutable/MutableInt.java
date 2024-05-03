/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.mutable;

import org.apache.commons.lang3.mutable.Mutable;

public class MutableInt
extends Number
implements Comparable<MutableInt>,
Mutable<Number> {
    private static final long serialVersionUID = 512176391864L;
    private int value;

    public MutableInt() {
    }

    public MutableInt(int value) {
        this.value = value;
    }

    public MutableInt(Number value) {
        this.value = value.intValue();
    }

    public MutableInt(String value) throws NumberFormatException {
        this.value = Integer.parseInt(value);
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value.intValue();
    }

    public void increment() {
        ++this.value;
    }

    public void decrement() {
        --this.value;
    }

    public void add(int operand) {
        this.value += operand;
    }

    public void add(Number operand) {
        this.value += operand.intValue();
    }

    public void subtract(int operand) {
        this.value -= operand;
    }

    public void subtract(Number operand) {
        this.value -= operand.intValue();
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

    public Integer toInteger() {
        return this.intValue();
    }

    public boolean equals(Object obj) {
        if (obj instanceof MutableInt) {
            return this.value == ((MutableInt)obj).intValue();
        }
        return false;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public int compareTo(MutableInt other) {
        int anotherVal = other.value;
        return this.value < anotherVal ? -1 : (this.value == anotherVal ? 0 : 1);
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}

