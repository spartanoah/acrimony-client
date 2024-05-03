/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.buffer.UnpooledUnsafeNoCleanerDirectByteBuf;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

final class UnsafeByteBufUtil {
    private static final boolean UNALIGNED = PlatformDependent.isUnaligned();
    private static final byte ZERO = 0;

    static byte getByte(long address) {
        return PlatformDependent.getByte(address);
    }

    static short getShort(long address) {
        if (UNALIGNED) {
            short v = PlatformDependent.getShort(address);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Short.reverseBytes(v);
        }
        return (short)(PlatformDependent.getByte(address) << 8 | PlatformDependent.getByte(address + 1L) & 0xFF);
    }

    static short getShortLE(long address) {
        if (UNALIGNED) {
            short v = PlatformDependent.getShort(address);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(v) : v;
        }
        return (short)(PlatformDependent.getByte(address) & 0xFF | PlatformDependent.getByte(address + 1L) << 8);
    }

    static int getUnsignedMedium(long address) {
        if (UNALIGNED) {
            return (PlatformDependent.getByte(address) & 0xFF) << 16 | (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? PlatformDependent.getShort(address + 1L) : Short.reverseBytes(PlatformDependent.getShort(address + 1L))) & 0xFFFF;
        }
        return (PlatformDependent.getByte(address) & 0xFF) << 16 | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | PlatformDependent.getByte(address + 2L) & 0xFF;
    }

    static int getUnsignedMediumLE(long address) {
        if (UNALIGNED) {
            return PlatformDependent.getByte(address) & 0xFF | ((PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(PlatformDependent.getShort(address + 1L)) : PlatformDependent.getShort(address + 1L)) & 0xFFFF) << 8;
        }
        return PlatformDependent.getByte(address) & 0xFF | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 16;
    }

    static int getInt(long address) {
        if (UNALIGNED) {
            int v = PlatformDependent.getInt(address);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
        }
        return PlatformDependent.getByte(address) << 24 | (PlatformDependent.getByte(address + 1L) & 0xFF) << 16 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 8 | PlatformDependent.getByte(address + 3L) & 0xFF;
    }

    static int getIntLE(long address) {
        if (UNALIGNED) {
            int v = PlatformDependent.getInt(address);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(v) : v;
        }
        return PlatformDependent.getByte(address) & 0xFF | (PlatformDependent.getByte(address + 1L) & 0xFF) << 8 | (PlatformDependent.getByte(address + 2L) & 0xFF) << 16 | PlatformDependent.getByte(address + 3L) << 24;
    }

    static long getLong(long address) {
        if (UNALIGNED) {
            long v = PlatformDependent.getLong(address);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Long.reverseBytes(v);
        }
        return (long)PlatformDependent.getByte(address) << 56 | ((long)PlatformDependent.getByte(address + 1L) & 0xFFL) << 48 | ((long)PlatformDependent.getByte(address + 2L) & 0xFFL) << 40 | ((long)PlatformDependent.getByte(address + 3L) & 0xFFL) << 32 | ((long)PlatformDependent.getByte(address + 4L) & 0xFFL) << 24 | ((long)PlatformDependent.getByte(address + 5L) & 0xFFL) << 16 | ((long)PlatformDependent.getByte(address + 6L) & 0xFFL) << 8 | (long)PlatformDependent.getByte(address + 7L) & 0xFFL;
    }

    static long getLongLE(long address) {
        if (UNALIGNED) {
            long v = PlatformDependent.getLong(address);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(v) : v;
        }
        return (long)PlatformDependent.getByte(address) & 0xFFL | ((long)PlatformDependent.getByte(address + 1L) & 0xFFL) << 8 | ((long)PlatformDependent.getByte(address + 2L) & 0xFFL) << 16 | ((long)PlatformDependent.getByte(address + 3L) & 0xFFL) << 24 | ((long)PlatformDependent.getByte(address + 4L) & 0xFFL) << 32 | ((long)PlatformDependent.getByte(address + 5L) & 0xFFL) << 40 | ((long)PlatformDependent.getByte(address + 6L) & 0xFFL) << 48 | (long)PlatformDependent.getByte(address + 7L) << 56;
    }

