/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;

public class S41PacketServerDifficulty
implements Packet<INetHandlerPlayClient> {
    private EnumDifficulty difficulty;
    private boolean difficultyLocked;

    public S41PacketServerDifficulty() {
    }

    public S41PacketServerDifficulty(EnumDifficulty difficultyIn, boolean lockedIn) {
        this.difficulty = difficultyIn;
        this.difficultyLocked = lockedIn;
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleServerDifficulty(this);
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.difficulty = EnumDifficulty.getDifficultyEnum(buf.readUnsignedByte());
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.difficulty.getDifficultyId());
    }

    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    public EnumDifficulty getDifficulty() {
        return this.difficulty;
    }
}

