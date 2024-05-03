/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.diffutils.structs;

import me.xdrop.diffutils.structs.EditType;

public final class EditOp {
    public EditType type;
    public int spos;
    public int dpos;

    public String toString() {
        return this.type.name() + "(" + this.spos + "," + this.dpos + ")";
    }
}

