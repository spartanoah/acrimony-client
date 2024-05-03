/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S2EPacketCloseWindow
implements Packet<INetHandlerPlayClient> {
    private int windowId;

    public S2EPacketCloseWindow() {
    }

    public S2EPacketCloseWindow(int windowIdIn) {
        this.windowId = windowIdIn;
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleCloseWindow(this);
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readUnsignedByte();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
    }
}

