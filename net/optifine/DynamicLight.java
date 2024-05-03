/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.optifine.DynamicLights;

public class DynamicLight {
    private Entity entity = null;
    private double offsetY = 0.0;
    private double lastPosX = -2.147483648E9;
    private double lastPosY = -2.147483648E9;
    private double lastPosZ = -2.147483648E9;
    private int lastLightLevel = 0;
    private boolean underwater = false;
    private long timeCheckMs = 0L;
    private Set<BlockPos> setLitChunkPos = new HashSet<BlockPos>();
    private BlockPos.MutableBlockPos blockPosMutable = new BlockPos.MutableBlockPos();

    public DynamicLight(Entity entity) {
        this.entity = entity;
        this.offsetY = entity.getEyeHeight();
    }

    public void update(RenderGlobal renderGlobal) {
        if (Config.isDynamicLightsFast()) {
            long i = System.currentTimeMillis();
            if (i < this.timeCheckMs + 500L) {
                return;
            }
            this.timeCheckMs = i;
        }
        double d6 = this.entity.posX - 0.5;
        double d0 = this.entity.posY - 0.5 + this.offsetY;
        double d1 = this.entity.posZ - 0.5;
        int j = DynamicLights.getLightLevel(this.entity);
        double d2 = d6 - this.lastPosX;
        double d3 = d0 - this.lastPosY;
        double d4 = d1 - this.lastPosZ;
        double d5 = 0.1;
        if (Math.abs(d2) > d5 || Math.abs(d3) > d5 || Math.abs(d4) > d5 || this.lastLightLevel != j) {
            this.lastPosX = d6;
            this.lastPosY = d0;
            this.lastPosZ = d1;
            this.lastLightLevel = j;
            this.underwater = false;
            WorldClient world = renderGlobal.getWorld();
            if (world != null) {
                this.blockPosMutable.func_181079_c(MathHelper.floor_double(d6), MathHelper.floor_double(d0), MathHelper.floor_double(d1));
                IBlockState iblockstate = world.getBlockState(this.blockPosMutable);
                Block block = iblockstate.getBlock();
                this.underwater = block == Blocks.water;
            }
            HashSet<BlockPos> set = new HashSet<BlockPos>();
            if (j > 0) {
                EnumFacing enumfacing2 = (MathHelper.floor_double(d6) & 0xF) >= 8 ? EnumFacing.EAST : EnumFacing.WEST;
                EnumFacing enumfacing = (MathHelper.floor_double(d0) & 0xF) >= 8 ? EnumFacing.UP : EnumFacing.DOWN;
                EnumFacing enumfacing1 = (MathHelper.floor_double(d1) & 0xF) >= 8 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                BlockPos blockpos = new BlockPos(d6, d0, d1);
                RenderChunk renderchunk = renderGlobal.getRenderChunk(blockpos);
                BlockPos blockpos1 = this.getChunkPos(renderchunk, blockpos, enumfacing2);
                RenderChunk renderchunk1 = renderGlobal.getRenderChunk(blockpos1);
                BlockPos blockpos2 = this.getChunkPos(renderchunk, blockpos, enumfacing1);
                RenderChunk renderchunk2 = renderGlobal.getRenderChunk(blockpos2);
                BlockPos blockpos3 = this.getChunkPos(renderchunk1, blockpos1, enumfacing1);
                RenderChunk renderchunk3 = renderGlobal.getRenderChunk(blockpos3);
                BlockPos blockpos4 = this.getChunkPos(renderchunk, blockpos, enumfacing);
                RenderChunk renderchunk4 = renderGlobal.getRenderChunk(blockpos4);
                BlockPos blockpos5 = this.getChunkPos(renderchunk4, blockpos4, enumfacing2);
                RenderChunk renderchunk5 = renderGlobal.getRenderChunk(blockpos5);
                BlockPos blockpos6 = this.getChunkPos(renderchunk4, blockpos4, enumfacing1);
                RenderChunk renderchunk6 = renderGlobal.getRenderChunk(blockpos6);
                BlockPos blockpos7 = this.getChunkPos(renderchunk5, blockpos5, enumfacing1);
                RenderChunk renderchunk7 = renderGlobal.getRenderChunk(blockpos7);
                this.updateChunkLight(renderchunk, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk1, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk2, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk3, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk4, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk5, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk6, this.setLitChunkPos, set);
                this.updateChunkLight(renderchunk7, this.setLitChunkPos, set);
            }
            this.updateLitChunks(renderGlobal);
            this.setLitChunkPos = set;
        }
    }

    private BlockPos getChunkPos(RenderChunk renderChunk, BlockPos pos, EnumFacing facing) {
        return renderChunk != null ? renderChunk.func_181701_a(facing) : pos.offset(facing, 16);
    }

    private void updateChunkLight(RenderChunk renderChunk, Set<BlockPos> setPrevPos, Set<BlockPos> setNewPos) {
        if (renderChunk != null) {
            CompiledChunk compiledchunk = renderChunk.getCompiledChunk();
            if (compiledchunk != null && !compiledchunk.isEmpty()) {
                renderChunk.setNeedsUpdate(true);
            }
            BlockPos blockpos = renderChunk.getPosition();
            if (setPrevPos != null) {
                setPrevPos.remove(blockpos);
            }
            if (setNewPos != null) {
                setNewPos.add(blockpos);
            }
        }
    }

    public void updateLitChunks(RenderGlobal renderGlobal) {
        for (BlockPos blockpos : this.setLitChunkPos) {
            RenderChunk renderchunk = renderGlobal.getRenderChunk(blockpos);
            this.updateChunkLight(renderchunk, null, null);
        }
    }

    public Entity getEntity() {
        return this.entity;
    }

    public double getLastPosX() {
        return this.lastPosX;
    }

    public double getLastPosY() {
        return this.lastPosY;
    }

    public double getLastPosZ() {
        return this.lastPosZ;
    }

    public int getLastLightLevel() {
        return this.lastLightLevel;
    }

    public boolean isUnderwater() {
        return this.underwater;
    }

    public double getOffsetY() {
        return this.offsetY;
    }

    public String toString() {
        return "Entity: " + this.entity + ", offsetY: " + this.offsetY;
    }
}

