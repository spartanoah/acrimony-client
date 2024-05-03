/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag
extends NumberTag {
    public static final int ID = 4;
    private long value;

    public LongTag() {
        this(0L);
    }

    public LongTag(long value) {
        this.value = value;
    }

    public static LongTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
        tagLimiter.countLong();
        return new LongTag(in.readLong());
    }

    @Override
    @Deprecated
    public Long getValue() {
        return this.value;
    }

    @Override
    public String asRawString() {
        return Long.toString(this.value);
    }

    @Deprecated
    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        LongTag longTag = (LongTag)o;
        return this.value == longTag.value;
    }

    public int hashCode() {
        return Long.hashCode(this.value);
    }

    @Override
    public LongTag copy() {
        return new LongTag(this.value);
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
        return 4;
    }
}

