/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag
extends NumberTag {
    public static final int ID = 5;
    private float value;

    public FloatTag() {
        this(0.0f);
    }

    public FloatTag(float value) {
        this.value = value;
    }

    public static FloatTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
        tagLimiter.countFloat();
        return new FloatTag(in.readFloat());
    }

    @Override
    @Deprecated
    public Float getValue() {
        return Float.valueOf(this.value);
    }

    @Override
    public String asRawString() {
        return Float.toString(this.value);
    }

    @Deprecated
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeFloat(this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FloatTag floatTag = (FloatTag)o;
        return this.value == floatTag.value;
    }

    public int hashCode() {
        return Float.hashCode(this.value);
    }

    @Override
    public FloatTag copy() {
        return new FloatTag(this.value);
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
        return this.value;
    }

    @Override
    public double asDouble() {
        return this.value;
    }

    @Override
    public int getTagId() {
        return 5;
    }
}

