/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.type.CancellableEvent;
import net.minecraft.network.Packet;

public class PacketSendEvent
extends CancellableEvent {
    private Packet packet;

    public <T extends Packet> T getPacket() {
        return (T)this.packet;
    }

    public PacketSendEvent(Packet packet) {
        this.packet = packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}

