/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.FloatBuffer;
import org.lwjgl.BufferChecks;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class GL21 {
    public static final int GL_FLOAT_MAT2x3 = 35685;
    public static final int GL_FLOAT_MAT2x4 = 35686;
    public static final int GL_FLOAT_MAT3x2 = 35687;
    public static final int GL_FLOAT_MAT3x4 = 35688;
    public static final int GL_FLOAT_MAT4x2 = 35689;
    public static final int GL_FLOAT_MAT4x3 = 35690;
    public static final int GL_PIXEL_PACK_BUFFER = 35051;
    public static final int GL_PIXEL_UNPACK_BUFFER = 35052;
    public static final int GL_PIXEL_PACK_BUFFER_BINDING = 35053;
    public static final int GL_PIXEL_UNPACK_BUFFER_BINDING = 35055;
    public static final int GL_SRGB = 35904;
    public static final int GL_SRGB8 = 35905;
    public static final int GL_SRGB_ALPHA = 35906;
    public static final int GL_SRGB8_ALPHA8 = 35907;
    public static final int GL_SLUMINANCE_ALPHA = 35908;
    public static final int GL_SLUMINANCE8_ALPHA8 = 35909;
    public static final int GL_SLUMINANCE = 35910;
    public static final int GL_SLUMINANCE8 = 35911;
    public static final int GL_COMPRESSED_SRGB = 35912;
    public static final int GL_COMPRESSED_SRGB_ALPHA = 35913;
    public static final int GL_COMPRESSED_SLUMINANCE = 35914;
    public static final int GL_COMPRESSED_SLUMINANCE_ALPHA = 35915;
    public static final int GL_CURRENT_RASTER_SECONDARY_COLOR = 33887;

    private GL21() {
    }

    public static void glUniformMatrix2x3(int location, boolean transpose, FloatBuffer matrices) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUniformMatrix2x3fv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(matrices);
        GL21.nglUniformMatrix2x3fv(location, matrices.remaining() / 6, transpose, MemoryUtil.getAddress(matrices), function_pointer);
    }

    static native void nglUniformMatrix2x3fv(int var0, int var1, boolean var2, long var3, long var5);

    public static void glUniformMatrix3x2(int location, boolean transpose, FloatBuffer matrices) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUniformMatrix3x2fv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(matrices);
        GL21.nglUniformMatrix3x2fv(location, matrices.remaining() / 6, transpose, MemoryUtil.getAddress(matrices), function_pointer);
    }

    static native void nglUniformMatrix3x2fv(int var0, int var1, boolean var2, long var3, long var5);

    public static void glUniformMatrix2x4(int location, boolean transpose, FloatBuffer matrices) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUniformMatrix2x4fv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(matrices);
        GL21.nglUniformMatrix2x4fv(location, matrices.remaining() >> 3, transpose, MemoryUtil.getAddress(matrices), function_pointer);
    }

    static native void nglUniformMatrix2x4fv(int var0, int var1, boolean var2, long var3, long var5);

    public static void glUniformMatrix4x2(int location, boolean transpose, FloatBuffer matrices) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUniformMatrix4x2fv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(matrices);
        GL21.nglUniformMatrix4x2fv(location, matrices.remaining() >> 3, transpose, MemoryUtil.getAddress(matrices), function_pointer);
    }

    static native void nglUniformMatrix4x2fv(int var0, int var1, boolean var2, long var3, long var5);

    public static void glUniformMatrix3x4(int location, boolean transpose, FloatBuffer matrices) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUniformMatrix3x4fv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(matrices);
        GL21.nglUniformMatrix3x4fv(location, matrices.remaining() / 12, transpose, MemoryUtil.getAddress(matrices), function_pointer);
    }

    static native void nglUniformMatrix3x4fv(int var0, int var1, boolean var2, long var3, long var5);

    public static void glUniformMatrix4x3(int location, boolean transpose, FloatBuffer matrices) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUniformMatrix4x3fv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(matrices);
        GL21.nglUniformMatrix4x3fv(location, matrices.remaining() / 12, transpose, MemoryUtil.getAddress(matrices), function_pointer);
    }

    static native void nglUniformMatrix4x3fv(int var0, int var1, boolean var2, long var3, long var5);
}

