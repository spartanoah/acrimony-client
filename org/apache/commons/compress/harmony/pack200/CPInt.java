/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPInt
extends CPConstant {
    private final int theInt;

    public CPInt(int theInt) {
        this.theInt = theInt;
    }

    public int compareTo(Object obj) {
        if (this.theInt > ((CPInt)obj).theInt) {
            return 1;
        }
        if (this.theInt == ((CPInt)obj).theInt) {
            return 0;
        }
        return -1;
    }

    public int getInt() {
        return this.theInt;
    }
}

