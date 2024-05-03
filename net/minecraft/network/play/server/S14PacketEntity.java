/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

public class S14PacketEntity
implements Packet<INetHandlerPlayClient> {
    protected int entityId;
    protected byte posX;
    protected byte posY;
    protected byte posZ;
    protected byte yaw;
    protected byte pitch;
    protected boolean onGround;
    protected boolean field_149069_g;

    public S14PacketEntity() {
    }

    public S14PacketEntity(int entityIdIn) {
        this.entityId = entityIdIn;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.entityId = buf.readVarIntFromBuffer();
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.entityId);
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        handler.handleEntityMovement(this);
    }

    public String toString() {
        return "Entity_" + super.toString();
    }

    public Entity getEntity(World worldIn) {
        return worldIn.getEntityByID(this.entityId);
    }

    public byte getX() {
        return this.posX;
    }

    public byte getY() {
        return this.posY;
    }

    public byte getZ() {
        return this.posZ;
    }

    public byte getYaw() {
        return this.yaw;
    }

    public byte getPitch() {
        return this.pitch;
    }

    public boolean func_149060_h() {
        return this.field_149069_g;
    }

    public boolean getOnGround() {
        return this.onGround;
    }

    public static class S17PacketEntityLookMove
    extends S14PacketEntity {
        public S17PacketEntityLookMove() {
            this.field_149069_g = true;
        }

        public S17PacketEntityLookMove(int p_i45973_1_, byte p_i45973_2_, byte p_i45973_3_, byte p_i45973_4_, byte p_i45973_5_, byte p_i45973_6_, boolean p_i45973_7_) {
            super(p_i45973_1_);
            this.posX = p_i45973_2_;
            this.posY = p_i45973_3_;
            this.posZ = p_i45973_4_;
            this.yaw = p_i45973_5_;
            this.pitch = p_i45973_6_;
            this.onGround = p_i45973_7_;
            this.field_149069_g = true;
        }

        @Override
        public void readPacketData(PacketBuffer buf) throws IOException {
            super.readPacketData(buf);
            this.posX = buf.readByte();
            this.posY = buf.readByte();
            this.posZ = buf.readByte();
            this.yaw = buf.readByte();
            this.pitch = buf.readByte();
            this.onGround = buf.readBoolean();
        }

        @Override
        public void writePacketData(PacketBuffer buf) throws IOException {
            super.writePacketData(buf);
            buf.writeByte(this.posX);
            buf.writeByte(this.posY);
            buf.writeByte(this.posZ);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }
    }

    public static class S16PacketEntityLook
    extends S14PacketEntity {
        public S16PacketEntityLook() {
            this.field_149069_g = true;
        }

        public S16PacketEntityLook(int entityIdIn, byte yawIn, byte pitchIn, boolean onGroundIn) {
            super(entityIdIn);
            this.yaw = yawIn;
            this.pitch = pitchIn;
            this.field_149069_g = true;
            this.onGround = onGroundIn;
        }

        @Override
        public void readPacketData(PacketBuffer buf) throws IOException {
            super.readPacketData(buf);
            this.yaw = buf.readByte();
            this.pitch = buf.readByte();
            this.onGround = buf.readBoolean();
        }

        @Override
        public void writePacketData(PacketBuffer buf) throws IOException {
            super.writePacketData(buf);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }
    }

    public static class S15PacketEntityRelMove
    extends S14PacketEntity {
        public S15PacketEntityRelMove() {
        }

        public S15PacketEntityRelMove(int entityIdIn, byte x, byte y, byte z, boolean onGroundIn) {
            super(entityIdIn);
            this.posX = x;
            this.posY = y;
            this.posZ = z;
            this.onGround = onGroundIn;
        }

        @Override
        public void readPacketData(PacketBuffer buf) throws IOException {
            super.readPacketData(buf);
            this.posX = buf.readByte();
            this.posY = buf.readByte();
            this.posZ = buf.readByte();
            this.onGround = buf.readBoolean();
        }

        @Override
        public void writePacketData(PacketBuffer buf) throws IOException {
            super.writePacketData(buf);
            buf.writeByte(this.posX);
            buf.writeByte(this.posY);
            buf.writeByte(this.posZ);
            buf.writeBoolean(this.onGround);
        }
    }
}

