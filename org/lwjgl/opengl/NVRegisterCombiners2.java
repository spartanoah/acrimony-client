/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.FloatBuffer;
import org.lwjgl.BufferChecks;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class NVRegisterCombiners2 {
    public static final int GL_PER_STAGE_CONSTANTS_NV = 34101;

    private NVRegisterCombiners2() {
    }

    public static void glCombinerStageParameterNV(int stage, int pname, FloatBuffer params) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glCombinerStageParameterfvNV;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkBuffer(params, 4);
        NVRegisterCombiners2.nglCombinerStageParameterfvNV(stage, pname, MemoryUtil.getAddress(params), function_pointer);
    }

    static native void nglCombinerStageParameterfvNV(int var0, int var1, long var2, long var4);

    public static void glGetCombinerStageParameterNV(int stage, int pname, FloatBuffer params) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glGetCombinerStageParameterfvNV;
        BufferChecks.checkFunctionAddress(function_pointer);
        BufferChecks.checkBuffer(params, 4);
        NVRegisterCombiners2.nglGetCombinerStageParameterfvNV(stage, pname, MemoryUtil.getAddress(params), function_pointer);
    }

    static native void nglGetCombinerStageParameterfvNV(int var0, int var1, long var2, long var4);
}

