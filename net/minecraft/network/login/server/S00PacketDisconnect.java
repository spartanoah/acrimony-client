/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.util.IChatComponent;

public class S00PacketDisconnect
implements Packet<INetHandlerLoginClient> {
    private IChatComponent reason;

    public S00PacketDisconnect() {
    }

    public S00PacketDisconnect(IChatComponent reasonIn) {
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
    public void processPacket(INetHandlerLoginClient handler) {
        handler.handleDisconnect(this);
    }

    public IChatComponent func_149603_c() {
        return this.reason;
    }
}

