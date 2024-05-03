/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.projectile;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityFishHook
extends Entity {
    private static final List<WeightedRandomFishable> JUNK = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10).setMaxDamagePercent(0.9f), new WeightedRandomFishable(new ItemStack(Items.leather), 10), new WeightedRandomFishable(new ItemStack(Items.bone), 10), new WeightedRandomFishable(new ItemStack(Items.potionitem), 10), new WeightedRandomFishable(new ItemStack(Items.string), 5), new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2).setMaxDamagePercent(0.9f), new WeightedRandomFishable(new ItemStack(Items.bowl), 10), new WeightedRandomFishable(new ItemStack(Items.stick), 5), new WeightedRandomFishable(new ItemStack(Items.dye, 10, EnumDyeColor.BLACK.getDyeDamage()), 1), new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10), new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
    private static final List<WeightedRandomFishable> TREASURE = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1), new WeightedRandomFishable(new ItemStack(Items.name_tag), 1), new WeightedRandomFishable(new ItemStack(Items.saddle), 1), new WeightedRandomFishable(new ItemStack(Items.bow), 1).setMaxDamagePercent(0.25f).setEnchantable(), new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1).setMaxDamagePercent(0.25f).setEnchantable(), new WeightedRandomFishable(new ItemStack(Items.book), 1).setEnchantable());
    private static final List<WeightedRandomFishable> FISH = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.getMetadata()), 60), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.getMetadata()), 25), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.getMetadata()), 2), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.getMetadata()), 13));
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile;
    private boolean inGround;
    public int shake;
    public EntityPlayer angler;
    private int ticksInGround;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    public Entity caughtEntity;
    private int fishPosRotationIncrements;
    private double fishX;
    private double fishY;
    private double fishZ;
    private double fishYaw;
    private double fishPitch;
    private double clientMotionX;
    private double clientMotionY;
    private double clientMotionZ;

    public static List<WeightedRandomFishable> func_174855_j() {
        return FISH;
    }

    public EntityFishHook(World worldIn) {
        super(worldIn);
        this.setSize(0.25f, 0.25f);
        this.ignoreFrustumCheck = true;
    }

    public EntityFishHook(World worldIn, double x, double y, double z, EntityPlayer anglerIn) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.ignoreFrustumCheck = true;
        this.angler = anglerIn;
        anglerIn.fishEntity = this;
    }

    public EntityFishHook(World worldIn, EntityPlayer fishingPlayer) {
        super(worldIn);
        this.ignoreFrustumCheck = true;
        this.angler = fishingPlayer;
        this.angler.fishEntity = this;
        this.setSize(0.25f, 0.25f);
        this.setLocationAndAngles(fishingPlayer.posX, fishingPlayer.posY + (double)fishingPlayer.getEyeHeight(), fishingPlayer.posZ, fishingPlayer.rotationYaw, fishingPlayer.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
        this.posY -= (double)0.1f;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
        this.setPosition(this.posX, this.posY, this.posZ);
        float f = 0.4f;
        this.motionX = -MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f;
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f;
        this.motionY = -MathHelper.sin(this.rotationPitch / 180.0f * (float)Math.PI) * f;
        this.handleHookCasting(this.motionX, this.motionY, this.motionZ, 1.5f, 1.0f);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0;
        if (Double.isNaN(d0)) {
            d0 = 4.0;
        }
        return distance < (d0 *= 64.0) * d0;
    }

    public void handleHookCasting(double p_146035_1_, double p_146035_3_, double p_146035_5_, float p_146035_7_, float p_146035_8_) {
        float f = MathHelper.sqrt_double(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_);
        p_146035_1_ /= (double)f;
        p_146035_3_ /= (double)f;
        p_146035_5_ /= (double)f;
        p_146035_1_ += this.rand.nextGaussian() * (double)0.0075f * (double)p_146035_8_;
        p_146035_3_ += this.rand.nextGaussian() * (double)0.0075f * (double)p_146035_8_;
        p_146035_5_ += this.rand.nextGaussian() * (double)0.0075f * (double)p_146035_8_;
        this.motionX = p_146035_1_ *= (double)p_146035_7_;
        this.motionY = p_146035_3_ *= (double)p_146035_7_;
        this.motionZ = p_146035_5_ *= (double)p_146035_7_;
        float f1 = MathHelper.sqrt_double(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_);
        this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.func_181159_b(p_146035_1_, p_146035_5_) * 180.0 / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.func_181159_b(p_146035_3_, f1) * 180.0 / Math.PI);
        this.ticksInGround = 0;
    }

    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
        this.fishX = x;
        this.fishY = y;
        this.fishZ = z;
        this.fishYaw = yaw;
        this.fishPitch = pitch;
        this.fishPosRotationIncrements = posRotationIncrements;
        this.motionX = this.clientMotionX;
        this.motionY = this.clientMotionY;
        this.motionZ = this.clientMotionZ;
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        this.clientMotionX = this.motionX = x;
        this.clientMotionY = this.motionY = y;
        this.clientMotionZ = this.motionZ = z;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.fishPosRotationIncrements > 0) {
            double d7 = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
            double d8 = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
            double d9 = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
            double d1 = MathHelper.wrapAngleTo180_double(this.fishYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d1 / (double)this.fishPosRotationIncrements);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
            --this.fishPosRotationIncrements;
            this.setPosition(d7, d8, d9);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        } else {
            if (!this.worldObj.isRemote) {
                ItemStack itemstack = this.angler.getCurrentEquippedItem();
                if (this.angler.isDead || !this.angler.isEntityAlive() || itemstack == null || itemstack.getItem() != Items.fishing_rod || this.getDistanceSqToEntity(this.angler) > 1024.0) {
                    this.setDead();
                    this.angler.fishEntity = null;
                    return;
                }
                if (this.caughtEntity != null) {
                    if (!this.caughtEntity.isDead) {
                        this.posX = this.caughtEntity.posX;
                        double d17 = this.caughtEntity.height;
                        this.posY = this.caughtEntity.getEntityBoundingBox().minY + d17 * 0.8;
                        this.posZ = this.caughtEntity.posZ;
                        return;
                    }
                    this.caughtEntity = null;
                }
            }
            if (this.shake > 0) {
                --this.shake;
            }
            if (this.inGround) {
                if (this.worldObj.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
                    ++this.ticksInGround;
                    if (this.ticksInGround == 1200) {
                        this.setDead();
                    }
                    return;
                }
                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2f);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2f);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2f);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            } else {
                ++this.ticksInAir;
            }
            Vec3 vec31 = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3);
            vec31 = new Vec3(this.posX, this.posY, this.posZ);
            vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (movingobjectposition != null) {
                vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }
            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
            double d0 = 0.0;
            for (int i = 0; i < list.size(); ++i) {
                double d2;
                Entity entity1 = list.get(i);
                if (!entity1.canBeCollidedWith() || entity1 == this.angler && this.ticksInAir < 5) continue;
                float f = 0.3f;
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
                MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec31, vec3);
                if (movingobjectposition1 == null || !((d2 = vec31.squareDistanceTo(movingobjectposition1.hitVec)) < d0) && d0 != 0.0) continue;
                entity = entity1;
                d0 = d2;
            }
            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }
            if (movingobjectposition != null) {
                if (movingobjectposition.entityHit != null) {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.angler), 0.0f)) {
                        this.caughtEntity = movingobjectposition.entityHit;
                    }
                } else {
                    this.inGround = true;
                }
            }
            if (!this.inGround) {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                float f5 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                this.rotationYaw = (float)(MathHelper.func_181159_b(this.motionX, this.motionZ) * 180.0 / Math.PI);
                this.rotationPitch = (float)(MathHelper.func_181159_b(this.motionY, f5) * 180.0 / Math.PI);
                while (this.rotationPitch - this.prevRotationPitch < -180.0f) {
                    this.prevRotationPitch -= 360.0f;
                }
                while (this.rotationPitch - this.prevRotationPitch >= 180.0f) {
                    this.prevRotationPitch += 360.0f;
                }
                while (this.rotationYaw - this.prevRotationYaw < -180.0f) {
                    this.prevRotationYaw -= 360.0f;
                }
                while (this.rotationYaw - this.prevRotationYaw >= 180.0f) {
                    this.prevRotationYaw += 360.0f;
                }
                this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2f;
                this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2f;
                float f6 = 0.92f;
                if (this.onGround || this.isCollidedHorizontally) {
                    f6 = 0.5f;
                }
                int j = 5;
                double d10 = 0.0;
                for (int k = 0; k < j; ++k) {
                    AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
                    double d3 = axisalignedbb1.maxY - axisalignedbb1.minY;
                    double d4 = axisalignedbb1.minY + d3 * (double)k / (double)j;
                    double d5 = axisalignedbb1.minY + d3 * (double)(k + 1) / (double)j;
                    AxisAlignedBB axisalignedbb2 = new AxisAlignedBB(axisalignedbb1.minX, d4, axisalignedbb1.minZ, axisalignedbb1.maxX, d5, axisalignedbb1.maxZ);
                    if (!this.worldObj.isAABBInMaterial(axisalignedbb2, Material.water)) continue;
                    d10 += 1.0 / (double)j;
                }
                if (!this.worldObj.isRemote && d10 > 0.0) {
                    WorldServer worldserver = (WorldServer)this.worldObj;
                    int l = 1;
                    BlockPos blockpos = new BlockPos(this).up();
                    if (this.rand.nextFloat() < 0.25f && this.worldObj.canLightningStrike(blockpos)) {
                        l = 2;
                    }
                    if (this.rand.nextFloat() < 0.5f && !this.worldObj.canSeeSky(blockpos)) {
                        --l;
                    }
                    if (this.ticksCatchable > 0) {
                        --this.ticksCatchable;
                        if (this.ticksCatchable <= 0) {
                            this.ticksCaughtDelay = 0;
                            this.ticksCatchableDelay = 0;
                        }
                    } else if (this.ticksCatchableDelay > 0) {
                        this.ticksCatchableDelay -= l;
                        if (this.ticksCatchableDelay <= 0) {
                            this.motionY -= (double)0.2f;
                            this.playSound("random.splash", 0.25f, 1.0f + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4f);
                            float f8 = MathHelper.floor_double(this.getEntityBoundingBox().minY);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(f8 + 1.0f), this.posZ, (int)(1.0f + this.width * 20.0f), (double)this.width, 0.0, (double)this.width, (double)0.2f, new int[0]);
                            worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(f8 + 1.0f), this.posZ, (int)(1.0f + this.width * 20.0f), (double)this.width, 0.0, (double)this.width, (double)0.2f, new int[0]);
                            this.ticksCatchable = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                        } else {
                            double d16;
                            this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0);
                            float f7 = this.fishApproachAngle * ((float)Math.PI / 180);
                            float f10 = MathHelper.sin(f7);
                            float f11 = MathHelper.cos(f7);
                            double d13 = this.posX + (double)(f10 * (float)this.ticksCatchableDelay * 0.1f);
                            double d15 = (float)MathHelper.floor_double(this.getEntityBoundingBox().minY) + 1.0f;
                            Block block1 = worldserver.getBlockState(new BlockPos((int)d13, (int)d15 - 1, (int)(d16 = this.posZ + (double)(f11 * (float)this.ticksCatchableDelay * 0.1f)))).getBlock();
                            if (block1 == Blocks.water || block1 == Blocks.flowing_water) {
                                if (this.rand.nextFloat() < 0.15f) {
                                    worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d13, d15 - (double)0.1f, d16, 1, (double)f10, 0.1, (double)f11, 0.0, new int[0]);
                                }
                                float f3 = f10 * 0.04f;
                                float f4 = f11 * 0.04f;
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d13, d15, d16, 0, (double)f4, 0.01, (double)(-f3), 1.0, new int[0]);
                                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d13, d15, d16, 0, (double)(-f4), 0.01, (double)f3, 1.0, new int[0]);
                            }
                        }
                    } else if (this.ticksCaughtDelay > 0) {
                        this.ticksCaughtDelay -= l;
                        float f1 = 0.15f;
                        if (this.ticksCaughtDelay < 20) {
                            f1 = (float)((double)f1 + (double)(20 - this.ticksCaughtDelay) * 0.05);
                        } else if (this.ticksCaughtDelay < 40) {
                            f1 = (float)((double)f1 + (double)(40 - this.ticksCaughtDelay) * 0.02);
                        } else if (this.ticksCaughtDelay < 60) {
                            f1 = (float)((double)f1 + (double)(60 - this.ticksCaughtDelay) * 0.01);
                        }
                        if (this.rand.nextFloat() < f1) {
                            double d6;
                            double d14;
                            float f9 = MathHelper.randomFloatClamp(this.rand, 0.0f, 360.0f) * ((float)Math.PI / 180);
                            float f2 = MathHelper.randomFloatClamp(this.rand, 25.0f, 60.0f);
                            double d12 = this.posX + (double)(MathHelper.sin(f9) * f2 * 0.1f);
                            Block block = worldserver.getBlockState(new BlockPos((int)d12, (int)(d14 = (double)((float)MathHelper.floor_double(this.getEntityBoundingBox().minY) + 1.0f)) - 1, (int)(d6 = this.posZ + (double)(MathHelper.cos(f9) * f2 * 0.1f)))).getBlock();
                            if (block == Blocks.water || block == Blocks.flowing_water) {
                                worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d12, d14, d6, 2 + this.rand.nextInt(2), (double)0.1f, 0.0, (double)0.1f, 0.0, new int[0]);
                            }
                        }
                        if (this.ticksCaughtDelay <= 0) {
                            this.fishApproachAngle = MathHelper.randomFloatClamp(this.rand, 0.0f, 360.0f);
                            this.ticksCatchableDelay = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                        }
                    } else {
                        this.ticksCaughtDelay = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                        this.ticksCaughtDelay -= EnchantmentHelper.getLureModifier(this.angler) * 20 * 5;
                    }
                    if (this.ticksCatchable > 0) {
                        this.motionY -= (double)(this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat()) * 0.2;
                    }
                }
                double d11 = d10 * 2.0 - 1.0;
                this.motionY += (double)0.04f * d11;
                if (d10 > 0.0) {
                    f6 = (float)((double)f6 * 0.9);
                    this.motionY *= 0.8;
                }
                this.motionX *= (double)f6;
                this.motionY *= (double)f6;
                this.motionZ *= (double)f6;
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setShort("xTile", (short)this.xTile);
        tagCompound.setShort("yTile", (short)this.yTile);
        tagCompound.setShort("zTile", (short)this.zTile);
        ResourceLocation resourcelocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.inTile);
        tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
        tagCompound.setByte("shake", (byte)this.shake);
        tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        this.xTile = tagCompund.getShort("xTile");
        this.yTile = tagCompund.getShort("yTile");
        this.zTile = tagCompund.getShort("zTile");
        this.inTile = tagCompund.hasKey("inTile", 8) ? Block.getBlockFromName(tagCompund.getString("inTile")) : Block.getBlockById(tagCompund.getByte("inTile") & 0xFF);
        this.shake = tagCompund.getByte("shake") & 0xFF;
        this.inGround = tagCompund.getByte("inGround") == 1;
    }

    public int handleHookRetraction() {
        if (this.worldObj.isRemote) {
            return 0;
        }
        int i = 0;
        if (this.caughtEntity != null) {
            double d0 = this.angler.posX - this.posX;
            double d2 = this.angler.posY - this.posY;
            double d4 = this.angler.posZ - this.posZ;
            double d6 = MathHelper.sqrt_double(d0 * d0 + d2 * d2 + d4 * d4);
            double d8 = 0.1;
            this.caughtEntity.motionX += d0 * d8;
            this.caughtEntity.motionY += d2 * d8 + (double)MathHelper.sqrt_double(d6) * 0.08;
            this.caughtEntity.motionZ += d4 * d8;
            i = 3;
        } else if (this.ticksCatchable > 0) {
            EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getFishingResult());
            double d1 = this.angler.posX - this.posX;
            double d3 = this.angler.posY - this.posY;
            double d5 = this.angler.posZ - this.posZ;
            double d7 = MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
            double d9 = 0.1;
            entityitem.motionX = d1 * d9;
            entityitem.motionY = d3 * d9 + (double)MathHelper.sqrt_double(d7) * 0.08;
            entityitem.motionZ = d5 * d9;
            this.worldObj.spawnEntityInWorld(entityitem);
            this.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(this.angler.worldObj, this.angler.posX, this.angler.posY + 0.5, this.angler.posZ + 0.5, this.rand.nextInt(6) + 1));
            i = 1;
        }
        if (this.inGround) {
            i = 2;
        }
        this.setDead();
        this.angler.fishEntity = null;
        return i;
    }

    private ItemStack getFishingResult() {
        float f = this.worldObj.rand.nextFloat();
        int i = EnchantmentHelper.getLuckOfSeaModifier(this.angler);
        int j = EnchantmentHelper.getLureModifier(this.angler);
        float f1 = 0.1f - (float)i * 0.025f - (float)j * 0.01f;
        float f2 = 0.05f + (float)i * 0.01f - (float)j * 0.01f;
        f1 = MathHelper.clamp_float(f1, 0.0f, 1.0f);
        f2 = MathHelper.clamp_float(f2, 0.0f, 1.0f);
        if (f < f1) {
            this.angler.triggerAchievement(StatList.junkFishedStat);
            return WeightedRandom.getRandomItem(this.rand, JUNK).getItemStack(this.rand);
        }
        if ((f -= f1) < f2) {
            this.angler.triggerAchievement(StatList.treasureFishedStat);
            return WeightedRandom.getRandomItem(this.rand, TREASURE).getItemStack(this.rand);
        }
        float f3 = f - f2;
        this.angler.triggerAchievement(StatList.fishCaughtStat);
        return WeightedRandom.getRandomItem(this.rand, FISH).getItemStack(this.rand);
    }

    @Override
    public void setDead() {
        super.setDead();
        if (this.angler != null) {
            this.angler.fishEntity = null;
        }
    }
}

