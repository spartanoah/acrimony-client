/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.type.CancellableEvent;

public class ChatSendEvent
extends CancellableEvent {
    private String message;

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatSendEvent(String message) {
        this.message = message;
    }
}

