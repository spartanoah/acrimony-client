/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class EXTCompiledVertexArray {
    public static final int GL_ARRAY_ELEMENT_LOCK_FIRST_EXT = 33192;
    public static final int GL_ARRAY_ELEMENT_LOCK_COUNT_EXT = 33193;

    private EXTCompiledVertexArray() {
    }

    public static void glLockArraysEXT(int first, int count) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glLockArraysEXT;
        BufferChecks.checkFunctionAddress(function_pointer);
        EXTCompiledVertexArray.nglLockArraysEXT(first, count, function_pointer);
    }

    static native void nglLockArraysEXT(int var0, int var1, long var2);

    public static void glUnlockArraysEXT() {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUnlockArraysEXT;
        BufferChecks.checkFunctionAddress(function_pointer);
        EXTCompiledVertexArray.nglUnlockArraysEXT(function_pointer);
    }

    static native void nglUnlockArraysEXT(long var0);
}

