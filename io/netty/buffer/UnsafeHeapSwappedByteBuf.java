/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.AbstractUnsafeSwappedByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.PlatformDependent;

final class UnsafeHeapSwappedByteBuf
extends AbstractUnsafeSwappedByteBuf {
    UnsafeHeapSwappedByteBuf(AbstractByteBuf buf) {
        super(buf);
    }

    private static int idx(ByteBuf wrapped, int index) {
        return wrapped.arrayOffset() + index;
    }

    @Override
    protected long _getLong(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getLong((byte[])wrapped.array(), (int)UnsafeHeapSwappedByteBuf.idx(wrapped, index));
    }

    @Override
    protected int _getInt(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getInt((byte[])wrapped.array(), (int)UnsafeHeapSwappedByteBuf.idx(wrapped, index));
    }

    @Override
    protected short _getShort(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getShort((byte[])wrapped.array(), (int)UnsafeHeapSwappedByteBuf.idx(wrapped, index));
    }

    @Override
    protected void _setShort(AbstractByteBuf wrapped, int index, short value) {
        PlatformDependent.putShort((byte[])wrapped.array(), (int)UnsafeHeapSwappedByteBuf.idx(wrapped, index), (short)value);
    }

    @Override
    protected void _setInt(AbstractByteBuf wrapped, int index, int value) {
        PlatformDependent.putInt((byte[])wrapped.array(), (int)UnsafeHeapSwappedByteBuf.idx(wrapped, index), (int)value);
    }

    @Override
    protected void _setLong(AbstractByteBuf wrapped, int index, long value) {
        PlatformDependent.putLong((byte[])wrapped.array(), (int)UnsafeHeapSwappedByteBuf.idx(wrapped, index), (long)value);
    }
}

