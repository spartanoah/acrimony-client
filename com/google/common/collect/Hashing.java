/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import javax.annotation.Nullable;

@GwtCompatible
final class Hashing {
    private static final int C1 = -862048943;
    private static final int C2 = 461845907;
    private static int MAX_TABLE_SIZE = 0x40000000;

    private Hashing() {
    }

    static int smear(int hashCode) {
        return 461845907 * Integer.rotateLeft(hashCode * -862048943, 15);
    }

    static int smearedHash(@Nullable Object o) {
        return Hashing.smear(o == null ? 0 : o.hashCode());
    }

    static int closedTableSize(int expectedEntries, double loadFactor) {
        int tableSize;
        if ((expectedEntries = Math.max(expectedEntries, 2)) > (int)(loadFactor * (double)(tableSize = Integer.highestOneBit(expectedEntries)))) {
            return (tableSize <<= 1) > 0 ? tableSize : MAX_TABLE_SIZE;
        }
        return tableSize;
    }

    static boolean needsResizing(int size, int tableSize, double loadFactor) {
        return (double)size > loadFactor * (double)tableSize && tableSize < MAX_TABLE_SIZE;
    }
}

