/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

class UnpooledUnsafeNoCleanerDirectByteBuf
extends UnpooledUnsafeDirectByteBuf {
    UnpooledUnsafeNoCleanerDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
        super(alloc, initialCapacity, maxCapacity);
    }

    @Override
    protected ByteBuffer allocateDirect(int initialCapacity) {
        return PlatformDependent.allocateDirectNoCleaner((int)initialCapacity);
    }

    ByteBuffer reallocateDirect(ByteBuffer oldBuffer, int initialCapacity) {
        return PlatformDependent.reallocateDirectNoCleaner((ByteBuffer)oldBuffer, (int)initialCapacity);
    }

    @Override
    protected void freeDirect(ByteBuffer buffer) {
        PlatformDependent.freeDirectNoCleaner((ByteBuffer)buffer);
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        this.checkNewCapacity(newCapacity);
        int oldCapacity = this.capacity();
        if (newCapacity == oldCapacity) {
            return this;
        }
        this.trimIndicesToCapacity(newCapacity);
        this.setByteBuffer(this.reallocateDirect(this.buffer, newCapacity), false);
        return this;
    }
}

