/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S13PacketDestroyEntities
implements Packet<INetHandlerPlayClient> {
    private int[] entityIDs;

    public S13PacketDestroyEntities() {
    }

    public S13PacketDestroyEntities(int ... entityIDsIn) {
        this.entityIDs = entityIDsIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityIDs = new int[buf.readVarIntFromBuffer()];
        for (int i = 0; i < this.entityIDs.length; ++i) {
            this.entityIDs[i] = buf.readVarIntFromBuffer();
        }
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entityIDs.length);
        for (int i = 0; i < this.entityIDs.length; ++i) {
            buf.writeVarIntToBuffer(this.entityIDs[i]);
        }
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleDestroyEntities(this);
    }

    public int[] getEntityIDs() {
        return this.entityIDs;
    }
}

