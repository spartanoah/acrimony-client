/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class ARBDrawBuffersBlend {
    private ARBDrawBuffersBlend() {
    }

    public static void glBlendEquationiARB(int buf, int mode) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBlendEquationiARB;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBDrawBuffersBlend.nglBlendEquationiARB(buf, mode, function_pointer);
    }

    static native void nglBlendEquationiARB(int var0, int var1, long var2);

    public static void glBlendEquationSeparateiARB(int buf, int modeRGB, int modeAlpha) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBlendEquationSeparateiARB;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBDrawBuffersBlend.nglBlendEquationSeparateiARB(buf, modeRGB, modeAlpha, function_pointer);
    }

    static native void nglBlendEquationSeparateiARB(int var0, int var1, int var2, long var3);

    public static void glBlendFunciARB(int buf, int src, int dst) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBlendFunciARB;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBDrawBuffersBlend.nglBlendFunciARB(buf, src, dst, function_pointer);
    }

    static native void nglBlendFunciARB(int var0, int var1, int var2, long var3);

    public static void glBlendFuncSeparateiARB(int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glBlendFuncSeparateiARB;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBDrawBuffersBlend.nglBlendFuncSeparateiARB(buf, srcRGB, dstRGB, srcAlpha, dstAlpha, function_pointer);
    }

    static native void nglBlendFuncSeparateiARB(int var0, int var1, int var2, int var3, int var4, long var5);
}

