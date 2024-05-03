/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.lwjgl.opencl;

import org.lwjgl.opencl.CLObject;

abstract class CLObjectRetainable
extends CLObject {
    private int refCount;

    protected CLObjectRetainable(long pointer) {
        super(pointer);
        if (super.isValid()) {
            this.refCount = 1;
        }
    }

    public final int getReferenceCount() {
        return this.refCount;
    }

    @Override
    public final boolean isValid() {
        return this.refCount > 0;
    }

    int retain() {
        this.checkValid();
        return ++this.refCount;
    }

    int release() {
        this.checkValid();
        return --this.refCount;
    }
}

