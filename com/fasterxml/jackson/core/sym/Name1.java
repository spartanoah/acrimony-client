/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.sym;

import com.fasterxml.jackson.core.sym.Name;

public final class Name1
extends Name {
    private static final Name1 EMPTY = new Name1("", 0, 0);
    private final int q;

    Name1(String name, int hash, int quad) {
        super(name, hash);
        this.q = quad;
    }

    public static Name1 getEmptyName() {
        return EMPTY;
    }

    @Override
    public boolean equals(int quad) {
        return quad == this.q;
    }

    @Override
    public boolean equals(int quad1, int quad2) {
        return quad1 == this.q && quad2 == 0;
    }

    @Override
    public boolean equals(int q1, int q2, int q3) {
        return false;
    }

    @Override
    public boolean equals(int[] quads, int qlen) {
        return qlen == 1 && quads[0] == this.q;
    }
}

