/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;

public class KeyPressEvent
extends Event {
    private int key;

    public int getKey() {
        return this.key;
    }

    public KeyPressEvent(int key) {
        this.key = key;
    }
}

