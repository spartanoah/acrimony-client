/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.ConstantPoolEntry;

public class CPUTF8
extends ConstantPoolEntry
implements Comparable {
    private final String string;

    public CPUTF8(String string) {
        this.string = string;
    }

    public int compareTo(Object arg0) {
        return this.string.compareTo(((CPUTF8)arg0).string);
    }

    public String toString() {
        return this.string;
    }

    public String getUnderlyingString() {
        return this.string;
    }
}

