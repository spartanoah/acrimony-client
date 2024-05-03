/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.type.CancellableEvent;
import net.minecraft.network.Packet;

public class PacketReceiveEvent
extends CancellableEvent {
    private Packet packet;

    public <T extends Packet> T getPacket() {
        return (T)this.packet;
    }

    public PacketReceiveEvent(Packet packet) {
        this.packet = packet;
    }
}

