/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityItem
extends Entity {
    private static final Logger logger = LogManager.getLogger();
    private int age;
    private int delayBeforeCanPickup;
    private int health = 5;
    private String thrower;
    private String owner;
    public float hoverStart = (float)(Math.random() * Math.PI * 2.0);

    public EntityItem(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.setSize(0.25f, 0.25f);
        this.setPosition(x, y, z);
        this.rotationYaw = (float)(Math.random() * 360.0);
        this.motionX = (float)(Math.random() * (double)0.2f - (double)0.1f);
        this.motionY = 0.2f;
        this.motionZ = (float)(Math.random() * (double)0.2f - (double)0.1f);
    }

    public EntityItem(World worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        this.setEntityItemStack(stack);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    public EntityItem(World worldIn) {
        super(worldIn);
        this.setSize(0.25f, 0.25f);
        this.setEntityItemStack(new ItemStack(Blocks.air, 0));
    }

    @Override
    protected void entityInit() {
        this.getDataWatcher().addObjectByDataType(10, 5);
    }

    @Override
    public void onUpdate() {
        if (this.getEntityItem() == null) {
            this.setDead();
        } else {
            boolean flag;
            super.onUpdate();
            if (this.delayBeforeCanPickup > 0 && this.delayBeforeCanPickup != Short.MAX_VALUE) {
                --this.delayBeforeCanPickup;
            }
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY -= (double)0.04f;
            this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0, this.posZ);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            boolean bl = flag = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;
            if (flag || this.ticksExisted % 25 == 0) {
                if (this.worldObj.getBlockState(new BlockPos(this)).getBlock().getMaterial() == Material.lava) {
                    this.motionY = 0.2f;
                    this.motionX = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2f;
                    this.motionZ = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2f;
                    this.playSound("random.fizz", 0.4f, 2.0f + this.rand.nextFloat() * 0.4f);
                }
                if (!this.worldObj.isRemote) {
                    this.searchForOtherItemsNearby();
                }
            }
            float f = 0.98f;
            if (this.onGround) {
                f = this.worldObj.getBlockState((BlockPos)new BlockPos((int)MathHelper.floor_double((double)this.posX), (int)(MathHelper.floor_double((double)this.getEntityBoundingBox().minY) - 1), (int)MathHelper.floor_double((double)this.posZ))).getBlock().slipperiness * 0.98f;
            }
            this.motionX *= (double)f;
            this.motionY *= (double)0.98f;
            this.motionZ *= (double)f;
            if (this.onGround) {
                this.motionY *= -0.5;
            }
            if (this.age != Short.MIN_VALUE) {
                ++this.age;
            }
            this.handleWaterMovement();
            if (!this.worldObj.isRemote && this.age >= 6000) {
                this.setDead();
            }
        }
    }

    private void searchForOtherItemsNearby() {
        for (EntityItem entityitem : this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(0.5, 0.0, 0.5))) {
            this.combineItems(entityitem);
        }
    }

    private boolean combineItems(EntityItem other) {
        if (other == this) {
            return false;
        }
        if (other.isEntityAlive() && this.isEntityAlive()) {
            ItemStack itemstack = this.getEntityItem();
            ItemStack itemstack1 = other.getEntityItem();
            if (this.delayBeforeCanPickup != Short.MAX_VALUE && other.delayBeforeCanPickup != Short.MAX_VALUE) {
                if (this.age != Short.MIN_VALUE && other.age != Short.MIN_VALUE) {
                    if (itemstack1.getItem() != itemstack.getItem()) {
                        return false;
                    }
                    if (itemstack1.hasTagCompound() ^ itemstack.hasTagCompound()) {
                        return false;
                    }
                    if (itemstack1.hasTagCompound() && !itemstack1.getTagCompound().equals(itemstack.getTagCompound())) {
                        return false;
                    }
                    if (itemstack1.getItem() == null) {
                        return false;
                    }
                    if (itemstack1.getItem().getHasSubtypes() && itemstack1.getMetadata() != itemstack.getMetadata()) {
                        return false;
                    }
                    if (itemstack1.stackSize < itemstack.stackSize) {
                        return other.combineItems(this);
                    }
                    if (itemstack1.stackSize + itemstack.stackSize > itemstack1.getMaxStackSize()) {
                        return false;
                    }
                    itemstack1.stackSize += itemstack.stackSize;
                    other.delayBeforeCanPickup = Math.max(other.delayBeforeCanPickup, this.delayBeforeCanPickup);
                    other.age = Math.min(other.age, this.age);
                    other.setEntityItemStack(itemstack1);
                    this.setDead();
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public void setAgeToCreativeDespawnTime() {
        this.age = 4800;
    }

    @Override
    public boolean handleWaterMovement() {
        if (this.worldObj.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.water, this)) {
            if (!this.inWater && !this.firstUpdate) {
                this.resetHeight();
            }
            this.inWater = true;
        } else {
            this.inWater = false;
        }
        return this.inWater;
    }

    @Override
    protected void dealFireDamage(int amount) {
        this.attackEntityFrom(DamageSource.inFire, amount);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (this.getEntityItem() != null && this.getEntityItem().getItem() == Items.nether_star && source.isExplosion()) {
            return false;
        }
        this.setBeenAttacked();
        this.health = (int)((float)this.health - amount);
        if (this.health <= 0) {
            this.setDead();
        }
        return false;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setShort("Health", (byte)this.health);
        tagCompound.setShort("Age", (short)this.age);
        tagCompound.setShort("PickupDelay", (short)this.delayBeforeCanPickup);
        if (this.getThrower() != null) {
            tagCompound.setString("Thrower", this.thrower);
        }
        if (this.getOwner() != null) {
            tagCompound.setString("Owner", this.owner);
        }
        if (this.getEntityItem() != null) {
            tagCompound.setTag("Item", this.getEntityItem().writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        this.health = tagCompund.getShort("Health") & 0xFF;
        this.age = tagCompund.getShort("Age");
        if (tagCompund.hasKey("PickupDelay")) {
            this.delayBeforeCanPickup = tagCompund.getShort("PickupDelay");
        }
        if (tagCompund.hasKey("Owner")) {
            this.owner = tagCompund.getString("Owner");
        }
        if (tagCompund.hasKey("Thrower")) {
            this.thrower = tagCompund.getString("Thrower");
        }
        NBTTagCompound nbttagcompound = tagCompund.getCompoundTag("Item");
        this.setEntityItemStack(ItemStack.loadItemStackFromNBT(nbttagcompound));
        if (this.getEntityItem() == null) {
            this.setDead();
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if (!this.worldObj.isRemote) {
            ItemStack itemstack = this.getEntityItem();
            int i = itemstack.stackSize;
            if (this.delayBeforeCanPickup == 0 && (this.owner == null || 6000 - this.age <= 200 || this.owner.equals(entityIn.getCommandSenderName())) && entityIn.inventory.addItemStackToInventory(itemstack)) {
                EntityPlayer entityplayer;
                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log)) {
                    entityIn.triggerAchievement(AchievementList.mineWood);
                }
                if (itemstack.getItem() == Item.getItemFromBlock(Blocks.log2)) {
                    entityIn.triggerAchievement(AchievementList.mineWood);
                }
                if (itemstack.getItem() == Items.leather) {
                    entityIn.triggerAchievement(AchievementList.killCow);
                }
                if (itemstack.getItem() == Items.diamond) {
                    entityIn.triggerAchievement(AchievementList.diamonds);
                }
                if (itemstack.getItem() == Items.blaze_rod) {
                    entityIn.triggerAchievement(AchievementList.blazeRod);
                }
                if (itemstack.getItem() == Items.diamond && this.getThrower() != null && (entityplayer = this.worldObj.getPlayerEntityByName(this.getThrower())) != null && entityplayer != entityIn) {
                    entityplayer.triggerAchievement(AchievementList.diamondsToYou);
                }
                if (!this.isSilent()) {
                    this.worldObj.playSoundAtEntity(entityIn, "random.pop", 0.2f, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7f + 1.0f) * 2.0f);
                }
                entityIn.onItemPickup(this, i);
                if (itemstack.stackSize <= 0) {
                    this.setDead();
                }
            }
        }
    }

    @Override
    public String getCommandSenderName() {
        return this.hasCustomName() ? this.getCustomNameTag() : StatCollector.translateToLocal("item." + this.getEntityItem().getUnlocalizedName());
    }

    @Override
    public boolean canAttackWithItem() {
        return false;
    }

    @Override
    public void travelToDimension(int dimensionId) {
        super.travelToDimension(dimensionId);
        if (!this.worldObj.isRemote) {
            this.searchForOtherItemsNearby();
        }
    }

    public ItemStack getEntityItem() {
        ItemStack itemstack = this.getDataWatcher().getWatchableObjectItemStack(10);
        if (itemstack == null) {
            if (this.worldObj != null) {
                logger.error("Item entity " + this.getEntityId() + " has no item?!");
            }
            return new ItemStack(Blocks.stone);
        }
        return itemstack;
    }

    public void setEntityItemStack(ItemStack stack) {
        this.getDataWatcher().updateObject(10, stack);
        this.getDataWatcher().setObjectWatched(10);
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getThrower() {
        return this.thrower;
    }

    public void setThrower(String thrower) {
        this.thrower = thrower;
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickupDelay() {
        this.delayBeforeCanPickup = 10;
    }

    public void setNoPickupDelay() {
        this.delayBeforeCanPickup = 0;
    }

    public void setInfinitePickupDelay() {
        this.delayBeforeCanPickup = Short.MAX_VALUE;
    }

    public void setPickupDelay(int ticks) {
        this.delayBeforeCanPickup = ticks;
    }

    public boolean cannotPickup() {
        return this.delayBeforeCanPickup > 0;
    }

    public void setNoDespawn() {
        this.age = -6000;
    }

    public void func_174870_v() {
        this.setInfinitePickupDelay();
        this.age = 5999;
    }
}

