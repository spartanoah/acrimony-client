/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.PointerWrapper;

public abstract class PointerWrapperAbstract
implements PointerWrapper {
    protected final long pointer;

    protected PointerWrapperAbstract(long pointer) {
        this.pointer = pointer;
    }

    public boolean isValid() {
        return this.pointer != 0L;
    }

    public final void checkValid() {
        if (LWJGLUtil.DEBUG && !this.isValid()) {
            throw new IllegalStateException("This " + this.getClass().getSimpleName() + " pointer is not valid.");
        }
    }

    @Override
    public final long getPointer() {
        this.checkValid();
        return this.pointer;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PointerWrapperAbstract)) {
            return false;
        }
        PointerWrapperAbstract that = (PointerWrapperAbstract)o;
        return this.pointer == that.pointer;
    }

    public int hashCode() {
        return (int)(this.pointer ^ this.pointer >>> 32);
    }

    public String toString() {
        return this.getClass().getSimpleName() + " pointer (0x" + Long.toHexString(this.pointer).toUpperCase() + ")";
    }
}

