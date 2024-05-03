/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.ai.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class EntityMinecart
extends Entity
implements IWorldNameable {
    private boolean isInReverse;
    private String entityName;
    private static final int[][][] matrix = new int[][][]{new int[][]{{0, 0, -1}, {0, 0, 1}}, new int[][]{{-1, 0, 0}, {1, 0, 0}}, new int[][]{{-1, -1, 0}, {1, 0, 0}}, new int[][]{{-1, 0, 0}, {1, -1, 0}}, new int[][]{{0, 0, -1}, {0, -1, 1}}, new int[][]{{0, -1, -1}, {0, 0, 1}}, new int[][]{{0, 0, 1}, {1, 0, 0}}, new int[][]{{0, 0, 1}, {-1, 0, 0}}, new int[][]{{0, 0, -1}, {-1, 0, 0}}, new int[][]{{0, 0, -1}, {1, 0, 0}}};
    private int turnProgress;
    private double minecartX;
    private double minecartY;
    private double minecartZ;
    private double minecartYaw;
    private double minecartPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public EntityMinecart(World worldIn) {
        super(worldIn);
        this.preventEntitySpawning = true;
        this.setSize(0.98f, 0.7f);
    }

    public static EntityMinecart func_180458_a(World worldIn, double p_180458_1_, double p_180458_3_, double p_180458_5_, EnumMinecartType p_180458_7_) {
        switch (p_180458_7_) {
            case CHEST: {
                return new EntityMinecartChest(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
            }
            case FURNACE: {
                return new EntityMinecartFurnace(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
            }
            case TNT: {
                return new EntityMinecartTNT(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
            }
            case SPAWNER: {
                return new EntityMinecartMobSpawner(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
            }
            case HOPPER: {
                return new EntityMinecartHopper(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
            }
            case COMMAND_BLOCK: {
                return new EntityMinecartCommandBlock(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
            }
        }
        return new EntityMinecartEmpty(worldIn, p_180458_1_, p_180458_3_, p_180458_5_);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(17, new Integer(0));
        this.dataWatcher.addObject(18, new Integer(1));
        this.dataWatcher.addObject(19, new Float(0.0f));
        this.dataWatcher.addObject(20, new Integer(0));
        this.dataWatcher.addObject(21, new Integer(6));
        this.dataWatcher.addObject(22, (byte)0);
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    public EntityMinecart(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    public double getMountedYOffset() {
        return 0.0;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.worldObj.isRemote && !this.isDead) {
            boolean flag;
            if (this.isEntityInvulnerable(source)) {
                return false;
            }
            this.setRollingDirection(-this.getRollingDirection());
            this.setRollingAmplitude(10);
            this.setBeenAttacked();
            this.setDamage(this.getDamage() + amount * 10.0f);
            boolean bl = flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;
            if (flag || this.getDamage() > 40.0f) {
                if (this.riddenByEntity != null) {
                    this.riddenByEntity.mountEntity(null);
                }
                if (flag && !this.hasCustomName()) {
                    this.setDead();
                } else {
                    this.killMinecart(source);
                }
            }
            return true;
        }
        return true;
    }

    public void killMinecart(DamageSource p_94095_1_) {
        this.setDead();
        if (this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops")) {
            ItemStack itemstack = new ItemStack(Items.minecart, 1);
            if (this.entityName != null) {
                itemstack.setStackDisplayName(this.entityName);
            }
            this.entityDropItem(itemstack, 0.0f);
        }
    }

    @Override
    public void performHurtAnimation() {
        this.setRollingDirection(-this.getRollingDirection());
        this.setRollingAmplitude(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0f);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public void setDead() {
        super.setDead();
    }

    @Override
    public void onUpdate() {
        if (this.getRollingAmplitude() > 0) {
            this.setRollingAmplitude(this.getRollingAmplitude() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        if (this.posY < -64.0) {
            this.kill();
        }
        if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer) {
            this.worldObj.theProfiler.startSection("portal");
            MinecraftServer minecraftserver = ((WorldServer)this.worldObj).getMinecraftServer();
            int i = this.getMaxInPortalTime();
            if (this.inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (this.ridingEntity == null && this.portalCounter++ >= i) {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        int j = this.worldObj.provider.getDimensionId() == -1 ? 0 : -1;
                        this.travelToDimension(j);
                    }
                    this.inPortal = false;
                }
            } else {
                if (this.portalCounter > 0) {
                    this.portalCounter -= 4;
                }
                if (this.portalCounter < 0) {
                    this.portalCounter = 0;
                }
            }
            if (this.timeUntilPortal > 0) {
                --this.timeUntilPortal;
            }
            this.worldObj.theProfiler.endSection();
        }
        if (this.worldObj.isRemote) {
            if (this.turnProgress > 0) {
                double d4 = this.posX + (this.minecartX - this.posX) / (double)this.turnProgress;
                double d5 = this.posY + (this.minecartY - this.posY) / (double)this.turnProgress;
                double d6 = this.posZ + (this.minecartZ - this.posZ) / (double)this.turnProgress;
                double d1 = MathHelper.wrapAngleTo180_double(this.minecartYaw - (double)this.rotationYaw);
                this.rotationYaw = (float)((double)this.rotationYaw + d1 / (double)this.turnProgress);
                this.rotationPitch = (float)((double)this.rotationPitch + (this.minecartPitch - (double)this.rotationPitch) / (double)this.turnProgress);
                --this.turnProgress;
                this.setPosition(d4, d5, d6);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
        } else {
            double d3;
            BlockPos blockpos;
            IBlockState iblockstate;
            int i1;
            int l;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY -= (double)0.04f;
            int k = MathHelper.floor_double(this.posX);
            if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(k, (l = MathHelper.floor_double(this.posY)) - 1, i1 = MathHelper.floor_double(this.posZ)))) {
                --l;
            }
            if (BlockRailBase.isRailBlock(iblockstate = this.worldObj.getBlockState(blockpos = new BlockPos(k, l, i1)))) {
                this.func_180460_a(blockpos, iblockstate);
                if (iblockstate.getBlock() == Blocks.activator_rail) {
                    this.onActivatorRailPass(k, l, i1, iblockstate.getValue(BlockRailPowered.POWERED));
                }
            } else {
                this.moveDerailedMinecart();
            }
            this.doBlockCollisions();
            this.rotationPitch = 0.0f;
            double d0 = this.prevPosX - this.posX;
            double d2 = this.prevPosZ - this.posZ;
            if (d0 * d0 + d2 * d2 > 0.001) {
                this.rotationYaw = (float)(MathHelper.func_181159_b(d2, d0) * 180.0 / Math.PI);
                if (this.isInReverse) {
                    this.rotationYaw += 180.0f;
                }
            }
            if ((d3 = (double)MathHelper.wrapAngleTo180_float(this.rotationYaw - this.prevRotationYaw)) < -170.0 || d3 >= 170.0) {
                this.rotationYaw += 180.0f;
                this.isInReverse = !this.isInReverse;
            }
            this.setRotation(this.rotationYaw, this.rotationPitch);
            for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.2f, 0.0, 0.2f))) {
                if (entity == this.riddenByEntity || !entity.canBePushed() || !(entity instanceof EntityMinecart)) continue;
                entity.applyEntityCollision(this);
            }
            if (this.riddenByEntity != null && this.riddenByEntity.isDead) {
                if (this.riddenByEntity.ridingEntity == this) {
                    this.riddenByEntity.ridingEntity = null;
                }
                this.riddenByEntity = null;
            }
            this.handleWaterMovement();
        }
    }

    protected double getMaximumSpeed() {
        return 0.4;
    }

    public void onActivatorRailPass(int x, int y, int z, boolean receivingPower) {
    }

    protected void moveDerailedMinecart() {
        double d0 = this.getMaximumSpeed();
        this.motionX = MathHelper.clamp_double(this.motionX, -d0, d0);
        this.motionZ = MathHelper.clamp_double(this.motionZ, -d0, d0);
        if (this.onGround) {
            this.motionX *= 0.5;
            this.motionY *= 0.5;
            this.motionZ *= 0.5;
        }
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        if (!this.onGround) {
            this.motionX *= (double)0.95f;
            this.motionY *= (double)0.95f;
            this.motionZ *= (double)0.95f;
        }
    }

    protected void func_180460_a(BlockPos p_180460_1_, IBlockState p_180460_2_) {
        double d6;
        double d5;
        this.fallDistance = 0.0f;
        Vec3 vec3 = this.func_70489_a(this.posX, this.posY, this.posZ);
        this.posY = p_180460_1_.getY();
        boolean flag = false;
        boolean flag1 = false;
        BlockRailBase blockrailbase = (BlockRailBase)p_180460_2_.getBlock();
        if (blockrailbase == Blocks.golden_rail) {
            flag = p_180460_2_.getValue(BlockRailPowered.POWERED);
            flag1 = !flag;
        }
        double d0 = 0.0078125;
        BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = p_180460_2_.getValue(blockrailbase.getShapeProperty());
        switch (blockrailbase$enumraildirection) {
            case ASCENDING_EAST: {
                this.motionX -= 0.0078125;
                this.posY += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                this.motionX += 0.0078125;
                this.posY += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                this.motionZ += 0.0078125;
                this.posY += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                this.motionZ -= 0.0078125;
                this.posY += 1.0;
            }
        }
        int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
        double d1 = aint[1][0] - aint[0][0];
        double d2 = aint[1][2] - aint[0][2];
        double d3 = Math.sqrt(d1 * d1 + d2 * d2);
        double d4 = this.motionX * d1 + this.motionZ * d2;
        if (d4 < 0.0) {
            d1 = -d1;
            d2 = -d2;
        }
        if ((d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ)) > 2.0) {
            d5 = 2.0;
        }
        this.motionX = d5 * d1 / d3;
        this.motionZ = d5 * d2 / d3;
        if (this.riddenByEntity instanceof EntityLivingBase && (d6 = (double)((EntityLivingBase)this.riddenByEntity).moveForward) > 0.0) {
            double d7 = -Math.sin(this.riddenByEntity.rotationYaw * (float)Math.PI / 180.0f);
            double d8 = Math.cos(this.riddenByEntity.rotationYaw * (float)Math.PI / 180.0f);
            double d9 = this.motionX * this.motionX + this.motionZ * this.motionZ;
            if (d9 < 0.01) {
                this.motionX += d7 * 0.1;
                this.motionZ += d8 * 0.1;
                flag1 = false;
            }
        }
        if (flag1) {
            double d17 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d17 < 0.03) {
                this.motionX *= 0.0;
                this.motionY *= 0.0;
                this.motionZ *= 0.0;
            } else {
                this.motionX *= 0.5;
                this.motionY *= 0.0;
                this.motionZ *= 0.5;
            }
        }
        double d18 = 0.0;
        double d19 = (double)p_180460_1_.getX() + 0.5 + (double)aint[0][0] * 0.5;
        double d20 = (double)p_180460_1_.getZ() + 0.5 + (double)aint[0][2] * 0.5;
        double d21 = (double)p_180460_1_.getX() + 0.5 + (double)aint[1][0] * 0.5;
        double d10 = (double)p_180460_1_.getZ() + 0.5 + (double)aint[1][2] * 0.5;
        d1 = d21 - d19;
        d2 = d10 - d20;
        if (d1 == 0.0) {
            this.posX = (double)p_180460_1_.getX() + 0.5;
            d18 = this.posZ - (double)p_180460_1_.getZ();
        } else if (d2 == 0.0) {
            this.posZ = (double)p_180460_1_.getZ() + 0.5;
            d18 = this.posX - (double)p_180460_1_.getX();
        } else {
            double d11 = this.posX - d19;
            double d12 = this.posZ - d20;
            d18 = (d11 * d1 + d12 * d2) * 2.0;
        }
        this.posX = d19 + d1 * d18;
        this.posZ = d20 + d2 * d18;
        this.setPosition(this.posX, this.posY, this.posZ);
        double d22 = this.motionX;
        double d23 = this.motionZ;
        if (this.riddenByEntity != null) {
            d22 *= 0.75;
            d23 *= 0.75;
        }
        double d13 = this.getMaximumSpeed();
        d22 = MathHelper.clamp_double(d22, -d13, d13);
        d23 = MathHelper.clamp_double(d23, -d13, d13);
        this.moveEntity(d22, 0.0, d23);
        if (aint[0][1] != 0 && MathHelper.floor_double(this.posX) - p_180460_1_.getX() == aint[0][0] && MathHelper.floor_double(this.posZ) - p_180460_1_.getZ() == aint[0][2]) {
            this.setPosition(this.posX, this.posY + (double)aint[0][1], this.posZ);
        } else if (aint[1][1] != 0 && MathHelper.floor_double(this.posX) - p_180460_1_.getX() == aint[1][0] && MathHelper.floor_double(this.posZ) - p_180460_1_.getZ() == aint[1][2]) {
            this.setPosition(this.posX, this.posY + (double)aint[1][1], this.posZ);
        }
        this.applyDrag();
        Vec3 vec31 = this.func_70489_a(this.posX, this.posY, this.posZ);
        if (vec31 != null && vec3 != null) {
            double d14 = (vec3.yCoord - vec31.yCoord) * 0.05;
            d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d5 > 0.0) {
                this.motionX = this.motionX / d5 * (d5 + d14);
                this.motionZ = this.motionZ / d5 * (d5 + d14);
            }
            this.setPosition(this.posX, vec31.yCoord, this.posZ);
        }
        int j = MathHelper.floor_double(this.posX);
        int i = MathHelper.floor_double(this.posZ);
        if (j != p_180460_1_.getX() || i != p_180460_1_.getZ()) {
            d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.motionX = d5 * (double)(j - p_180460_1_.getX());
            this.motionZ = d5 * (double)(i - p_180460_1_.getZ());
        }
        if (flag) {
            double d15 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d15 > 0.01) {
                double d16 = 0.06;
                this.motionX += this.motionX / d15 * d16;
                this.motionZ += this.motionZ / d15 * d16;
            } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.EAST_WEST) {
                if (this.worldObj.getBlockState(p_180460_1_.west()).getBlock().isNormalCube()) {
                    this.motionX = 0.02;
                } else if (this.worldObj.getBlockState(p_180460_1_.east()).getBlock().isNormalCube()) {
                    this.motionX = -0.02;
                }
            } else if (blockrailbase$enumraildirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
                if (this.worldObj.getBlockState(p_180460_1_.north()).getBlock().isNormalCube()) {
                    this.motionZ = 0.02;
                } else if (this.worldObj.getBlockState(p_180460_1_.south()).getBlock().isNormalCube()) {
                    this.motionZ = -0.02;
                }
            }
        }
    }

    protected void applyDrag() {
        if (this.riddenByEntity != null) {
            this.motionX *= (double)0.997f;
            this.motionY *= 0.0;
            this.motionZ *= (double)0.997f;
        } else {
            this.motionX *= (double)0.96f;
            this.motionY *= 0.0;
            this.motionZ *= (double)0.96f;
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float f = this.width / 2.0f;
        float f1 = this.height;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
    }

    public Vec3 func_70495_a(double p_70495_1_, double p_70495_3_, double p_70495_5_, double p_70495_7_) {
        IBlockState iblockstate;
        int k;
        int j;
        int i = MathHelper.floor_double(p_70495_1_);
        if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(i, (j = MathHelper.floor_double(p_70495_3_)) - 1, k = MathHelper.floor_double(p_70495_5_)))) {
            --j;
        }
        if (BlockRailBase.isRailBlock(iblockstate = this.worldObj.getBlockState(new BlockPos(i, j, k)))) {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getValue(((BlockRailBase)iblockstate.getBlock()).getShapeProperty());
            p_70495_3_ = j;
            if (blockrailbase$enumraildirection.isAscending()) {
                p_70495_3_ = j + 1;
            }
            int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
            double d0 = aint[1][0] - aint[0][0];
            double d1 = aint[1][2] - aint[0][2];
            double d2 = Math.sqrt(d0 * d0 + d1 * d1);
            if (aint[0][1] != 0 && MathHelper.floor_double(p_70495_1_ += (d0 /= d2) * p_70495_7_) - i == aint[0][0] && MathHelper.floor_double(p_70495_5_ += (d1 /= d2) * p_70495_7_) - k == aint[0][2]) {
                p_70495_3_ += (double)aint[0][1];
            } else if (aint[1][1] != 0 && MathHelper.floor_double(p_70495_1_) - i == aint[1][0] && MathHelper.floor_double(p_70495_5_) - k == aint[1][2]) {
                p_70495_3_ += (double)aint[1][1];
            }
            return this.func_70489_a(p_70495_1_, p_70495_3_, p_70495_5_);
        }
        return null;
    }

    public Vec3 func_70489_a(double p_70489_1_, double p_70489_3_, double p_70489_5_) {
        IBlockState iblockstate;
        int k;
        int j;
        int i = MathHelper.floor_double(p_70489_1_);
        if (BlockRailBase.isRailBlock(this.worldObj, new BlockPos(i, (j = MathHelper.floor_double(p_70489_3_)) - 1, k = MathHelper.floor_double(p_70489_5_)))) {
            --j;
        }
        if (BlockRailBase.isRailBlock(iblockstate = this.worldObj.getBlockState(new BlockPos(i, j, k)))) {
            BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getValue(((BlockRailBase)iblockstate.getBlock()).getShapeProperty());
            int[][] aint = matrix[blockrailbase$enumraildirection.getMetadata()];
            double d0 = 0.0;
            double d1 = (double)i + 0.5 + (double)aint[0][0] * 0.5;
            double d2 = (double)j + 0.0625 + (double)aint[0][1] * 0.5;
            double d3 = (double)k + 0.5 + (double)aint[0][2] * 0.5;
            double d4 = (double)i + 0.5 + (double)aint[1][0] * 0.5;
            double d5 = (double)j + 0.0625 + (double)aint[1][1] * 0.5;
            double d6 = (double)k + 0.5 + (double)aint[1][2] * 0.5;
            double d7 = d4 - d1;
            double d8 = (d5 - d2) * 2.0;
            double d9 = d6 - d3;
            if (d7 == 0.0) {
                p_70489_1_ = (double)i + 0.5;
                d0 = p_70489_5_ - (double)k;
            } else if (d9 == 0.0) {
                p_70489_5_ = (double)k + 0.5;
                d0 = p_70489_1_ - (double)i;
            } else {
                double d10 = p_70489_1_ - d1;
                double d11 = p_70489_5_ - d3;
                d0 = (d10 * d7 + d11 * d9) * 2.0;
            }
            p_70489_1_ = d1 + d7 * d0;
            p_70489_3_ = d2 + d8 * d0;
            p_70489_5_ = d3 + d9 * d0;
            if (d8 < 0.0) {
                p_70489_3_ += 1.0;
            }
            if (d8 > 0.0) {
                p_70489_3_ += 0.5;
            }
            return new Vec3(p_70489_1_, p_70489_3_, p_70489_5_);
        }
        return null;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        if (tagCompund.getBoolean("CustomDisplayTile")) {
            int i = tagCompund.getInteger("DisplayData");
            if (tagCompund.hasKey("DisplayTile", 8)) {
                Block block = Block.getBlockFromName(tagCompund.getString("DisplayTile"));
                if (block == null) {
                    this.func_174899_a(Blocks.air.getDefaultState());
                } else {
                    this.func_174899_a(block.getStateFromMeta(i));
                }
            } else {
                Block block1 = Block.getBlockById(tagCompund.getInteger("DisplayTile"));
                if (block1 == null) {
                    this.func_174899_a(Blocks.air.getDefaultState());
                } else {
                    this.func_174899_a(block1.getStateFromMeta(i));
                }
            }
            this.setDisplayTileOffset(tagCompund.getInteger("DisplayOffset"));
        }
        if (tagCompund.hasKey("CustomName", 8) && tagCompund.getString("CustomName").length() > 0) {
            this.entityName = tagCompund.getString("CustomName");
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        if (this.hasDisplayTile()) {
            tagCompound.setBoolean("CustomDisplayTile", true);
            IBlockState iblockstate = this.getDisplayTile();
            ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(iblockstate.getBlock());
            tagCompound.setString("DisplayTile", resourcelocation == null ? "" : resourcelocation.toString());
            tagCompound.setInteger("DisplayData", iblockstate.getBlock().getMetaFromState(iblockstate));
            tagCompound.setInteger("DisplayOffset", this.getDisplayTileOffset());
        }
        if (this.entityName != null && this.entityName.length() > 0) {
            tagCompound.setString("CustomName", this.entityName);
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!(this.worldObj.isRemote || entityIn.noClip || this.noClip || entityIn == this.riddenByEntity)) {
            double d1;
            double d0;
            double d2;
            if (entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityPlayer) && !(entityIn instanceof EntityIronGolem) && this.getMinecartType() == EnumMinecartType.RIDEABLE && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01 && this.riddenByEntity == null && entityIn.ridingEntity == null) {
                entityIn.mountEntity(this);
            }
            if ((d2 = (d0 = entityIn.posX - this.posX) * d0 + (d1 = entityIn.posZ - this.posZ) * d1) >= (double)1.0E-4f) {
                d2 = MathHelper.sqrt_double(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0 / d2;
                if (d3 > 1.0) {
                    d3 = 1.0;
                }
                d0 *= d3;
                d1 *= d3;
                d0 *= (double)0.1f;
                d1 *= (double)0.1f;
                d0 *= (double)(1.0f - this.entityCollisionReduction);
                d1 *= (double)(1.0f - this.entityCollisionReduction);
                d0 *= 0.5;
                d1 *= 0.5;
                if (entityIn instanceof EntityMinecart) {
                    Vec3 vec31;
                    double d4 = entityIn.posX - this.posX;
                    double d5 = entityIn.posZ - this.posZ;
                    Vec3 vec3 = new Vec3(d4, 0.0, d5).normalize();
                    double d6 = Math.abs(vec3.dotProduct(vec31 = new Vec3(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0f), 0.0, MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0f)).normalize()));
                    if (d6 < (double)0.8f) {
                        return;
                    }
                    double d7 = entityIn.motionX + this.motionX;
                    double d8 = entityIn.motionZ + this.motionZ;
                    if (((EntityMinecart)entityIn).getMinecartType() == EnumMinecartType.FURNACE && this.getMinecartType() != EnumMinecartType.FURNACE) {
                        this.motionX *= (double)0.2f;
                        this.motionZ *= (double)0.2f;
                        this.addVelocity(entityIn.motionX - d0, 0.0, entityIn.motionZ - d1);
                        entityIn.motionX *= (double)0.95f;
                        entityIn.motionZ *= (double)0.95f;
                    } else if (((EntityMinecart)entityIn).getMinecartType() != EnumMinecartType.FURNACE && this.getMinecartType() == EnumMinecartType.FURNACE) {
                        entityIn.motionX *= (double)0.2f;
                        entityIn.motionZ *= (double)0.2f;
                        entityIn.addVelocity(this.motionX + d0, 0.0, this.motionZ + d1);
                        this.motionX *= (double)0.95f;
                        this.motionZ *= (double)0.95f;
                    } else {
                        this.motionX *= (double)0.2f;
                        this.motionZ *= (double)0.2f;
                        this.addVelocity((d7 /= 2.0) - d0, 0.0, (d8 /= 2.0) - d1);
                        entityIn.motionX *= (double)0.2f;
                        entityIn.motionZ *= (double)0.2f;
                        entityIn.addVelocity(d7 + d0, 0.0, d8 + d1);
                    }
                } else {
                    this.addVelocity(-d0, 0.0, -d1);
                    entityIn.addVelocity(d0 / 4.0, 0.0, d1 / 4.0);
                }
            }
        }
    }

    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
        this.minecartX = x;
        this.minecartY = y;
        this.minecartZ = z;
        this.minecartYaw = yaw;
        this.minecartPitch = pitch;
        this.turnProgress = posRotationIncrements + 2;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        this.velocityX = this.motionX = x;
        this.velocityY = this.motionY = y;
        this.velocityZ = this.motionZ = z;
    }

    public void setDamage(float p_70492_1_) {
        this.dataWatcher.updateObject(19, Float.valueOf(p_70492_1_));
    }

    public float getDamage() {
        return this.dataWatcher.getWatchableObjectFloat(19);
    }

    public void setRollingAmplitude(int p_70497_1_) {
        this.dataWatcher.updateObject(17, p_70497_1_);
    }

    public int getRollingAmplitude() {
        return this.dataWatcher.getWatchableObjectInt(17);
    }

    public void setRollingDirection(int p_70494_1_) {
        this.dataWatcher.updateObject(18, p_70494_1_);
    }

    public int getRollingDirection() {
        return this.dataWatcher.getWatchableObjectInt(18);
    }

    public abstract EnumMinecartType getMinecartType();

    public IBlockState getDisplayTile() {
        return !this.hasDisplayTile() ? this.getDefaultDisplayTile() : Block.getStateById(this.getDataWatcher().getWatchableObjectInt(20));
    }

    public IBlockState getDefaultDisplayTile() {
        return Blocks.air.getDefaultState();
    }

    public int getDisplayTileOffset() {
        return !this.hasDisplayTile() ? this.getDefaultDisplayTileOffset() : this.getDataWatcher().getWatchableObjectInt(21);
    }

    public int getDefaultDisplayTileOffset() {
        return 6;
    }

    public void func_174899_a(IBlockState p_174899_1_) {
        this.getDataWatcher().updateObject(20, Block.getStateId(p_174899_1_));
        this.setHasDisplayTile(true);
    }

    public void setDisplayTileOffset(int p_94086_1_) {
        this.getDataWatcher().updateObject(21, p_94086_1_);
        this.setHasDisplayTile(true);
    }

    public boolean hasDisplayTile() {
        return this.getDataWatcher().getWatchableObjectByte(22) == 1;
    }

    public void setHasDisplayTile(boolean p_94096_1_) {
        this.getDataWatcher().updateObject(22, (byte)(p_94096_1_ ? 1 : 0));
    }

    @Override
    public void setCustomNameTag(String name) {
        this.entityName = name;
    }

    @Override
    public String getCommandSenderName() {
        return this.entityName != null ? this.entityName : super.getCommandSenderName();
    }

    @Override
    public boolean hasCustomName() {
        return this.entityName != null;
    }

    @Override
    public String getCustomNameTag() {
        return this.entityName;
    }

    @Override
    public IChatComponent getDisplayName() {
        if (this.hasCustomName()) {
            ChatComponentText chatcomponenttext = new ChatComponentText(this.entityName);
            chatcomponenttext.getChatStyle().setChatHoverEvent(this.getHoverEvent());
            chatcomponenttext.getChatStyle().setInsertion(this.getUniqueID().toString());
            return chatcomponenttext;
        }
        ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(this.getCommandSenderName(), new Object[0]);
        chatcomponenttranslation.getChatStyle().setChatHoverEvent(this.getHoverEvent());
        chatcomponenttranslation.getChatStyle().setInsertion(this.getUniqueID().toString());
        return chatcomponenttranslation;
    }

    public static enum EnumMinecartType {
        RIDEABLE(0, "MinecartRideable"),
        CHEST(1, "MinecartChest"),
        FURNACE(2, "MinecartFurnace"),
        TNT(3, "MinecartTNT"),
        SPAWNER(4, "MinecartSpawner"),
        HOPPER(5, "MinecartHopper"),
        COMMAND_BLOCK(6, "MinecartCommandBlock");

        private static final Map<Integer, EnumMinecartType> ID_LOOKUP;
        private final int networkID;
        private final String name;

        private EnumMinecartType(int networkID, String name) {
            this.networkID = networkID;
            this.name = name;
        }

        public int getNetworkID() {
            return this.networkID;
        }

        public String getName() {
            return this.name;
        }

        public static EnumMinecartType byNetworkID(int id) {
            EnumMinecartType entityminecart$enumminecarttype = ID_LOOKUP.get(id);
            return entityminecart$enumminecarttype == null ? RIDEABLE : entityminecart$enumminecarttype;
        }

        static {
            ID_LOOKUP = Maps.newHashMap();
            for (EnumMinecartType entityminecart$enumminecarttype : EnumMinecartType.values()) {
                ID_LOOKUP.put(entityminecart$enumminecarttype.getNetworkID(), entityminecart$enumminecarttype);
            }
        }
    }
}

