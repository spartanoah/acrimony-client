/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.handler.packet;

import Acrimony.util.misc.TimerUtil;
import net.minecraft.network.Packet;

public class DelayedPacket {
    private final Packet packet;
    private final TimerUtil timer;

    public DelayedPacket(Packet packet) {
        this.packet = packet;
        this.timer = new TimerUtil();
    }

    public <T extends Packet> T getPacket() {
        return (T)this.packet;
    }

    public TimerUtil getTimer() {
        return this.timer;
    }
}

