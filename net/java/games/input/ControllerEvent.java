/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import net.java.games.input.Controller;

public class ControllerEvent {
    private Controller controller;

    public ControllerEvent(Controller c) {
        this.controller = c;
    }

    public Controller getController() {
        return this.controller;
    }
}

