/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.world;

import Acrimony.util.IMinecraft;
import Acrimony.util.player.MovementUtil;
import Acrimony.util.player.RotationsUtil;
import Acrimony.util.world.BlockInfo;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class WorldUtil
implements IMinecraft {
    public static BlockInfo getBlockInfo(double x, double y, double z, int maxRange) {
        return WorldUtil.getBlockInfo(new BlockPos(x, y, z), maxRange);
    }

    public static BlockInfo getBlockInfo(BlockPos pos, int maxRange) {
        EnumFacing playerDirectionFacing = WorldUtil.getHorizontalFacing(MovementUtil.getPlayerDirection()).getOpposite();
        ArrayList<EnumFacing> facingValues = new ArrayList<EnumFacing>();
        facingValues.add(playerDirectionFacing);
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing == playerDirectionFacing || facing == EnumFacing.UP) continue;
            facingValues.add(facing);
        }
        CopyOnWriteArrayList<BlockPos> aaa = new CopyOnWriteArrayList<BlockPos>();
        aaa.add(pos);
        for (int i = 0; i < maxRange; ++i) {
            ArrayList ccc = new ArrayList(aaa);
            if (!aaa.isEmpty()) {
                for (BlockPos bbbb : aaa) {
                    for (EnumFacing facing : facingValues) {
                        BlockPos n = bbbb.offset(facing);
                        if (WorldUtil.isAirOrLiquid(n)) {
                            aaa.add(n);
                            continue;
                        }
                        return new BlockInfo(n, facing.getOpposite());
                    }
                }
            }
            for (BlockPos dddd : ccc) {
                aaa.remove(dddd);
            }
            ccc.clear();
        }
        return null;
    }

    public static EnumFacing getHorizontalFacing(float yaw) {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double)(yaw * 4.0f / 360.0f) + 0.5) & 3);
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing facing, boolean randomised) {
        Vec3 vec3 = new Vec3(pos);
        double amountX = 0.5;
        double amountY = 0.5;
        double amountZ = 0.5;
        if (randomised) {
            amountX = 0.45 + Math.random() * 0.1;
            amountY = 0.05 + Math.random() * 0.1;
            amountZ = 0.45 + Math.random() * 0.1;
        }
        if (facing == EnumFacing.UP) {
            vec3 = vec3.addVector(amountX, 1.0, amountZ);
        } else if (facing == EnumFacing.DOWN) {
            vec3 = vec3.addVector(amountX, 0.0, amountZ);
        } else if (facing == EnumFacing.EAST) {
            vec3 = vec3.addVector(1.0, amountY, amountZ);
        } else if (facing == EnumFacing.WEST) {
            vec3 = vec3.addVector(0.0, amountY, amountZ);
        } else if (facing == EnumFacing.NORTH) {
            vec3 = vec3.addVector(amountX, amountY, 0.0);
        } else if (facing == EnumFacing.SOUTH) {
            vec3 = vec3.addVector(amountX, amountY, 1.0);
        }
        return vec3;
    }

    public static Vec3 getVec3ClosestFromRots(BlockPos pos, EnumFacing facing, boolean randomised, float yaw, float pitch) {
        Vec3 originalVec3;
        double smallestDiff = Double.MAX_VALUE;
        Vec3 finalVec3 = null;
        Vec3 modifiedVec3 = originalVec3 = new Vec3(pos);
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            boolean y = facing == EnumFacing.UP;
            double random1 = randomised ? Math.random() * 0.01 - 0.005 : 0.0;
            double random2 = randomised ? Math.random() * 0.04 : 0.0;
            for (double amount1 = 0.05; amount1 <= 0.95; amount1 += 0.025) {
                for (double amount2 = 0.05; amount2 <= 0.95; amount2 += 0.025) {
                    double pitchDiff;
                    modifiedVec3 = originalVec3.addVector(amount1 + random1, (double)y, amount2 + random2);
                    float[] rots = RotationsUtil.getRotationsToPosition(modifiedVec3.xCoord, modifiedVec3.yCoord, modifiedVec3.zCoord);
                    double yawDiff = Math.abs(rots[0] - yaw);
                    double diff = Math.hypot(yawDiff, pitchDiff = (double)Math.abs(rots[1] - pitch));
                    if (!(diff < smallestDiff)) continue;
                    smallestDiff = diff;
                    finalVec3 = modifiedVec3;
                }
            }
        } else {
            double random1 = randomised ? Math.random() * 0.01 - 0.005 : 0.0;
            double random2 = randomised ? Math.random() * 0.04 : 0.0;
            for (double amount = 0.05; amount <= 0.95; amount += 0.025) {
                if (facing == EnumFacing.EAST) {
                    modifiedVec3 = originalVec3.addVector(1.0, 0.05 + random2, amount + random1);
                } else if (facing == EnumFacing.WEST) {
                    modifiedVec3 = originalVec3.addVector(0.0, 0.05 + random2, amount + random1);
                } else if (facing == EnumFacing.NORTH) {
                    modifiedVec3 = originalVec3.addVector(amount + random1, 0.05 + random2, 0.0);
                } else if (facing == EnumFacing.SOUTH) {
                    modifiedVec3 = originalVec3.addVector(amount + random1, 0.05 + random2, 1.0);
                }
                float[] rots = RotationsUtil.getRotationsToPosition(modifiedVec3.xCoord, modifiedVec3.yCoord, modifiedVec3.zCoord);
                double yawDiff = Math.abs(rots[0] - yaw);
                double pitchDiff = Math.abs(rots[1] - pitch);
                double diff = Math.hypot(yawDiff, pitchDiff);
                if (!(diff < smallestDiff)) continue;
                smallestDiff = diff;
                finalVec3 = modifiedVec3;
            }
        }
        return finalVec3;
    }

    public static boolean isAir(BlockPos pos) {
        Block block = WorldUtil.mc.theWorld.getBlockState(pos).getBlock();
        return block instanceof BlockAir;
    }

    public static boolean isAirOrLiquid(BlockPos pos) {
        Block block = WorldUtil.mc.theWorld.getBlockState(pos).getBlock();
        return block instanceof BlockAir || block instanceof BlockLiquid;
    }

    public static boolean isOverAirOrLiquid() {
        return WorldUtil.isAirOrLiquid(new BlockPos(WorldUtil.mc.thePlayer.posX, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ));
    }

    public static MovingObjectPosition raytrace(float yaw, float pitch) {
        float blockReachDistance = WorldUtil.mc.playerController.getBlockReachDistance();
        Vec3 vec3 = new Vec3(WorldUtil.mc.thePlayer.posX, WorldUtil.mc.thePlayer.posY + (double)WorldUtil.mc.thePlayer.getEyeHeight(), WorldUtil.mc.thePlayer.posZ);
        Vec3 vec31 = WorldUtil.mc.thePlayer.getVectorForRotation(pitch, yaw);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * 1000.0, vec31.yCoord * 1000.0, vec31.zCoord * 1000.0);
        return WorldUtil.mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    public static MovingObjectPosition raytraceLegit(float yaw, float pitch, float lastYaw, float lastPitch) {
        float partialTicks = WorldUtil.mc.timer.renderPartialTicks;
        float blockReachDistance = WorldUtil.mc.playerController.getBlockReachDistance();
        Vec3 vec3 = WorldUtil.mc.thePlayer.getPositionEyes(partialTicks);
        float f = lastPitch + (pitch - lastPitch) * partialTicks;
        float f1 = lastYaw + (yaw - lastYaw) * partialTicks;
        Vec3 vec31 = WorldUtil.mc.thePlayer.getVectorForRotation(f, f1);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * (double)blockReachDistance, vec31.yCoord * (double)blockReachDistance, vec31.zCoord * (double)blockReachDistance);
        return WorldUtil.mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    public static boolean isBlockUnder() {
        for (int y = (int)WorldUtil.mc.thePlayer.posY; y >= 0; --y) {
            if (WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX, (double)y, WorldUtil.mc.thePlayer.posZ)).getBlock() instanceof BlockAir) continue;
            return true;
        }
        return false;
    }

    public static boolean isBlockUnder(int distance) {
        for (int y = (int)WorldUtil.mc.thePlayer.posY; y >= (int)WorldUtil.mc.thePlayer.posY - distance; --y) {
            if (WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX, (double)y, WorldUtil.mc.thePlayer.posZ)).getBlock() instanceof BlockAir) continue;
            return true;
        }
        return false;
    }

    public static boolean negativeExpand(double negativeExpandValue) {
        return WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX + negativeExpandValue, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX - negativeExpandValue, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX - negativeExpandValue, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX + negativeExpandValue, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(WorldUtil.mc.thePlayer.posX, WorldUtil.mc.thePlayer.posY - 1.0, WorldUtil.mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir;
    }

    public static boolean negativeExpand(double posX, double posY, double posZ, double negativeExpandValue) {
        return WorldUtil.mc.theWorld.getBlockState(new BlockPos(posX + negativeExpandValue, posY - 1.0, posZ + negativeExpandValue)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(posX - negativeExpandValue, posY - 1.0, posZ - negativeExpandValue)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(posX - negativeExpandValue, posY - 1.0, posZ)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(posX + negativeExpandValue, posY - 1.0, posZ)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(posX, posY - 1.0, posZ + negativeExpandValue)).getBlock() instanceof BlockAir && WorldUtil.mc.theWorld.getBlockState(new BlockPos(posX, posY - 1.0, posZ - negativeExpandValue)).getBlock() instanceof BlockAir;
    }
}

