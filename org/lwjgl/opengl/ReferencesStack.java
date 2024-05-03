/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opengl;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.References;

class ReferencesStack {
    private References[] references_stack;
    private int stack_pos;

    public References getReferences() {
        return this.references_stack[this.stack_pos];
    }

    public void pushState() {
        int pos;
        if ((pos = ++this.stack_pos) == this.references_stack.length) {
            this.growStack();
        }
        this.references_stack[pos].copy(this.references_stack[pos - 1], -1);
    }

    public References popState(int mask) {
        References result = this.references_stack[this.stack_pos--];
        this.references_stack[this.stack_pos].copy(result, ~mask);
        result.clear();
        return result;
    }

    private void growStack() {
        References[] new_references_stack = new References[this.references_stack.length + 1];
        System.arraycopy(this.references_stack, 0, new_references_stack, 0, this.references_stack.length);
        this.references_stack = new_references_stack;
        this.references_stack[this.references_stack.length - 1] = new References(GLContext.getCapabilities());
    }

    ReferencesStack() {
        ContextCapabilities caps = GLContext.getCapabilities();
        this.references_stack = new References[1];
        this.stack_pos = 0;
        for (int i = 0; i < this.references_stack.length; ++i) {
            this.references_stack[i] = new References(caps);
        }
    }
}

