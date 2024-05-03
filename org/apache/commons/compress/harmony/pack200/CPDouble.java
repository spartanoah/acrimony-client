/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPDouble
extends CPConstant {
    private final double theDouble;

    public CPDouble(double theDouble) {
        this.theDouble = theDouble;
    }

    public int compareTo(Object obj) {
        return Double.compare(this.theDouble, ((CPDouble)obj).theDouble);
    }

    public double getDouble() {
        return this.theDouble;
    }
}

