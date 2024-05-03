/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPFloat
extends CPConstant {
    private final float theFloat;

    public CPFloat(float theFloat) {
        this.theFloat = theFloat;
    }

    public int compareTo(Object obj) {
        return Float.compare(this.theFloat, ((CPFloat)obj).theFloat);
    }

    public float getFloat() {
        return this.theFloat;
    }
}

