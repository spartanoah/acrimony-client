/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.mutable;

import org.apache.commons.lang3.mutable.Mutable;

public class MutableDouble
extends Number
implements Comparable<MutableDouble>,
Mutable<Number> {
    private static final long serialVersionUID = 1587163916L;
    private double value;

    public MutableDouble() {
    }

    public MutableDouble(double value) {
        this.value = value;
    }

    public MutableDouble(Number value) {
        this.value = value.doubleValue();
    }

    public MutableDouble(String value) throws NumberFormatException {
        this.value = Double.parseDouble(value);
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value.doubleValue();
    }

    public boolean isNaN() {
        return Double.isNaN(this.value);
    }

    public boolean isInfinite() {
        return Double.isInfinite(this.value);
    }

    public void increment() {
        this.value += 1.0;
    }

    public void decrement() {
        this.value -= 1.0;
    }

    public void add(double operand) {
        this.value += operand;
    }

    public void add(Number operand) {
        this.value += operand.doubleValue();
    }

    public void subtract(double operand) {
        this.value -= operand;
    }

    public void subtract(Number operand) {
        this.value -= operand.doubleValue();
    }

    @Override
    public int intValue() {
        return (int)this.value;
    }

    @Override
    public long longValue() {
        return (long)this.value;
    }

    @Override
    public float floatValue() {
        return (float)this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    public Double toDouble() {
        return this.doubleValue();
    }

    public boolean equals(Object obj) {
        return obj instanceof MutableDouble && Double.doubleToLongBits(((MutableDouble)obj).value) == Double.doubleToLongBits(this.value);
    }

    public int hashCode() {
        long bits = Double.doubleToLongBits(this.value);
        return (int)(bits ^ bits >>> 32);
    }

    @Override
    public int compareTo(MutableDouble other) {
        double anotherVal = other.value;
        return Double.compare(this.value, anotherVal);
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}

