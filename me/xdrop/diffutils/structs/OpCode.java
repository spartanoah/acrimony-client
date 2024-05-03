/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.diffutils.structs;

import me.xdrop.diffutils.structs.EditType;

public final class OpCode {
    public EditType type;
    public int sbeg;
    public int send;
    public int dbeg;
    public int dend;

    public String toString() {
        return this.type.name() + "(" + this.sbeg + "," + this.send + "," + this.dbeg + "," + this.dend + ")";
    }
}

