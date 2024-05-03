/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S28PacketEffect;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.WorldServer;

public class WorldManager
implements IWorldAccess {
    private MinecraftServer mcServer;
    private WorldServer theWorldServer;

    public WorldManager(MinecraftServer p_i1517_1_, WorldServer p_i1517_2_) {
        this.mcServer = p_i1517_1_;
        this.theWorldServer = p_i1517_2_;
    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int ... p_180442_15_) {
    }

    @Override
    public void onEntityAdded(Entity entityIn) {
        this.theWorldServer.getEntityTracker().trackEntity(entityIn);
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {
        this.theWorldServer.getEntityTracker().untrackEntity(entityIn);
        this.theWorldServer.getScoreboard().func_181140_a(entityIn);
    }

    @Override
    public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {
        this.mcServer.getConfigurationManager().sendToAllNear(x, y, z, volume > 1.0f ? (double)(16.0f * volume) : 16.0, this.theWorldServer.provider.getDimensionId(), new S29PacketSoundEffect(soundName, x, y, z, volume, pitch));
    }

    @Override
    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {
        this.mcServer.getConfigurationManager().sendToAllNearExcept(except, x, y, z, volume > 1.0f ? (double)(16.0f * volume) : 16.0, this.theWorldServer.provider.getDimensionId(), new S29PacketSoundEffect(soundName, x, y, z, volume, pitch));
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    @Override
    public void markBlockForUpdate(BlockPos pos) {
        this.theWorldServer.getPlayerManager().markBlockForUpdate(pos);
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
    }

    @Override
    public void playRecord(String recordName, BlockPos blockPosIn) {
    }

    @Override
    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_) {
        this.mcServer.getConfigurationManager().sendToAllNearExcept(player, blockPosIn.getX(), blockPosIn.getY(), blockPosIn.getZ(), 64.0, this.theWorldServer.provider.getDimensionId(), new S28PacketEffect(sfxType, blockPosIn, p_180439_4_, false));
    }

    @Override
    public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_) {
        this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S28PacketEffect(p_180440_1_, p_180440_2_, p_180440_3_, true));
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        for (EntityPlayerMP entityplayermp : this.mcServer.getConfigurationManager().func_181057_v()) {
            double d2;
            double d1;
            double d0;
            if (entityplayermp == null || entityplayermp.worldObj != this.theWorldServer || entityplayermp.getEntityId() == breakerId || !((d0 = (double)pos.getX() - entityplayermp.posX) * d0 + (d1 = (double)pos.getY() - entityplayermp.posY) * d1 + (d2 = (double)pos.getZ() - entityplayermp.posZ) * d2 < 1024.0)) continue;
            entityplayermp.playerNetServerHandler.sendPacket(new S25PacketBlockBreakAnim(breakerId, pos, progress));
        }
    }
}

