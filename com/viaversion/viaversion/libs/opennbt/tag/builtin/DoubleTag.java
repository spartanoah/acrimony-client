/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag
extends NumberTag {
    public static final int ID = 6;
    private double value;

    public DoubleTag() {
        this(0.0);
    }

    public DoubleTag(double value) {
        this.value = value;
    }

    public static DoubleTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
        tagLimiter.countDouble();
        return new DoubleTag(in.readDouble());
    }

    @Override
    @Deprecated
    public Double getValue() {
        return this.value;
    }

    @Override
    public String asRawString() {
        return Double.toString(this.value);
    }

    @Deprecated
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DoubleTag doubleTag = (DoubleTag)o;
        return this.value == doubleTag.value;
    }

    public int hashCode() {
        return Double.hashCode(this.value);
    }

    @Override
    public DoubleTag copy() {
        return new DoubleTag(this.value);
    }

    @Override
    public byte asByte() {
        return (byte)this.value;
    }

    @Override
    public short asShort() {
        return (short)this.value;
    }

    @Override
    public int asInt() {
        return (int)this.value;
    }

    @Override
    public long asLong() {
        return (long)this.value;
    }

    @Override
    public float asFloat() {
        return (float)this.value;
    }

    @Override
    public double asDouble() {
        return this.value;
    }

    @Override
    public int getTagId() {
        return 6;
    }
}

