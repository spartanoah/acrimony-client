/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag
extends NumberTag {
    public static final int ID = 2;
    private short value;

    public ShortTag() {
        this(0);
    }

    public ShortTag(short value) {
        this.value = value;
    }

    public static ShortTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
        tagLimiter.countShort();
        return new ShortTag(in.readShort());
    }

    @Override
    @Deprecated
    public Short getValue() {
        return this.value;
    }

    @Override
    public String asRawString() {
        return Short.toString(this.value);
    }

    @Deprecated
    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeShort(this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ShortTag shortTag = (ShortTag)o;
        return this.value == shortTag.value;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public ShortTag copy() {
        return new ShortTag(this.value);
    }

    @Override
    public byte asByte() {
        return (byte)this.value;
    }

    @Override
    public short asShort() {
        return this.value;
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
        return 2;
    }
}

