/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S09PacketHeldItemChange
implements Packet<INetHandlerPlayClient> {
    private int heldItemHotbarIndex;

    public S09PacketHeldItemChange() {
    }

    public S09PacketHeldItemChange(int hotbarIndexIn) {
        this.heldItemHotbarIndex = hotbarIndexIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.heldItemHotbarIndex = buf.readByte();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.heldItemHotbarIndex);
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleHeldItemChange(this);
    }

    public int getHeldItemHotbarIndex() {
        return this.heldItemHotbarIndex;
    }
}

