/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.fastutil.ints;

public interface IntHash {

    public static interface Strategy {
        public int hashCode(int var1);

        public boolean equals(int var1, int var2);
    }
}

