/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.BufferChecks;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.ATIVertexArrayObject;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

public final class ATIMapObjectBuffer {
    private ATIMapObjectBuffer() {
    }

    public static ByteBuffer glMapObjectBufferATI(int buffer, ByteBuffer old_buffer) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glMapObjectBufferATI;
        BufferChecks.checkFunctionAddress(function_pointer);
        if (old_buffer != null) {
            BufferChecks.checkDirect(old_buffer);
        }
        ByteBuffer __result = ATIMapObjectBuffer.nglMapObjectBufferATI(buffer, ATIVertexArrayObject.glGetObjectBufferiATI(buffer, 34660), old_buffer, function_pointer);
        return LWJGLUtil.CHECKS && __result == null ? null : __result.order(ByteOrder.nativeOrder());
    }

    public static ByteBuffer glMapObjectBufferATI(int buffer, long length, ByteBuffer old_buffer) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glMapObjectBufferATI;
        BufferChecks.checkFunctionAddress(function_pointer);
        if (old_buffer != null) {
            BufferChecks.checkDirect(old_buffer);
        }
        ByteBuffer __result = ATIMapObjectBuffer.nglMapObjectBufferATI(buffer, length, old_buffer, function_pointer);
        return LWJGLUtil.CHECKS && __result == null ? null : __result.order(ByteOrder.nativeOrder());
    }

    static native ByteBuffer nglMapObjectBufferATI(int var0, long var1, ByteBuffer var3, long var4);

    public static void glUnmapObjectBufferATI(int buffer) {
        ContextCapabilities caps = GLContext.getCapabilities();
        long function_pointer = caps.glUnmapObjectBufferATI;
        BufferChecks.checkFunctionAddress(function_pointer);
        ATIMapObjectBuffer.nglUnmapObjectBufferATI(buffer, function_pointer);
    }

    static native void nglUnmapObjectBufferATI(int var0, long var1);
}

