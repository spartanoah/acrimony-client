/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GLContext;

public final class ARBTextureStorage {
    public static final int GL_TEXTURE_IMMUTABLE_FORMAT = 37167;

    private ARBTextureStorage() {
    }

    public static void glTexStorage1D(int target, int levels, int internalformat, int width) {
        GL42.glTexStorage1D(target, levels, internalformat, width);
    }

    public static void glTexStorage2D(int target, int levels, int internalformat, int width, int height) {
        GL42.glTexStorage2D(target, levels, internalformat, width, height);
    }

    public static void glTexStorage3D(int target, int levels, int internalformat, int width, int height, int depth) {
        GL42.glTexStorage3D(target, levels, internalformat, width, height, depth);
    }

    public static void glTextureStorage1DEXT(int texture, int target, int levels, int internalformat, int width) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glTextureStorage1DEXT;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBTextureStorage.nglTextureStorage1DEXT(texture, target, levels, internalformat, width, function_pointer);
    }

    static native void nglTextureStorage1DEXT(int var0, int var1, int var2, int var3, int var4, long var5);

    public static void glTextureStorage2DEXT(int texture, int target, int levels, int internalformat, int width, int height) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glTextureStorage2DEXT;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBTextureStorage.nglTextureStorage2DEXT(texture, target, levels, internalformat, width, height, function_pointer);
    }

    static native void nglTextureStorage2DEXT(int var0, int var1, int var2, int var3, int var4, int var5, long var6);

    public static void glTextureStorage3DEXT(int texture, int target, int levels, int internalformat, int width, int height, int depth) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glTextureStorage3DEXT;
        BufferChecks.checkFunctionAddress(function_pointer);
        ARBTextureStorage.nglTextureStorage3DEXT(texture, target, levels, internalformat, width, height, depth, function_pointer);
    }

    static native void nglTextureStorage3DEXT(int var0, int var1, int var2, int var3, int var4, int var5, int var6, long var7);
}

