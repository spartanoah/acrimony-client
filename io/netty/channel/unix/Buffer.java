/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import io.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Buffer {
    private Buffer() {
    }

    public static void free(ByteBuffer buffer) {
        PlatformDependent.freeDirectBuffer(buffer);
    }

    public static ByteBuffer allocateDirectWithNativeOrder(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }

    public static long memoryAddress(ByteBuffer buffer) {
        assert (buffer.isDirect());
        if (PlatformDependent.hasUnsafe()) {
            return PlatformDependent.directBufferAddress(buffer);
        }
        return Buffer.memoryAddress0(buffer);
    }

    public static int addressSize() {
        if (PlatformDependent.hasUnsafe()) {
            return PlatformDependent.addressSize();
        }
        return Buffer.addressSize0();
    }

    private static native int addressSize0();

    private static native long memoryAddress0(ByteBuffer var0);
}

