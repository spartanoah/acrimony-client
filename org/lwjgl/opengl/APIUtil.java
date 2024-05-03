/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.ContextCapabilities;

final class APIUtil {
    private static final int INITIAL_BUFFER_SIZE = 256;
    private static final int INITIAL_LENGTHS_SIZE = 4;
    private static final int BUFFERS_SIZE = 32;
    private char[] array = new char[256];
    private ByteBuffer buffer = BufferUtils.createByteBuffer(256);
    private IntBuffer lengths = BufferUtils.createIntBuffer(4);
    private final IntBuffer ints = BufferUtils.createIntBuffer(32);
    private final LongBuffer longs = BufferUtils.createLongBuffer(32);
    private final FloatBuffer floats = BufferUtils.createFloatBuffer(32);
    private final DoubleBuffer doubles = BufferUtils.createDoubleBuffer(32);

    APIUtil() {
    }

    private static char[] getArray(ContextCapabilities caps, int size) {
        char[] array = caps.util.array;
        if (array.length < size) {
            for (int sizeNew = array.length << 1; sizeNew < size; sizeNew <<= 1) {
            }
            caps.util.array = array = new char[size];
        }
        return array;
    }

    static ByteBuffer getBufferByte(ContextCapabilities caps, int size) {
        ByteBuffer buffer = caps.util.buffer;
        if (buffer.capacity() < size) {
            for (int sizeNew = buffer.capacity() << 1; sizeNew < size; sizeNew <<= 1) {
            }
            caps.util.buffer = buffer = BufferUtils.createByteBuffer(size);
        } else {
            buffer.clear();
        }
        return buffer;
    }

    private static ByteBuffer getBufferByteOffset(ContextCapabilities caps, int size) {
        ByteBuffer buffer = caps.util.buffer;
        if (buffer.capacity() < size) {
            for (int sizeNew = buffer.capacity() << 1; sizeNew < size; sizeNew <<= 1) {
            }
            ByteBuffer bufferNew = BufferUtils.createByteBuffer(size);
            bufferNew.put(buffer);
            caps.util.buffer = buffer = bufferNew;
        } else {
            buffer.position(buffer.limit());
            buffer.limit(buffer.capacity());
        }
        return buffer;
    }

    static IntBuffer getBufferInt(ContextCapabilities caps) {
        return caps.util.ints;
    }

    static LongBuffer getBufferLong(ContextCapabilities caps) {
        return caps.util.longs;
    }

    static FloatBuffer getBufferFloat(ContextCapabilities caps) {
        return caps.util.floats;
    }

    static DoubleBuffer getBufferDouble(ContextCapabilities caps) {
        return caps.util.doubles;
    }

    static IntBuffer getLengths(ContextCapabilities caps) {
        return APIUtil.getLengths(caps, 1);
    }

    static IntBuffer getLengths(ContextCapabilities caps, int size) {
        IntBuffer lengths = caps.util.lengths;
        if (lengths.capacity() < size) {
            for (int sizeNew = lengths.capacity(); sizeNew < size; sizeNew <<= 1) {
            }
            caps.util.lengths = lengths = BufferUtils.createIntBuffer(size);
        } else {
            lengths.clear();
        }
        return lengths;
    }

    private static ByteBuffer encode(ByteBuffer buffer, CharSequence string) {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (LWJGLUtil.DEBUG && '\u0080' <= c) {
                buffer.put((byte)26);
                continue;
            }
            buffer.put((byte)c);
        }
        return buffer;
    }

    static String getString(ContextCapabilities caps, ByteBuffer buffer) {
        int length = buffer.remaining();
        char[] charArray = APIUtil.getArray(caps, length);
        for (int i = buffer.position(); i < buffer.limit(); ++i) {
            charArray[i - buffer.position()] = (char)buffer.get(i);
        }
        return new String(charArray, 0, length);
    }

    static long getBuffer(ContextCapabilities caps, CharSequence string) {
        ByteBuffer buffer = APIUtil.encode(APIUtil.getBufferByte(caps, string.length()), string);
        buffer.flip();
        return MemoryUtil.getAddress0(buffer);
    }

    static long getBuffer(ContextCapabilities caps, CharSequence string, int offset) {
        ByteBuffer buffer = APIUtil.encode(APIUtil.getBufferByteOffset(caps, offset + string.length()), string);
        buffer.flip();
        return MemoryUtil.getAddress(buffer);
    }

    static long getBufferNT(ContextCapabilities caps, CharSequence string) {
        ByteBuffer buffer = APIUtil.encode(APIUtil.getBufferByte(caps, string.length() + 1), string);
        buffer.put((byte)0);
        buffer.flip();
        return MemoryUtil.getAddress0(buffer);
    }

    static int getTotalLength(CharSequence[] strings) {
        int length = 0;
        for (CharSequence string : strings) {
            length += string.length();
        }
        return length;
    }

    static long getBuffer(ContextCapabilities caps, CharSequence[] strings) {
        ByteBuffer buffer = APIUtil.getBufferByte(caps, APIUtil.getTotalLength(strings));
        for (CharSequence string : strings) {
            APIUtil.encode(buffer, string);
        }
        buffer.flip();
        return MemoryUtil.getAddress0(buffer);
    }

    static long getBufferNT(ContextCapabilities caps, CharSequence[] strings) {
        ByteBuffer buffer = APIUtil.getBufferByte(caps, APIUtil.getTotalLength(strings) + strings.length);
        for (CharSequence string : strings) {
            APIUtil.encode(buffer, string);
            buffer.put((byte)0);
        }
        buffer.flip();
        return MemoryUtil.getAddress0(buffer);
    }

    static long getLengths(ContextCapabilities caps, CharSequence[] strings) {
        IntBuffer buffer = APIUtil.getLengths(caps, strings.length);
        for (CharSequence string : strings) {
            buffer.put(string.length());
        }
        buffer.flip();
        return MemoryUtil.getAddress0(buffer);
    }

    static long getInt(ContextCapabilities caps, int value) {
        return MemoryUtil.getAddress0(APIUtil.getBufferInt(caps).put(0, value));
    }

    static long getBufferByte0(ContextCapabilities caps) {
        return MemoryUtil.getAddress0(APIUtil.getBufferByte(caps, 0));
    }
}

