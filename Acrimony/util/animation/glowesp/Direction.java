/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.animation.glowesp;

public enum Direction {
    FORWARDS,
    BACKWARDS;


    public Direction opposite() {
        if (this == FORWARDS) {
            return BACKWARDS;
        }
        return FORWARDS;
    }
}

