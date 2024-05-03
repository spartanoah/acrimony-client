/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.BufferChecks;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.APIUtil;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLChecks;
import org.lwjgl.opengl.GLContext;

public final class GL42 {
    public static final int GL_COMPRESSED_RGBA_BPTC_UNORM = 36492;
    public static final int GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM = 36493;
    public static final int GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT = 36494;
    public static final int GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT = 36495;
    public static final int GL_UNPACK_COMPRESSED_BLOCK_WIDTH = 37159;
    public static final int GL_UNPACK_COMPRESSED_BLOCK_HEIGHT = 37160;
    public static final int GL_UNPACK_COMPRESSED_BLOCK_DEPTH = 37161;
    public static final int GL_UNPACK_COMPRESSED_BLOCK_SIZE = 37162;
    public static final int GL_PACK_COMPRESSED_BLOCK_WIDTH = 37163;
    public static final int GL_PACK_COMPRESSED_BLOCK_HEIGHT = 37164;
    public static final int GL_PACK_COMPRESSED_BLOCK_DEPTH = 37165;
    public static final int GL_PACK_COMPRESSED_BLOCK_SIZE = 37166;
    public static final int GL_ATOMIC_COUNTER_BUFFER = 37568;
    public static final int GL_ATOMIC_COUNTER_BUFFER_BINDING = 37569;
    public static final int GL_ATOMIC_COUNTER_BUFFER_START = 37570;
    public static final int GL_ATOMIC_COUNTER_BUFFER_SIZE = 37571;
    public static final int GL_ATOMIC_COUNTER_BUFFER_DATA_SIZE = 37572;
    public static final int GL_ATOMIC_COUNTER_BUFFER_ACTIVE_ATOMIC_COUNTERS = 37573;
    public static final int GL_ATOMIC_COUNTER_BUFFER_ACTIVE_ATOMIC_COUNTER_INDICES = 37574;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_VERTEX_SHADER = 37575;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_TESS_CONTROL_SHADER = 37576;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_TESS_EVALUATION_SHADER = 37577;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_GEOMETRY_SHADER = 37578;
    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_FRAGMENT_SHADER = 37579;
    public static final int GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS = 37580;
    public static final int GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS = 37581;
    public static final int GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS = 37582;
    public static final int GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS = 37583;
    public static final int GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS = 37584;
    public static final int GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS = 37585;
    public static final int GL_MAX_VERTEX_ATOMIC_COUNTERS = 37586;
    public static final int GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS = 37587;
    public static final int GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS = 37588;
    public static final int GL_MAX_GEOMETRY_ATOMIC_COUNTERS = 37589;
    public static final int GL_MAX_FRAGMENT_ATOMIC_COUNTERS = 37590;
    public static final int GL_MAX_COMBINED_ATOMIC_COUNTERS = 37591;
    public static final int GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE = 37592;
    public static final int GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS = 37596;
    public static final int GL_ACTIVE_ATOMIC_COUNTER_BUFFERS = 37593;
    public static final int GL_UNIFORM_ATOMIC_COUNTER_BUFFER_INDEX = 37594;
    public static final int GL_UNSIGNED_INT_ATOMIC_COUNTER = 37595;
    public static final int GL_TEXTURE_IMMUTABLE_FORMAT = 37167;
    public static final int GL_MAX_IMAGE_UNITS = 36664;
    public static final int GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS = 36665;
    public static final int GL_MAX_IMAGE_SAMPLES = 36973;
    public static final int GL_MAX_VERTEX_IMAGE_UNIFORMS = 37066;
    public static final int GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS = 37067;
    public static final int GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS = 37068;
    public static final int GL_MAX_GEOMETRY_IMAGE_UNIFORMS = 37069;
    public static final int GL_MAX_FRAGMENT_IMAGE_UNIFORMS = 37070;
    public static final int GL_MAX_COMBINED_IMAGE_UNIFORMS = 37071;
    public static final int GL_IMAGE_BINDING_NAME = 36666;
    public static final int GL_IMAGE_BINDING_LEVEL = 36667;
    public static final int GL_IMAGE_BINDING_LAYERED = 36668;
    public static final int GL_IMAGE_BINDING_LAYER = 36669;
    public static final int GL_IMAGE_BINDING_ACCESS = 36670;
    public static final int GL_IMAGE_BINDING_FORMAT = 36974;
    public static final int GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT = 1;
    public static final int GL_ELEMENT_ARRAY_BARRIER_BIT = 2;
    public static final int GL_UNIFORM_BARRIER_BIT = 4;
    public static final int GL_TEXTURE_FETCH_BARRIER_BIT = 8;
    public static final int GL_SHADER_IMAGE_ACCESS_BARRIER_BIT = 32;
    public static final int GL_COMMAND_BARRIER_BIT = 64;
    public static final int GL_PIXEL_BUFFER_BARRIER_BIT = 128;
    public static final int GL_TEXTURE_UPDATE_BARRIER_BIT = 256;
    public static final int GL_BUFFER_UPDATE_BARRIER_BIT = 512;
    public static final int GL_FRAMEBUFFER_BARRIER_BIT = 1024;
    public static final int GL_TRANSFORM_FEEDBACK_BARRIER_BIT = 2048;
    public static final int GL_ATOMIC_COUNTER_BARRIER_BIT = 4096;
    public static final int GL_ALL_BARRIER_BITS = -1;
    public static final int GL_IMAGE_1D = 36940;
    public static final int GL_IMAGE_2D = 36941;
    public static final int GL_IMAGE_3D = 36942;
    public static final int GL_IMAGE_2D_RECT = 36943;
    public static final int GL_IMAGE_CUBE = 36944;
    public static final int GL_IMAGE_BUFFER = 36945;
    public static final int GL_IMAGE_1D_ARRAY = 36946;
    public static final int GL_IMAGE_2D_ARRAY = 36947;
    public static final int GL_IMAGE_CUBE_MAP_ARRAY = 36948;
    public static final int GL_IMAGE_2D_MULTISAMPLE = 36949;
    public static final int GL_IMAGE_2D_MULTISAMPLE_ARRAY = 36950;
    public static final int GL_INT_IMAGE_1D = 36951;
    public static final int GL_INT_IMAGE_2D = 36952;
    public static final int GL_INT_IMAGE_3D = 36953;
    public static final int GL_INT_IMAGE_2D_RECT = 36954;
    public static final int GL_INT_IMAGE_CUBE = 36955;
    public static final int GL_INT_IMAGE_BUFFER = 36956;
    public static final int GL_INT_IMAGE_1D_ARRAY = 36957;
    public static final int GL_INT_IMAGE_2D_ARRAY = 36958;
    public static final int GL_INT_IMAGE_CUBE_MAP_ARRAY = 36959;
    public static final int GL_INT_IMAGE_2D_MULTISAMPLE = 36960;
    public static final int GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY = 36961;
    public static final int GL_UNSIGNED_INT_IMAGE_1D = 36962;
    public static final int GL_UNSIGNED_INT_IMAGE_2D = 36963;
    public static final int GL_UNSIGNED_INT_IMAGE_3D = 36964;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_RECT = 36965;
    public static final int GL_UNSIGNED_INT_IMAGE_CUBE = 36966;
    public static final int GL_UNSIGNED_INT_IMAGE_BUFFER = 36967;
    public static final int GL_UNSIGNED_INT_IMAGE_1D_ARRAY = 36968;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_ARRAY = 36969;
    public static final int GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY = 36970;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE = 36971;
    public static final int GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY = 36972;
    public static final int GL_IMAGE_FORMAT_COMPATIBILITY_TYPE = 37063;
    public static final int GL_IMAGE_FORMAT_COMPATIBILITY_BY_SIZE = 37064;
    public static final int IMAGE_FORMAT_COMPATIBILITY_BY_CLASS = 37065;
    public static final int GL_NUM_SAMPLE_COUNTS = 37760;
    public static final int GL_MIN_MAP_BUFFER_ALIGNMENT = 37052;

