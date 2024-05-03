/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.opengl.GL43;

public final class ARBVertexAttribBinding {
    public static final int GL_VERTEX_ATTRIB_BINDING = 33492;
    public static final int GL_VERTEX_ATTRIB_RELATIVE_OFFSET = 33493;
    public static final int GL_VERTEX_BINDING_DIVISOR = 33494;
    public static final int GL_VERTEX_BINDING_OFFSET = 33495;
    public static final int GL_VERTEX_BINDING_STRIDE = 33496;
    public static final int GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET = 33497;
    public static final int GL_MAX_VERTEX_ATTRIB_BINDINGS = 33498;

    private ARBVertexAttribBinding() {
    }

    public static void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        GL43.glBindVertexBuffer(bindingindex, buffer, offset, stride);
    }

    public static void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        GL43.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
    }

    public static void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        GL43.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
    }

    public static void glVertexAttribLFormat(int attribindex, int size, int type, int relativeoffset) {
        GL43.glVertexAttribLFormat(attribindex, size, type, relativeoffset);
    }

    public static void glVertexAttribBinding(int attribindex, int bindingindex) {
        GL43.glVertexAttribBinding(attribindex, bindingindex);
    }

    public static void glVertexBindingDivisor(int bindingindex, int divisor) {
        GL43.glVertexBindingDivisor(bindingindex, divisor);
    }
}

