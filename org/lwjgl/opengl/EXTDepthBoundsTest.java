/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class EXTDepthBoundsTest {
    public static final int GL_DEPTH_BOUNDS_TEST_EXT = 34960;
    public static final int GL_DEPTH_BOUNDS_EXT = 34961;

    private EXTDepthBoundsTest() {
    }

    public static void glDepthBoundsEXT(double zmin, double zmax) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glDepthBoundsEXT;
        BufferChecks.checkFunctionAddress(function_pointer);
        EXTDepthBoundsTest.nglDepthBoundsEXT(zmin, zmax, function_pointer);
    }

    static native void nglDepthBoundsEXT(double var0, double var2, long var4);
}