    static void setByte(long address, int value) {
        PlatformDependent.putByte(address, (byte)value);
    }

    static void setShort(long address, int value) {
        if (UNALIGNED) {
            PlatformDependent.putShort(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value));
        } else {
            PlatformDependent.putByte(address, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 1L, (byte)value);
        }
    }

    static void setShortLE(long address, int value) {
        if (UNALIGNED) {
            PlatformDependent.putShort(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes((short)value) : (short)value);
        } else {
            PlatformDependent.putByte(address, (byte)value);
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
        }
    }

    static void setMedium(long address, int value) {
        PlatformDependent.putByte(address, (byte)(value >>> 16));
        if (UNALIGNED) {
            PlatformDependent.putShort(address + 1L, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value));
        } else {
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 2L, (byte)value);
        }
    }

    static void setMediumLE(long address, int value) {
        PlatformDependent.putByte(address, (byte)value);
        if (UNALIGNED) {
            PlatformDependent.putShort(address + 1L, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes((short)(value >>> 8)) : (short)(value >>> 8));
        } else {
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 2L, (byte)(value >>> 16));
        }
    }

    static void setInt(long address, int value) {
        if (UNALIGNED) {
            PlatformDependent.putInt(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value));
        } else {
            PlatformDependent.putByte(address, (byte)(value >>> 24));
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 16));
            PlatformDependent.putByte(address + 2L, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 3L, (byte)value);
        }
    }

    static void setIntLE(long address, int value) {
        if (UNALIGNED) {
            PlatformDependent.putInt(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(value) : value);
        } else {
            PlatformDependent.putByte(address, (byte)value);
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 2L, (byte)(value >>> 16));
            PlatformDependent.putByte(address + 3L, (byte)(value >>> 24));
        }
    }

    static void setLong(long address, long value) {
        if (UNALIGNED) {
            PlatformDependent.putLong(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Long.reverseBytes(value));
        } else {
            PlatformDependent.putByte(address, (byte)(value >>> 56));
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 48));
            PlatformDependent.putByte(address + 2L, (byte)(value >>> 40));
            PlatformDependent.putByte(address + 3L, (byte)(value >>> 32));
            PlatformDependent.putByte(address + 4L, (byte)(value >>> 24));
            PlatformDependent.putByte(address + 5L, (byte)(value >>> 16));
            PlatformDependent.putByte(address + 6L, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 7L, (byte)value);
        }
    }

    static void setLongLE(long address, long value) {
        if (UNALIGNED) {
            PlatformDependent.putLong(address, PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(value) : value);
        } else {
            PlatformDependent.putByte(address, (byte)value);
            PlatformDependent.putByte(address + 1L, (byte)(value >>> 8));
            PlatformDependent.putByte(address + 2L, (byte)(value >>> 16));
            PlatformDependent.putByte(address + 3L, (byte)(value >>> 24));
            PlatformDependent.putByte(address + 4L, (byte)(value >>> 32));
            PlatformDependent.putByte(address + 5L, (byte)(value >>> 40));
            PlatformDependent.putByte(address + 6L, (byte)(value >>> 48));
            PlatformDependent.putByte(address + 7L, (byte)(value >>> 56));
        }
    }

    static byte getByte(byte[] array, int index) {
        return PlatformDependent.getByte((byte[])array, (int)index);
    }

    static short getShort(byte[] array, int index) {
        if (UNALIGNED) {
            short v = PlatformDependent.getShort((byte[])array, (int)index);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Short.reverseBytes(v);
        }
        return (short)(PlatformDependent.getByte((byte[])array, (int)index) << 8 | PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFF);
    }

    static short getShortLE(byte[] array, int index) {
        if (UNALIGNED) {
            short v = PlatformDependent.getShort((byte[])array, (int)index);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(v) : v;
        }
        return (short)(PlatformDependent.getByte((byte[])array, (int)index) & 0xFF | PlatformDependent.getByte((byte[])array, (int)(index + 1)) << 8);
    }

    static int getUnsignedMedium(byte[] array, int index) {
        if (UNALIGNED) {
            return (PlatformDependent.getByte((byte[])array, (int)index) & 0xFF) << 16 | (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? PlatformDependent.getShort((byte[])array, (int)(index + 1)) : Short.reverseBytes(PlatformDependent.getShort((byte[])array, (int)(index + 1)))) & 0xFFFF;
        }
        return (PlatformDependent.getByte((byte[])array, (int)index) & 0xFF) << 16 | (PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFF) << 8 | PlatformDependent.getByte((byte[])array, (int)(index + 2)) & 0xFF;
    }

    static int getUnsignedMediumLE(byte[] array, int index) {
        if (UNALIGNED) {
            return PlatformDependent.getByte((byte[])array, (int)index) & 0xFF | ((PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes(PlatformDependent.getShort((byte[])array, (int)(index + 1))) : PlatformDependent.getShort((byte[])array, (int)(index + 1))) & 0xFFFF) << 8;
        }
        return PlatformDependent.getByte((byte[])array, (int)index) & 0xFF | (PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFF) << 8 | (PlatformDependent.getByte((byte[])array, (int)(index + 2)) & 0xFF) << 16;
    }

    static int getInt(byte[] array, int index) {
        if (UNALIGNED) {
            int v = PlatformDependent.getInt((byte[])array, (int)index);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Integer.reverseBytes(v);
        }
        return PlatformDependent.getByte((byte[])array, (int)index) << 24 | (PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFF) << 16 | (PlatformDependent.getByte((byte[])array, (int)(index + 2)) & 0xFF) << 8 | PlatformDependent.getByte((byte[])array, (int)(index + 3)) & 0xFF;
    }

    static int getIntLE(byte[] array, int index) {
        if (UNALIGNED) {
            int v = PlatformDependent.getInt((byte[])array, (int)index);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(v) : v;
        }
        return PlatformDependent.getByte((byte[])array, (int)index) & 0xFF | (PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFF) << 8 | (PlatformDependent.getByte((byte[])array, (int)(index + 2)) & 0xFF) << 16 | PlatformDependent.getByte((byte[])array, (int)(index + 3)) << 24;
    }

    static long getLong(byte[] array, int index) {
        if (UNALIGNED) {
            long v = PlatformDependent.getLong((byte[])array, (int)index);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? v : Long.reverseBytes(v);
        }
        return (long)PlatformDependent.getByte((byte[])array, (int)index) << 56 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFFL) << 48 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 2)) & 0xFFL) << 40 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 3)) & 0xFFL) << 32 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 4)) & 0xFFL) << 24 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 5)) & 0xFFL) << 16 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 6)) & 0xFFL) << 8 | (long)PlatformDependent.getByte((byte[])array, (int)(index + 7)) & 0xFFL;
    }

    static long getLongLE(byte[] array, int index) {
        if (UNALIGNED) {
            long v = PlatformDependent.getLong((byte[])array, (int)index);
            return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(v) : v;
        }
        return (long)PlatformDependent.getByte((byte[])array, (int)index) & 0xFFL | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 1)) & 0xFFL) << 8 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 2)) & 0xFFL) << 16 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 3)) & 0xFFL) << 24 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 4)) & 0xFFL) << 32 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 5)) & 0xFFL) << 40 | ((long)PlatformDependent.getByte((byte[])array, (int)(index + 6)) & 0xFFL) << 48 | (long)PlatformDependent.getByte((byte[])array, (int)(index + 7)) << 56;
    }

    static void setByte(byte[] array, int index, int value) {
        PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)value));
    }

    static void setShort(byte[] array, int index, int value) {
        if (UNALIGNED) {
            PlatformDependent.putShort((byte[])array, (int)index, (short)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value)));
        } else {
            PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)value));
        }
    }

    static void setShortLE(byte[] array, int index, int value) {
        if (UNALIGNED) {
            PlatformDependent.putShort((byte[])array, (int)index, (short)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes((short)value) : (short)value));
        } else {
            PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)value));
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 8)));
        }
    }

    static void setMedium(byte[] array, int index, int value) {
        PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)(value >>> 16)));
        if (UNALIGNED) {
            PlatformDependent.putShort((byte[])array, (int)(index + 1), (short)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value)));
        } else {
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 2), (byte)((byte)value));
        }
    }

    static void setMediumLE(byte[] array, int index, int value) {
        PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)value));
        if (UNALIGNED) {
            PlatformDependent.putShort((byte[])array, (int)(index + 1), (short)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Short.reverseBytes((short)(value >>> 8)) : (short)(value >>> 8)));
        } else {
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 2), (byte)((byte)(value >>> 16)));
        }
    }

    static void setInt(byte[] array, int index, int value) {
        if (UNALIGNED) {
            PlatformDependent.putInt((byte[])array, (int)index, (int)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Integer.reverseBytes(value)));
        } else {
            PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)(value >>> 24)));
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 16)));
            PlatformDependent.putByte((byte[])array, (int)(index + 2), (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 3), (byte)((byte)value));
        }
    }

    static void setIntLE(byte[] array, int index, int value) {
        if (UNALIGNED) {
            PlatformDependent.putInt((byte[])array, (int)index, (int)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Integer.reverseBytes(value) : value));
        } else {
            PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)value));
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 2), (byte)((byte)(value >>> 16)));
            PlatformDependent.putByte((byte[])array, (int)(index + 3), (byte)((byte)(value >>> 24)));
        }
    }

    static void setLong(byte[] array, int index, long value) {
        if (UNALIGNED) {
            PlatformDependent.putLong((byte[])array, (int)index, (long)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? value : Long.reverseBytes(value)));
        } else {
            PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)(value >>> 56)));
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 48)));
            PlatformDependent.putByte((byte[])array, (int)(index + 2), (byte)((byte)(value >>> 40)));
            PlatformDependent.putByte((byte[])array, (int)(index + 3), (byte)((byte)(value >>> 32)));
            PlatformDependent.putByte((byte[])array, (int)(index + 4), (byte)((byte)(value >>> 24)));
            PlatformDependent.putByte((byte[])array, (int)(index + 5), (byte)((byte)(value >>> 16)));
            PlatformDependent.putByte((byte[])array, (int)(index + 6), (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 7), (byte)((byte)value));
        }
    }

    static void setLongLE(byte[] array, int index, long value) {
        if (UNALIGNED) {
            PlatformDependent.putLong((byte[])array, (int)index, (long)(PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? Long.reverseBytes(value) : value));
        } else {
            PlatformDependent.putByte((byte[])array, (int)index, (byte)((byte)value));
            PlatformDependent.putByte((byte[])array, (int)(index + 1), (byte)((byte)(value >>> 8)));
            PlatformDependent.putByte((byte[])array, (int)(index + 2), (byte)((byte)(value >>> 16)));
            PlatformDependent.putByte((byte[])array, (int)(index + 3), (byte)((byte)(value >>> 24)));
            PlatformDependent.putByte((byte[])array, (int)(index + 4), (byte)((byte)(value >>> 32)));
            PlatformDependent.putByte((byte[])array, (int)(index + 5), (byte)((byte)(value >>> 40)));
            PlatformDependent.putByte((byte[])array, (int)(index + 6), (byte)((byte)(value >>> 48)));
            PlatformDependent.putByte((byte[])array, (int)(index + 7), (byte)((byte)(value >>> 56)));
        }
    }

    static void setZero(byte[] array, int index, int length) {
        if (length == 0) {
            return;
        }
        PlatformDependent.setMemory((byte[])array, (int)index, (long)length, (byte)0);
    }

    static ByteBuf copy(AbstractByteBuf buf, long addr, int index, int length) {
        buf.checkIndex(index, length);
        ByteBuf copy = buf.alloc().directBuffer(length, buf.maxCapacity());
        if (length != 0) {
            if (copy.hasMemoryAddress()) {
                PlatformDependent.copyMemory(addr, copy.memoryAddress(), length);
                copy.setIndex(0, length);
            } else {
                copy.writeBytes(buf, index, length);
            }
        }
        return copy;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static int setBytes(AbstractByteBuf buf, long addr, int index, InputStream in, int length) throws IOException {
        buf.checkIndex(index, length);
        ByteBuf tmpBuf = buf.alloc().heapBuffer(length);
        try {
            byte[] tmp = tmpBuf.array();
            int offset = tmpBuf.arrayOffset();
            int readBytes = in.read(tmp, offset, length);
            if (readBytes > 0) {
                PlatformDependent.copyMemory(tmp, offset, addr, (long)readBytes);
            }
            int n = readBytes;
            return n;
        } finally {
            tmpBuf.release();
        }
    }

    static void getBytes(AbstractByteBuf buf, long addr, int index, ByteBuf dst, int dstIndex, int length) {
        buf.checkIndex(index, length);
        ObjectUtil.checkNotNull(dst, "dst");
        if (MathUtil.isOutOfBounds(dstIndex, length, dst.capacity())) {
            throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
        }
        if (dst.hasMemoryAddress()) {
            PlatformDependent.copyMemory(addr, dst.memoryAddress() + (long)dstIndex, length);
        } else if (dst.hasArray()) {
            PlatformDependent.copyMemory(addr, dst.array(), dst.arrayOffset() + dstIndex, (long)length);
        } else {
            dst.setBytes(dstIndex, buf, index, length);
        }
    }

    static void getBytes(AbstractByteBuf buf, long addr, int index, byte[] dst, int dstIndex, int length) {
        buf.checkIndex(index, length);
        ObjectUtil.checkNotNull(dst, "dst");
        if (MathUtil.isOutOfBounds(dstIndex, length, dst.length)) {
            throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
        }
        if (length != 0) {
            PlatformDependent.copyMemory(addr, dst, dstIndex, (long)length);
        }
    }

    static void getBytes(AbstractByteBuf buf, long addr, int index, ByteBuffer dst) {
        buf.checkIndex(index, dst.remaining());
        if (dst.remaining() == 0) {
            return;
        }
        if (dst.isDirect()) {
            if (dst.isReadOnly()) {
                throw new ReadOnlyBufferException();
            }
            long dstAddress = PlatformDependent.directBufferAddress(dst);
            PlatformDependent.copyMemory(addr, dstAddress + (long)dst.position(), dst.remaining());
            dst.position(dst.position() + dst.remaining());
        } else if (dst.hasArray()) {
            PlatformDependent.copyMemory(addr, dst.array(), dst.arrayOffset() + dst.position(), (long)dst.remaining());
            dst.position(dst.position() + dst.remaining());
        } else {
            dst.put(buf.nioBuffer());
        }
    }

    static void setBytes(AbstractByteBuf buf, long addr, int index, ByteBuf src, int srcIndex, int length) {
        buf.checkIndex(index, length);
        ObjectUtil.checkNotNull(src, "src");
        if (MathUtil.isOutOfBounds(srcIndex, length, src.capacity())) {
            throw new IndexOutOfBoundsException("srcIndex: " + srcIndex);
        }
        if (length != 0) {
            if (src.hasMemoryAddress()) {
                PlatformDependent.copyMemory(src.memoryAddress() + (long)srcIndex, addr, length);
            } else if (src.hasArray()) {
                PlatformDependent.copyMemory(src.array(), src.arrayOffset() + srcIndex, addr, (long)length);
            } else {
                src.getBytes(srcIndex, buf, index, length);
            }
        }
    }

    static void setBytes(AbstractByteBuf buf, long addr, int index, byte[] src, int srcIndex, int length) {
        buf.checkIndex(index, length);
        ObjectUtil.checkNotNull(src, "src");
        if (MathUtil.isOutOfBounds(srcIndex, length, src.length)) {
            throw new IndexOutOfBoundsException("srcIndex: " + srcIndex);
        }
        if (length != 0) {
            PlatformDependent.copyMemory(src, srcIndex, addr, (long)length);
        }
    }

    static void setBytes(AbstractByteBuf buf, long addr, int index, ByteBuffer src) {
        int length = src.remaining();
        if (length == 0) {
            return;
        }
        if (src.isDirect()) {
            buf.checkIndex(index, length);
            long srcAddress = PlatformDependent.directBufferAddress(src);
            PlatformDependent.copyMemory(srcAddress + (long)src.position(), addr, length);
            src.position(src.position() + length);
        } else if (src.hasArray()) {
            buf.checkIndex(index, length);
            PlatformDependent.copyMemory(src.array(), src.arrayOffset() + src.position(), addr, (long)length);
            src.position(src.position() + length);
        } else if (length < 8) {
            UnsafeByteBufUtil.setSingleBytes(buf, addr, index, src, length);
        } else {
            assert (buf.nioBufferCount() == 1);
            ByteBuffer internalBuffer = buf.internalNioBuffer(index, length);
            internalBuffer.put(src);
        }
    }

    private static void setSingleBytes(AbstractByteBuf buf, long addr, int index, ByteBuffer src, int length) {
        buf.checkIndex(index, length);
        int srcPosition = src.position();
        int srcLimit = src.limit();
        long dstAddr = addr;
        for (int srcIndex = srcPosition; srcIndex < srcLimit; ++srcIndex) {
            byte value = src.get(srcIndex);
            PlatformDependent.putByte(dstAddr, value);
            ++dstAddr;
        }
        src.position(srcLimit);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void getBytes(AbstractByteBuf buf, long addr, int index, OutputStream out, int length) throws IOException {
        buf.checkIndex(index, length);
        if (length != 0) {
            int len = Math.min(length, 8192);
            if (len <= 1024 || !buf.alloc().isDirectBufferPooled()) {
                UnsafeByteBufUtil.getBytes(addr, ByteBufUtil.threadLocalTempArray((int)len), 0, len, out, length);
            } else {
                ByteBuf tmpBuf = buf.alloc().heapBuffer(len);
                try {
                    byte[] tmp = tmpBuf.array();
                    int offset = tmpBuf.arrayOffset();
                    UnsafeByteBufUtil.getBytes(addr, tmp, offset, len, out, length);
                } finally {
                    tmpBuf.release();
                }
            }
        }
    }

    private static void getBytes(long inAddr, byte[] in, int inOffset, int inLen, OutputStream out, int outLen) throws IOException {
        int len;
        do {
            len = Math.min(inLen, outLen);
            PlatformDependent.copyMemory(inAddr, in, inOffset, (long)len);
            out.write(in, inOffset, len);
            inAddr += (long)len;
        } while ((outLen -= len) > 0);
    }

    static void setZero(long addr, int length) {
        if (length == 0) {
            return;
        }
        PlatformDependent.setMemory((long)addr, (long)length, (byte)0);
    }

    static UnpooledUnsafeDirectByteBuf newUnsafeDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
        if (PlatformDependent.useDirectBufferNoCleaner()) {
            return new UnpooledUnsafeNoCleanerDirectByteBuf(alloc, initialCapacity, maxCapacity);
        }
        return new UnpooledUnsafeDirectByteBuf(alloc, initialCapacity, maxCapacity);
    }

    private UnsafeByteBufUtil() {
    }
}

