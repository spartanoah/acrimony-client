/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.java.games.input;

import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.WinTabComponent;
import net.java.games.input.WinTabContext;
import net.java.games.input.WinTabPacket;

public class WinTabCursorComponent
extends WinTabComponent {
    private int index;

    protected WinTabCursorComponent(WinTabContext context, int parentDevice, String name, Component.Identifier id, int index) {
        super(context, parentDevice, name, id);
        this.index = index;
    }

    public Event processPacket(WinTabPacket packet) {
        Event newEvent = null;
        if (packet.PK_CURSOR == this.index && this.lastKnownValue == 0.0f) {
            this.lastKnownValue = 1.0f;
            newEvent = new Event();
            newEvent.set(this, this.lastKnownValue, packet.PK_TIME * 1000L);
        } else if (packet.PK_CURSOR != this.index && this.lastKnownValue == 1.0f) {
            this.lastKnownValue = 0.0f;
            newEvent = new Event();
            newEvent.set(this, this.lastKnownValue, packet.PK_TIME * 1000L);
        }
        return newEvent;
    }
}

