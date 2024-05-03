/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.player;

import Acrimony.util.IMinecraft;
import Acrimony.util.world.WorldUtil;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.optifine.reflect.Reflector;

public class RotationsUtil
implements IMinecraft {
    public static float[] getRotationsToPosition(double x, double y, double z) {
        double deltaX = x - RotationsUtil.mc.thePlayer.posX;
        double deltaY = y - RotationsUtil.mc.thePlayer.posY - (double)RotationsUtil.mc.thePlayer.getEyeHeight();
        double deltaZ = z - RotationsUtil.mc.thePlayer.posZ;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)Math.toDegrees(-Math.atan2(deltaX, deltaZ));
        float pitch = (float)Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsToPosition(double x, double y, double z, double targetX, double targetY, double targetZ) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.toDegrees(-Math.atan2(dx, dz));
        float pitch = (float)Math.toDegrees(-Math.atan2(dy, horizontalDistance));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsToEntity(EntityLivingBase entity, boolean usePartialTicks) {
        float partialTicks = RotationsUtil.mc.timer.renderPartialTicks;
        double entityX = usePartialTicks ? entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks : entity.posX;
        double entityY = usePartialTicks ? entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks : entity.posY;
        double entityZ = usePartialTicks ? entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks : entity.posZ;
        double yDiff = RotationsUtil.mc.thePlayer.posY - entityY;
        double finalEntityY = yDiff >= 0.0 ? entityY + (double)entity.getEyeHeight() : (-yDiff < (double)RotationsUtil.mc.thePlayer.getEyeHeight() ? RotationsUtil.mc.thePlayer.posY + (double)RotationsUtil.mc.thePlayer.getEyeHeight() : entityY);
        return RotationsUtil.getRotationsToPosition(entityX, finalEntityY, entityZ);
    }

    public static float[] getRotationsToEntity(double x, double y, double z, EntityLivingBase entity, boolean usePartialTicks) {
        float partialTicks = RotationsUtil.mc.timer.renderPartialTicks;
        double entityX = usePartialTicks ? entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks : entity.posX;
        double entityY = usePartialTicks ? entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks : entity.posY;
        double entityZ = usePartialTicks ? entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks : entity.posZ;
        double yDiff = RotationsUtil.mc.thePlayer.posY - entityY;
        double finalEntityY = yDiff >= 0.0 ? entityY + (double)entity.getEyeHeight() : (-yDiff < (double)RotationsUtil.mc.thePlayer.getEyeHeight() ? y + (double)RotationsUtil.mc.thePlayer.getEyeHeight() : entityY);
        return RotationsUtil.getRotationsToPosition(x, y + (double)RotationsUtil.mc.thePlayer.getEyeHeight(), z, entityX, finalEntityY, entityZ);
    }

    public static float[] getRotationsToEntityRandomised(EntityLivingBase entity, boolean usePartialTicks, double randomAmount) {
        float partialTicks = RotationsUtil.mc.timer.renderPartialTicks;
        double entityX = usePartialTicks ? entity.lastTickPosX + (entity.posX + (Math.random() * randomAmount - randomAmount * 0.5) - entity.lastTickPosX) * (double)partialTicks : entity.posX + (Math.random() * randomAmount - randomAmount * 0.5);
        double entityY = usePartialTicks ? entity.lastTickPosY + (entity.posY + (Math.random() * randomAmount - randomAmount * 0.5) - entity.lastTickPosY) * (double)partialTicks : entity.posY + (Math.random() * randomAmount - randomAmount * 0.9);
        double entityZ = usePartialTicks ? entity.lastTickPosZ + (entity.posZ + (Math.random() * randomAmount - randomAmount * 0.5) - entity.lastTickPosZ) * (double)partialTicks : entity.posZ + (Math.random() * randomAmount - randomAmount * 0.5);
        double yDiff = RotationsUtil.mc.thePlayer.posY - entityY;
        double finalEntityY = yDiff >= 0.0 ? entityY + (double)entity.getEyeHeight() : (-yDiff < (double)RotationsUtil.mc.thePlayer.getEyeHeight() ? RotationsUtil.mc.thePlayer.posY + (double)RotationsUtil.mc.thePlayer.getEyeHeight() : entityY);
        return RotationsUtil.getRotationsToPosition(entityX, finalEntityY, entityZ);
    }

    public static boolean raycastEntity(EntityLivingBase target, float yaw, float pitch, float lastYaw, float lastPitch, double reach) {
        Entity entity = mc.getRenderViewEntity();
        Entity pointedEntity = null;
        if (entity != null && RotationsUtil.mc.theWorld != null) {
            float partialTicks = RotationsUtil.mc.timer.renderPartialTicks;
            double d0 = RotationsUtil.mc.playerController.getBlockReachDistance();
            RotationsUtil.mc.objectMouseOver = WorldUtil.raytraceLegit(yaw, pitch, lastYaw, lastPitch);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            int i = 3;
            if (RotationsUtil.mc.playerController.extendedReach()) {
                d0 = 6.0;
                d1 = 6.0;
            } else if (d0 > reach) {
                flag = true;
            }
            if (RotationsUtil.mc.objectMouseOver != null) {
                d1 = RotationsUtil.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }
            float aaaa = lastPitch + (pitch - lastPitch) * partialTicks;
            float bbbb = lastYaw + (yaw - lastYaw) * partialTicks;
            Vec3 vec31 = RotationsUtil.mc.thePlayer.getVectorForRotation(aaaa, bbbb);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0f;
            List<Entity> list = RotationsUtil.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>(){

                @Override
                public boolean apply(Entity p_apply_1_) {
                    return p_apply_1_.canBeCollidedWith();
                }
            }));
            double d2 = d1;
            for (int j = 0; j < list.size(); ++j) {
                double d3;
                Entity entity1 = list.get(j);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (!(d2 >= 0.0)) continue;
                    pointedEntity = entity1;
                    vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                    d2 = 0.0;
                    continue;
                }
                if (movingobjectposition == null || !((d3 = vec3.distanceTo(movingobjectposition.hitVec)) < d2) && d2 != 0.0) continue;
                boolean flag1 = false;
                if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                    flag1 = Reflector.callBoolean(entity1, Reflector.ForgeEntity_canRiderInteract, new Object[0]);
                }
                if (!flag1 && entity1 == entity.ridingEntity) {
                    if (d2 != 0.0) continue;
                    pointedEntity = entity1;
                    vec33 = movingobjectposition.hitVec;
                    continue;
                }
                pointedEntity = entity1;
                vec33 = movingobjectposition.hitVec;
                d2 = d3;
            }
            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > reach) {
                pointedEntity = null;
                RotationsUtil.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }
            if (pointedEntity != null && (d2 < d1 || RotationsUtil.mc.objectMouseOver == null)) {
                RotationsUtil.mc.objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
            }
        }
        return pointedEntity != null && pointedEntity == target;
    }

    public static float getGCD() {
        return (float)(Math.pow((double)RotationsUtil.mc.gameSettings.mouseSensitivity * 0.6 + 0.2, 3.0) * 1.2);
    }
}

