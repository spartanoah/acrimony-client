/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.epoll;

import io.netty.channel.epoll.Native;
import io.netty.channel.unix.Buffer;
import io.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

final class EpollEventArray {
    private static final int EPOLL_EVENT_SIZE = Native.sizeofEpollEvent();
    private static final int EPOLL_DATA_OFFSET = Native.offsetofEpollData();
    private ByteBuffer memory;
    private long memoryAddress;
    private int length;

    EpollEventArray(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be >= 1 but was " + length);
        }
        this.length = length;
        this.memory = Buffer.allocateDirectWithNativeOrder(EpollEventArray.calculateBufferCapacity(length));
        this.memoryAddress = Buffer.memoryAddress(this.memory);
    }

    long memoryAddress() {
        return this.memoryAddress;
    }

    int length() {
        return this.length;
    }

    void increase() {
        this.length <<= 1;
        ByteBuffer buffer = Buffer.allocateDirectWithNativeOrder(EpollEventArray.calculateBufferCapacity(this.length));
        Buffer.free(this.memory);
        this.memory = buffer;
        this.memoryAddress = Buffer.memoryAddress(buffer);
    }

    void free() {
        Buffer.free(this.memory);
        this.memoryAddress = 0L;
    }

    int events(int index) {
        return this.getInt(index, 0);
    }

    int fd(int index) {
        return this.getInt(index, EPOLL_DATA_OFFSET);
    }

    private int getInt(int index, int offset) {
        if (PlatformDependent.hasUnsafe()) {
            long n = (long)index * (long)EPOLL_EVENT_SIZE;
            return PlatformDependent.getInt(this.memoryAddress + n + (long)offset);
        }
        return this.memory.getInt(index * EPOLL_EVENT_SIZE + offset);
    }

    private static int calculateBufferCapacity(int capacity) {
        return capacity * EPOLL_EVENT_SIZE;
    }
}

