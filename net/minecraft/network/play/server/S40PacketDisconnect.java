/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IChatComponent;

public class S40PacketDisconnect
implements Packet<INetHandlerPlayClient> {
    private IChatComponent reason;

    public S40PacketDisconnect() {
    }

    public S40PacketDisconnect(IChatComponent reasonIn) {
        this.reason = reasonIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.reason = buf.readChatComponent();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeChatComponent(this.reason);
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleDisconnect(this);
    }

    public IChatComponent getReason() {
        return this.reason;
    }
}