    private GL42() {
    }

    public static void glGetActiveAtomicCounterBuffer(int program, int bufferIndex, int pname, IntBuffer params) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glGetActiveAtomicCounterBufferiv;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkBuffer(params, 1);
        GL42.nglGetActiveAtomicCounterBufferiv(program, bufferIndex, pname, MemoryUtil.getAddress(params), function_pointer);
    }

    static native void nglGetActiveAtomicCounterBufferiv(int var0, int var1, int var2, long var3, long var5);

    public static int glGetActiveAtomicCounterBuffer(int program, int bufferIndex, int pname) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glGetActiveAtomicCounterBufferiv;
        BufferChecks.checkFunctionAddress(function_pointer);
        IntBuffer params = APIUtil.getBufferInt(caps);
        GL42.nglGetActiveAtomicCounterBufferiv(program, bufferIndex, pname, MemoryUtil.getAddress(params), function_pointer);
        return params.get(0);
    }

    public static void glTexStorage1D(int target, int levels, int internalformat, int width) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glTexStorage1D;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglTexStorage1D(target, levels, internalformat, width, function_pointer);
    }

    static native void nglTexStorage1D(int var0, int var1, int var2, int var3, long var4);

    public static void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glTexStorage2D;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglTexStorage2D(target, levels, internalformat, width, height, function_pointer);
    }

    static native void nglTexStorage2D(int var0, int var1, int var2, int var3, int var4, long var5);

    public static void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glTexStorage3D;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglTexStorage3D(target, levels, internalformat, width, height, depth, function_pointer);
    }

    static native void nglTexStorage3D(int var0, int var1, int var2, int var3, int var4, int var5, long var6);

    public static void glDrawTransformFeedbackInstanced(int mode, int id, int primcount) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawTransformFeedbackInstanced;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglDrawTransformFeedbackInstanced(mode, id, primcount, function_pointer);
    }

    static native void nglDrawTransformFeedbackInstanced(int var0, int var1, int var2, long var3);

    public static void glDrawTransformFeedbackStreamInstanced(int mode, int id, int stream, int primcount) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawTransformFeedbackStreamInstanced;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglDrawTransformFeedbackStreamInstanced(mode, id, stream, primcount, function_pointer);
    }

    static native void nglDrawTransformFeedbackStreamInstanced(int var0, int var1, int var2, int var3, long var4);

    public static void glDrawArraysInstancedBaseInstance(int mode, int first, int count, int primcount, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawArraysInstancedBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglDrawArraysInstancedBaseInstance(mode, first, count, primcount, baseinstance, function_pointer);
    }

    static native void nglDrawArraysInstancedBaseInstance(int var0, int var1, int var2, int var3, int var4, long var5);

    public static void glDrawElementsInstancedBaseInstance(int mode, ByteBuffer indices, int primcount, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOdisabled(caps);
        BufferChecks.checkDirect(indices);
        GL42.nglDrawElementsInstancedBaseInstance(mode, indices.remaining(), 5121, MemoryUtil.getAddress(indices), primcount, baseinstance, function_pointer);
    }

    public static void glDrawElementsInstancedBaseInstance(int mode, IntBuffer indices, int primcount, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOdisabled(caps);
        BufferChecks.checkDirect(indices);
        GL42.nglDrawElementsInstancedBaseInstance(mode, indices.remaining(), 5125, MemoryUtil.getAddress(indices), primcount, baseinstance, function_pointer);
    }

    public static void glDrawElementsInstancedBaseInstance(int mode, ShortBuffer indices, int primcount, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOdisabled(caps);
        BufferChecks.checkDirect(indices);
        GL42.nglDrawElementsInstancedBaseInstance(mode, indices.remaining(), 5123, MemoryUtil.getAddress(indices), primcount, baseinstance, function_pointer);
    }

    static native void nglDrawElementsInstancedBaseInstance(int var0, int var1, int var2, long var3, int var5, int var6, long var7);

    public static void glDrawElementsInstancedBaseInstance(int mode, int indices_count, int type, long indices_buffer_offset, int primcount, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOenabled(caps);
        GL42.nglDrawElementsInstancedBaseInstanceBO(mode, indices_count, type, indices_buffer_offset, primcount, baseinstance, function_pointer);
    }

    static native void nglDrawElementsInstancedBaseInstanceBO(int var0, int var1, int var2, long var3, int var5, int var6, long var7);

    public static void glDrawElementsInstancedBaseVertexBaseInstance(int mode, ByteBuffer indices, int primcount, int basevertex, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseVertexBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOdisabled(caps);
        BufferChecks.checkDirect(indices);
        GL42.nglDrawElementsInstancedBaseVertexBaseInstance(mode, indices.remaining(), 5121, MemoryUtil.getAddress(indices), primcount, basevertex, baseinstance, function_pointer);
    }

    public static void glDrawElementsInstancedBaseVertexBaseInstance(int mode, IntBuffer indices, int primcount, int basevertex, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseVertexBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOdisabled(caps);
        BufferChecks.checkDirect(indices);
        GL42.nglDrawElementsInstancedBaseVertexBaseInstance(mode, indices.remaining(), 5125, MemoryUtil.getAddress(indices), primcount, basevertex, baseinstance, function_pointer);
    }

    public static void glDrawElementsInstancedBaseVertexBaseInstance(int mode, ShortBuffer indices, int primcount, int basevertex, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseVertexBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOdisabled(caps);
        BufferChecks.checkDirect(indices);
        GL42.nglDrawElementsInstancedBaseVertexBaseInstance(mode, indices.remaining(), 5123, MemoryUtil.getAddress(indices), primcount, basevertex, baseinstance, function_pointer);
    }

    static native void nglDrawElementsInstancedBaseVertexBaseInstance(int var0, int var1, int var2, long var3, int var5, int var6, int var7, long var8);

    public static void glDrawElementsInstancedBaseVertexBaseInstance(int mode, int indices_count, int type, long indices_buffer_offset, int primcount, int basevertex, int baseinstance) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDrawElementsInstancedBaseVertexBaseInstance;
        BufferChecks.checkFunctionAddress(function_pointer);
        GLChecks.ensureElementVBOenabled(caps);
        GL42.nglDrawElementsInstancedBaseVertexBaseInstanceBO(mode, indices_count, type, indices_buffer_offset, primcount, basevertex, baseinstance, function_pointer);
    }

    static native void nglDrawElementsInstancedBaseVertexBaseInstanceBO(int var0, int var1, int var2, long var3, int var5, int var6, int var7, long var8);

    public static void glBindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBindImageTexture;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglBindImageTexture(unit, texture, level, layered, layer, access, format, function_pointer);
    }

    static native void nglBindImageTexture(int var0, int var1, int var2, boolean var3, int var4, int var5, int var6, long var7);

    public static void glMemoryBarrier(int barriers) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glMemoryBarrier;
        BufferChecks.checkFunctionAddress(function_pointer);
        GL42.nglMemoryBarrier(barriers, function_pointer);
    }

    static native void nglMemoryBarrier(int var0, long var1);

    public static void glGetInternalformat(int target, int internalformat, int pname, IntBuffer params) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glGetInternalformativ;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkDirect(params);
        GL42.nglGetInternalformativ(target, internalformat, pname, params.remaining(), MemoryUtil.getAddress(params), function_pointer);
    }

    static native void nglGetInternalformativ(int var0, int var1, int var2, int var3, long var4, long var6);

    public static int glGetInternalformat(int target, int internalformat, int pname) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glGetInternalformativ;
        BufferChecks.checkFunctionAddress(function_pointer);
        IntBuffer params = APIUtil.getBufferInt(caps);
        GL42.nglGetInternalformativ(target, internalformat, pname, 1, MemoryUtil.getAddress(params), function_pointer);
        return params.get(0);
    }
}

