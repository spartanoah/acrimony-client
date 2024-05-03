/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.PointerBuffer;

public class BufferChecks {
    private BufferChecks() {
    }

    public static void checkFunctionAddress(long pointer) {
        if (LWJGLUtil.CHECKS && pointer == 0L) {
            throw new IllegalStateException("Function is not supported");
        }
    }

    public static void checkNullTerminated(ByteBuffer buf) {
        if (LWJGLUtil.CHECKS && buf.get(buf.limit() - 1) != 0) {
            throw new IllegalArgumentException("Missing null termination");
        }
    }

    public static void checkNullTerminated(ByteBuffer buf, int count) {
        if (LWJGLUtil.CHECKS) {
            int nullFound = 0;
            for (int i = buf.position(); i < buf.limit(); ++i) {
                if (buf.get(i) != 0) continue;
                ++nullFound;
            }
            if (nullFound < count) {
                throw new IllegalArgumentException("Missing null termination");
            }
        }
    }

    public static void checkNullTerminated(IntBuffer buf) {
        if (LWJGLUtil.CHECKS && buf.get(buf.limit() - 1) != 0) {
            throw new IllegalArgumentException("Missing null termination");
        }
    }

    public static void checkNullTerminated(LongBuffer buf) {
        if (LWJGLUtil.CHECKS && buf.get(buf.limit() - 1) != 0L) {
            throw new IllegalArgumentException("Missing null termination");
        }
    }

    public static void checkNullTerminated(PointerBuffer buf) {
        if (LWJGLUtil.CHECKS && buf.get(buf.limit() - 1) != 0L) {
            throw new IllegalArgumentException("Missing null termination");
        }
    }

    public static void checkNotNull(Object o) {
        if (LWJGLUtil.CHECKS && o == null) {
            throw new IllegalArgumentException("Null argument");
        }
    }

    public static void checkDirect(ByteBuffer buf) {
        if (LWJGLUtil.CHECKS && !buf.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer is not direct");
        }
    }

    public static void checkDirect(ShortBuffer buf) {
        if (LWJGLUtil.CHECKS && !buf.isDirect()) {
            throw new IllegalArgumentException("ShortBuffer is not direct");
        }
    }

    public static void checkDirect(IntBuffer buf) {
        if (LWJGLUtil.CHECKS && !buf.isDirect()) {
            throw new IllegalArgumentException("IntBuffer is not direct");
        }
    }

    public static void checkDirect(LongBuffer buf) {
        if (LWJGLUtil.CHECKS && !buf.isDirect()) {
            throw new IllegalArgumentException("LongBuffer is not direct");
        }
    }

    public static void checkDirect(FloatBuffer buf) {
        if (LWJGLUtil.CHECKS && !buf.isDirect()) {
            throw new IllegalArgumentException("FloatBuffer is not direct");
        }
    }

    public static void checkDirect(DoubleBuffer buf) {
        if (LWJGLUtil.CHECKS && !buf.isDirect()) {
            throw new IllegalArgumentException("DoubleBuffer is not direct");
        }
    }

    public static void checkDirect(PointerBuffer buf) {
    }

    public static void checkArray(Object[] array) {
        if (LWJGLUtil.CHECKS && (array == null || array.length == 0)) {
            throw new IllegalArgumentException("Invalid array");
        }
    }

    private static void throwBufferSizeException(Buffer buf, int size) {
        throw new IllegalArgumentException("Number of remaining buffer elements is " + buf.remaining() + ", must be at least " + size + ". Because at most " + size + " elements can be returned, a buffer with at least " + size + " elements is required, regardless of actual returned element count");
    }

    private static void throwBufferSizeException(PointerBuffer buf, int size) {
        throw new IllegalArgumentException("Number of remaining pointer buffer elements is " + buf.remaining() + ", must be at least " + size);
    }

    private static void throwArraySizeException(Object[] array, int size) {
        throw new IllegalArgumentException("Number of array elements is " + array.length + ", must be at least " + size);
    }

    private static void throwArraySizeException(long[] array, int size) {
        throw new IllegalArgumentException("Number of array elements is " + array.length + ", must be at least " + size);
    }

    public static void checkBufferSize(Buffer buf, int size) {
        if (LWJGLUtil.CHECKS && buf.remaining() < size) {
            BufferChecks.throwBufferSizeException(buf, size);
        }
    }

    public static int checkBuffer(Buffer buffer, int size) {
        int posShift;
        if (buffer instanceof ByteBuffer) {
            BufferChecks.checkBuffer((ByteBuffer)buffer, size);
            posShift = 0;
        } else if (buffer instanceof ShortBuffer) {
            BufferChecks.checkBuffer((ShortBuffer)buffer, size);
            posShift = 1;
        } else if (buffer instanceof IntBuffer) {
            BufferChecks.checkBuffer((IntBuffer)buffer, size);
            posShift = 2;
        } else if (buffer instanceof LongBuffer) {
            BufferChecks.checkBuffer((LongBuffer)buffer, size);
            posShift = 4;
        } else if (buffer instanceof FloatBuffer) {
            BufferChecks.checkBuffer((FloatBuffer)buffer, size);
            posShift = 2;
        } else if (buffer instanceof DoubleBuffer) {
            BufferChecks.checkBuffer((DoubleBuffer)buffer, size);
            posShift = 4;
        } else {
            throw new IllegalArgumentException("Unsupported Buffer type specified: " + buffer.getClass());
        }
        return buffer.position() << posShift;
    }

    public static void checkBuffer(ByteBuffer buf, int size) {
        if (LWJGLUtil.CHECKS) {
            BufferChecks.checkBufferSize(buf, size);
            BufferChecks.checkDirect(buf);
        }
    }

    public static void checkBuffer(ShortBuffer buf, int size) {
        if (LWJGLUtil.CHECKS) {
            BufferChecks.checkBufferSize(buf, size);
            BufferChecks.checkDirect(buf);
        }
    }

    public static void checkBuffer(IntBuffer buf, int size) {
        if (LWJGLUtil.CHECKS) {
            BufferChecks.checkBufferSize(buf, size);
            BufferChecks.checkDirect(buf);
        }
    }

    public static void checkBuffer(LongBuffer buf, int size) {
        if (LWJGLUtil.CHECKS) {
            BufferChecks.checkBufferSize(buf, size);
            BufferChecks.checkDirect(buf);
        }
    }

    public static void checkBuffer(FloatBuffer buf, int size) {
        if (LWJGLUtil.CHECKS) {
            BufferChecks.checkBufferSize(buf, size);
            BufferChecks.checkDirect(buf);
        }
    }

    public static void checkBuffer(DoubleBuffer buf, int size) {
        if (LWJGLUtil.CHECKS) {
            BufferChecks.checkBufferSize(buf, size);
            BufferChecks.checkDirect(buf);
        }
    }

    public static void checkBuffer(PointerBuffer buf, int size) {
        if (LWJGLUtil.CHECKS && buf.remaining() < size) {
            BufferChecks.throwBufferSizeException(buf, size);
        }
    }

    public static void checkArray(Object[] array, int size) {
        if (LWJGLUtil.CHECKS && array.length < size) {
            BufferChecks.throwArraySizeException(array, size);
        }
    }

    public static void checkArray(long[] array, int size) {
        if (LWJGLUtil.CHECKS && array.length < size) {
            BufferChecks.throwArraySizeException(array, size);
        }
    }
}

