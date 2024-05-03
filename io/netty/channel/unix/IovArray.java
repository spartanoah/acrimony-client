/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.unix.Buffer;
import io.netty.channel.unix.Limits;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IovArray
implements ChannelOutboundBuffer.MessageProcessor {
    private static final int ADDRESS_SIZE = Buffer.addressSize();
    public static final int IOV_SIZE = 2 * ADDRESS_SIZE;
    private static final int MAX_CAPACITY = Limits.IOV_MAX * IOV_SIZE;
    private final long memoryAddress;
    private final ByteBuf memory;
    private int count;
    private long size;
    private long maxBytes = Limits.SSIZE_MAX;

    public IovArray() {
        this(Unpooled.wrappedBuffer(Buffer.allocateDirectWithNativeOrder(MAX_CAPACITY)).setIndex(0, 0));
    }

    public IovArray(ByteBuf memory) {
        assert (memory.writerIndex() == 0);
        assert (memory.readerIndex() == 0);
        this.memory = PlatformDependent.hasUnsafe() ? memory : memory.order(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        this.memoryAddress = memory.hasMemoryAddress() ? memory.memoryAddress() : Buffer.memoryAddress(memory.internalNioBuffer(0, memory.capacity()));
    }

    public void clear() {
        this.count = 0;
        this.size = 0L;
    }

    @Deprecated
    public boolean add(ByteBuf buf) {
        return this.add(buf, buf.readerIndex(), buf.readableBytes());
    }

    public boolean add(ByteBuf buf, int offset, int len) {
        ByteBuffer[] buffers;
        if (this.count == Limits.IOV_MAX) {
            return false;
        }
        if (buf.nioBufferCount() == 1) {
            if (len == 0) {
                return true;
            }
            if (buf.hasMemoryAddress()) {
                return this.add(this.memoryAddress, buf.memoryAddress() + (long)offset, len);
            }
            ByteBuffer nioBuffer = buf.internalNioBuffer(offset, len);
            return this.add(this.memoryAddress, Buffer.memoryAddress(nioBuffer) + (long)nioBuffer.position(), len);
        }
        for (ByteBuffer nioBuffer : buffers = buf.nioBuffers(offset, len)) {
            int remaining = nioBuffer.remaining();
            if (remaining == 0 || this.add(this.memoryAddress, Buffer.memoryAddress(nioBuffer) + (long)nioBuffer.position(), remaining) && this.count != Limits.IOV_MAX) continue;
            return false;
        }
        return true;
    }

    private boolean add(long memoryAddress, long addr, int len) {
        assert (addr != 0L);
        if (this.maxBytes - (long)len < this.size && this.count > 0 || this.memory.capacity() < (this.count + 1) * IOV_SIZE) {
            return false;
        }
        int baseOffset = IovArray.idx(this.count);
        int lengthOffset = baseOffset + ADDRESS_SIZE;
        this.size += (long)len;
        ++this.count;
        if (ADDRESS_SIZE == 8) {
            if (PlatformDependent.hasUnsafe()) {
                PlatformDependent.putLong((long)baseOffset + memoryAddress, addr);
                PlatformDependent.putLong((long)lengthOffset + memoryAddress, len);
            } else {
                this.memory.setLong(baseOffset, addr);
                this.memory.setLong(lengthOffset, len);
            }
        } else {
            assert (ADDRESS_SIZE == 4);
            if (PlatformDependent.hasUnsafe()) {
                PlatformDependent.putInt((long)baseOffset + memoryAddress, (int)addr);
                PlatformDependent.putInt((long)lengthOffset + memoryAddress, len);
            } else {
                this.memory.setInt(baseOffset, (int)addr);
                this.memory.setInt(lengthOffset, len);
            }
        }
        return true;
    }

    public int count() {
        return this.count;
    }

    public long size() {
        return this.size;
    }

    public void maxBytes(long maxBytes) {
        this.maxBytes = Math.min(Limits.SSIZE_MAX, ObjectUtil.checkPositive(maxBytes, "maxBytes"));
    }

    public long maxBytes() {
        return this.maxBytes;
    }

    public long memoryAddress(int offset) {
        return this.memoryAddress + (long)IovArray.idx(offset);
    }

    public void release() {
        this.memory.release();
    }

    @Override
    public boolean processMessage(Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf)msg;
            return this.add(buffer, buffer.readerIndex(), buffer.readableBytes());
        }
        return false;
    }

    private static int idx(int index) {
        return IOV_SIZE * index;
    }
}

