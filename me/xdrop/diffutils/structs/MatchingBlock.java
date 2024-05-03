/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.diffutils.structs;

public final class MatchingBlock {
    public int spos;
    public int dpos;
    public int length;

    public String toString() {
        return "(" + this.spos + "," + this.dpos + "," + this.length + ")";
    }
}

