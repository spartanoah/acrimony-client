/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPLong
extends CPConstant {
    private final long theLong;

    public CPLong(long theLong) {
        this.theLong = theLong;
    }

    public int compareTo(Object obj) {
        if (this.theLong > ((CPLong)obj).theLong) {
            return 1;
        }
        if (this.theLong == ((CPLong)obj).theLong) {
            return 0;
        }
        return -1;
    }

    public long getLong() {
        return this.theLong;
    }

    public String toString() {
        return "" + this.theLong;
    }
}

