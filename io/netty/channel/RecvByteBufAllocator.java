/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public interface RecvByteBufAllocator {
    public Handle newHandle();

    public static interface Handle {
        public ByteBuf allocate(ByteBufAllocator var1);

        public int guess();

        public void record(int var1);
    }
}

