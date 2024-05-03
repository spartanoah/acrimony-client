/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.DuplicatedByteBuf;

class UnpooledDuplicatedByteBuf
extends DuplicatedByteBuf {
    UnpooledDuplicatedByteBuf(AbstractByteBuf buffer) {
        super(buffer);
    }

    @Override
    public AbstractByteBuf unwrap() {
        return (AbstractByteBuf)super.unwrap();
    }

    @Override
    protected byte _getByte(int index) {
        return this.unwrap()._getByte(index);
    }

    @Override
    protected short _getShort(int index) {
        return this.unwrap()._getShort(index);
    }

    protected short _getShortLE(int index) {
        return this.unwrap()._getShortLE(index);
    }

    @Override
    protected int _getUnsignedMedium(int index) {
        return this.unwrap()._getUnsignedMedium(index);
    }

    protected int _getUnsignedMediumLE(int index) {
        return this.unwrap()._getUnsignedMediumLE(index);
    }

    @Override
    protected int _getInt(int index) {
        return this.unwrap()._getInt(index);
    }

    protected int _getIntLE(int index) {
        return this.unwrap()._getIntLE(index);
    }

    @Override
    protected long _getLong(int index) {
        return this.unwrap()._getLong(index);
    }

    protected long _getLongLE(int index) {
        return this.unwrap()._getLongLE(index);
    }

    @Override
    protected void _setByte(int index, int value) {
        this.unwrap()._setByte(index, value);
    }

    @Override
    protected void _setShort(int index, int value) {
        this.unwrap()._setShort(index, value);
    }

    protected void _setShortLE(int index, int value) {
        this.unwrap()._setShortLE(index, value);
    }

    @Override
    protected void _setMedium(int index, int value) {
        this.unwrap()._setMedium(index, value);
    }

    protected void _setMediumLE(int index, int value) {
        this.unwrap()._setMediumLE(index, value);
    }

    @Override
    protected void _setInt(int index, int value) {
        this.unwrap()._setInt(index, value);
    }

    protected void _setIntLE(int index, int value) {
        this.unwrap()._setIntLE(index, value);
    }

    @Override
    protected void _setLong(int index, long value) {
        this.unwrap()._setLong(index, value);
    }

    protected void _setLongLE(int index, long value) {
        this.unwrap()._setLongLE(index, value);
    }
}

