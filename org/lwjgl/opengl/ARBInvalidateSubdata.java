/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.IntBuffer;
import org.lwjgl.opengl.GL43;

public final class ARBInvalidateSubdata {
    private ARBInvalidateSubdata() {
    }

    public static void glInvalidateTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth) {
        GL43.glInvalidateTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth);
    }

    public static void glInvalidateTexImage(int texture, int level) {
        GL43.glInvalidateTexImage(texture, level);
    }

    public static void glInvalidateBufferSubData(int buffer, long offset, long length) {
        GL43.glInvalidateBufferSubData(buffer, offset, length);
    }

    public static void glInvalidateBufferData(int buffer) {
        GL43.glInvalidateBufferData(buffer);
    }

    public static void glInvalidateFramebuffer(int target, IntBuffer attachments) {
        GL43.glInvalidateFramebuffer(target, attachments);
    }

    public static void glInvalidateSubFramebuffer(int target, IntBuffer attachments, int x, int y, int width, int height) {
        GL43.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
    }
}

