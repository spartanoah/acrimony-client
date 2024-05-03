/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag
extends NumberTag {
    public static final int ID = 3;
    private int value;

    public IntTag() {
        this(0);
    }

    public IntTag(int value) {
        this.value = value;
    }

    public static IntTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
        tagLimiter.countInt();
        return new IntTag(in.readInt());
    }

    @Override
    @Deprecated
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String asRawString() {
        return Integer.toString(this.value);
    }

    @Deprecated
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        IntTag intTag = (IntTag)o;
        return this.value == intTag.value;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public IntTag copy() {
        return new IntTag(this.value);
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
        return this.value;
    }

    @Override
    public long asLong() {
        return this.value;
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
        return 3;
    }
}

