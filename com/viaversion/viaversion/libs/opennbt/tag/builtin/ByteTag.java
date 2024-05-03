/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.limiter.TagLimiter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag
extends NumberTag {
    public static final int ID = 1;
    private byte value;

    public ByteTag() {
        this(0);
    }

    public ByteTag(byte value) {
        this.value = value;
    }

    public static ByteTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
        tagLimiter.countByte();
        return new ByteTag(in.readByte());
    }

    @Override
    @Deprecated
    public Byte getValue() {
        return this.value;
    }

    @Override
    public String asRawString() {
        return Byte.toString(this.value);
    }

    @Deprecated
    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeByte(this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ByteTag byteTag = (ByteTag)o;
        return this.value == byteTag.value;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(this.value);
    }

    @Override
    public byte asByte() {
        return this.value;
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
        return 1;
    }
}

