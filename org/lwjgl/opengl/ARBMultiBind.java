/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.IntBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL44;

public final class ARBMultiBind {
    private ARBMultiBind() {
    }

    public static void glBindBuffersBase(int target, int first, int count, IntBuffer buffers) {
        GL44.glBindBuffersBase(target, first, count, buffers);
    }

    public static void glBindBuffersRange(int target, int first, int count, IntBuffer buffers, PointerBuffer offsets, PointerBuffer sizes) {
        GL44.glBindBuffersRange(target, first, count, buffers, offsets, sizes);
    }

    public static void glBindTextures(int first, int count, IntBuffer textures) {
        GL44.glBindTextures(first, count, textures);
    }

    public static void glBindSamplers(int first, int count, IntBuffer samplers) {
        GL44.glBindSamplers(first, count, samplers);
    }

    public static void glBindImageTextures(int first, int count, IntBuffer textures) {
        GL44.glBindImageTextures(first, count, textures);
    }

    public static void glBindVertexBuffers(int first, int count, IntBuffer buffers, PointerBuffer offsets, IntBuffer strides) {
        GL44.glBindVertexBuffers(first, count, buffers, offsets, strides);
    }
}

