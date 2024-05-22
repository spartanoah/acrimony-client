/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.player;

import Acrimony.Acrimony;
import Acrimony.module.impl.ghost.KeepSprint;
import Acrimony.util.waveycapes.sim.StickSimulation;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public abstract class EntityPlayer
extends EntityLivingBase {
    public InventoryPlayer inventory = new InventoryPlayer(this);
    private InventoryEnderChest theInventoryEnderChest = new InventoryEnderChest();
    public Container inventoryContainer;
    public Container openContainer;
    protected FoodStats foodStats = new FoodStats();
    protected int flyToggleTimer;
    public float prevCameraYaw;
    public float cameraYaw;
    public int xpCooldown;
    public double prevChasingPosX;
    public double prevChasingPosY;
    public double prevChasingPosZ;
    public double chasingPosX;
    public double chasingPosY;
    public double chasingPosZ;
    public final StickSimulation stickSimulation = new StickSimulation();
    protected boolean sleeping;
    public BlockPos playerLocation;
    private int sleepTimer;
    public float renderOffsetX;
    public float renderOffsetY;
    public float renderOffsetZ;
    private BlockPos spawnChunk;
    private boolean spawnForced;
    private BlockPos startMinecartRidingCoordinate;
    public PlayerCapabilities capabilities = new PlayerCapabilities();
    public int experienceLevel;
    public int experienceTotal;
    public float experience;
    private int xpSeed;
    private ItemStack itemInUse;
    private int itemInUseCount;
    protected float speedOnGround = 0.1f;
    protected float speedInAir = 0.02f;
    private int lastXPSound;
    private final GameProfile gameProfile;
    private boolean hasReducedDebug = false;
    public EntityFishHook fishEntity;

    public EntityPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn);
        this.entityUniqueID = EntityPlayer.getUUID(gameProfileIn);
        this.gameProfile = gameProfileIn;
        this.openContainer = this.inventoryContainer = new ContainerPlayer(this.inventory, !worldIn.isRemote, this);
        BlockPos blockpos = worldIn.getSpawnPoint();
        this.setLocationAndAngles((double)blockpos.getX() + 0.5, blockpos.getY() + 1, (double)blockpos.getZ() + 0.5, 0.0f, 0.0f);
        this.field_70741_aB = 180.0f;
        this.fireResistance = 20;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.1f);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, (byte)0);
        this.dataWatcher.addObject(17, Float.valueOf(0.0f));
        this.dataWatcher.addObject(18, 0);
        this.dataWatcher.addObject(10, (byte)0);
    }

    public ItemStack getItemInUse() {
        return this.itemInUse;
    }

    public int getItemInUseCount() {
        return this.itemInUseCount;
    }

    public boolean isUsingItem() {
        return this.itemInUse != null;
    }

    public int getItemInUseDuration() {
        return this.isUsingItem() ? this.itemInUse.getMaxItemUseDuration() - this.itemInUseCount : 0;
    }

    public void stopUsingItem() {
        if (this.itemInUse != null) {
            this.itemInUse.onPlayerStoppedUsing(this.worldObj, this, this.itemInUseCount);
        }
        this.clearItemInUse();
    }

    public void clearItemInUse() {
        this.itemInUse = null;
        this.itemInUseCount = 0;
        if (!this.worldObj.isRemote) {
            this.setEating(false);
        }
    }

    public void updateSimulation(EntityPlayer abstractClientPlayer, int partCount) {
        int i;
        boolean dirty = false;
        if (this.stickSimulation.points.size() != partCount) {
            this.stickSimulation.points.clear();
            this.stickSimulation.sticks.clear();
            for (i = 0; i < partCount; ++i) {
                StickSimulation.Point point = new StickSimulation.Point();
                point.position.y = -i;
                point.locked = i == 0;
                this.stickSimulation.points.add(point);
                if (i <= 0) continue;
                this.stickSimulation.sticks.add(new StickSimulation.Stick(this.stickSimulation.points.get(i - 1), point, 1.0f));
            }
            dirty = true;
        }
        if (dirty) {
            for (i = 0; i < 10; ++i) {
                this.simulate(abstractClientPlayer);
            }
        }
    }

    public void simulate(EntityPlayer abstractClientPlayer) {
        if (this.stickSimulation.points.isEmpty()) {
            return;
        }
        this.stickSimulation.points.get((int)0).prevPosition.copy(this.stickSimulation.points.get((int)0).position);
        double d = abstractClientPlayer.chasingPosX - abstractClientPlayer.posX;
        double m = abstractClientPlayer.chasingPosZ - abstractClientPlayer.posZ;
        float n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        double o = MathHelper.sin(n * ((float)Math.PI / 180));
        double p = -MathHelper.cos(n * ((float)Math.PI / 180));
        float heightMul = 6.0f;
        double fallHack = MathHelper.clamp_double((double)this.stickSimulation.points.get((int)0).position.y - abstractClientPlayer.posY * (double)heightMul, 0.0, 1.0);
        this.stickSimulation.points.get((int)0).position.x = (float)((double)this.stickSimulation.points.get((int)0).position.x + (d * o + m * p + fallHack));
        this.stickSimulation.points.get((int)0).position.y = (float)(abstractClientPlayer.posY * (double)heightMul + (double)(abstractClientPlayer.isSneaking() ? -4 : 0));
        this.stickSimulation.simulate();
    }

    public boolean isBlocking() {
        return this.isUsingItem() && this.itemInUse.getItem().getItemUseAction(this.itemInUse) == EnumAction.BLOCK;
    }

    @Override
    public void onUpdate() {
        this.simulate(this);
        this.noClip = this.isSpectator();
        if (this.isSpectator()) {
            this.onGround = false;
        }
        if (this.itemInUse != null) {
            ItemStack itemstack = this.inventory.getCurrentItem();
            if (itemstack == this.itemInUse) {
                if (this.itemInUseCount <= 25 && this.itemInUseCount % 4 == 0) {
                    this.updateItemUse(itemstack, 5);
                }
                if (--this.itemInUseCount == 0 && !this.worldObj.isRemote) {
                    this.onItemUseFinish();
                }
            } else {
                this.clearItemInUse();
            }
        }
        if (this.xpCooldown > 0) {
            --this.xpCooldown;
        }
        if (this.isPlayerSleeping()) {
            ++this.sleepTimer;
            if (this.sleepTimer > 100) {
                this.sleepTimer = 100;
            }
            if (!this.worldObj.isRemote) {
                if (!this.isInBed()) {
                    this.wakeUpPlayer(true, true, false);
                } else if (this.worldObj.isDaytime()) {
                    this.wakeUpPlayer(false, true, true);
                }
            }
        } else if (this.sleepTimer > 0) {
            ++this.sleepTimer;
            if (this.sleepTimer >= 110) {
                this.sleepTimer = 0;
            }
        }
        super.onUpdate();
        if (!this.worldObj.isRemote && this.openContainer != null && !this.openContainer.canInteractWith(this)) {
            this.closeScreen();
            this.openContainer = this.inventoryContainer;
        }
        if (this.isBurning() && this.capabilities.disableDamage) {
            this.extinguish();
        }
        this.prevChasingPosX = this.chasingPosX;
        this.prevChasingPosY = this.chasingPosY;
        this.prevChasingPosZ = this.chasingPosZ;
        double d5 = this.posX - this.chasingPosX;
        double d0 = this.posY - this.chasingPosY;
        double d1 = this.posZ - this.chasingPosZ;
        double d2 = 10.0;
        if (d5 > d2) {
            this.prevChasingPosX = this.chasingPosX = this.posX;
        }
        if (d1 > d2) {
            this.prevChasingPosZ = this.chasingPosZ = this.posZ;
        }
        if (d0 > d2) {
            this.prevChasingPosY = this.chasingPosY = this.posY;
        }
        if (d5 < -d2) {
            this.prevChasingPosX = this.chasingPosX = this.posX;
        }
        if (d1 < -d2) {
            this.prevChasingPosZ = this.chasingPosZ = this.posZ;
        }
        if (d0 < -d2) {
            this.prevChasingPosY = this.chasingPosY = this.posY;
        }
        this.chasingPosX += d5 * 0.25;
        this.chasingPosZ += d1 * 0.25;
        this.chasingPosY += d0 * 0.25;
        if (this.ridingEntity == null) {
            this.startMinecartRidingCoordinate = null;
        }
        if (!this.worldObj.isRemote) {
            this.foodStats.onUpdate(this);
            this.triggerAchievement(StatList.minutesPlayedStat);
            if (this.isEntityAlive()) {
                this.triggerAchievement(StatList.timeSinceDeathStat);
            }
        }
        int i = 29999999;
        double d3 = MathHelper.clamp_double(this.posX, -2.9999999E7, 2.9999999E7);
        double d4 = MathHelper.clamp_double(this.posZ, -2.9999999E7, 2.9999999E7);
        if (d3 != this.posX || d4 != this.posZ) {
            this.setPosition(d3, this.posY, d4);
        }
    }

    @Override
    public int getMaxInPortalTime() {
        return this.capabilities.disableDamage ? 0 : 80;
    }

    @Override
    protected String getSwimSound() {
        return "game.player.swim";
    }

    @Override
    protected String getSplashSound() {
        return "game.player.swim.splash";
    }

    @Override
    public int getPortalCooldown() {
        return 10;
    }

    @Override
    public void playSound(String name, float volume, float pitch) {
        this.worldObj.playSoundToNearExcept(this, name, volume, pitch);
    }

    protected void updateItemUse(ItemStack itemStackIn, int p_71010_2_) {
        if (itemStackIn.getItemUseAction() == EnumAction.DRINK) {
            this.playSound("random.drink", 0.5f, this.worldObj.rand.nextFloat() * 0.1f + 0.9f);
        }
        if (itemStackIn.getItemUseAction() == EnumAction.EAT) {
            for (int i = 0; i < p_71010_2_; ++i) {
                Vec3 vec3 = new Vec3(((double)this.rand.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
                vec3 = vec3.rotatePitch(-this.rotationPitch * (float)Math.PI / 180.0f);
                vec3 = vec3.rotateYaw(-this.rotationYaw * (float)Math.PI / 180.0f);
                double d0 = (double)(-this.rand.nextFloat()) * 0.6 - 0.3;
                Vec3 vec31 = new Vec3(((double)this.rand.nextFloat() - 0.5) * 0.3, d0, 0.6);
                vec31 = vec31.rotatePitch(-this.rotationPitch * (float)Math.PI / 180.0f);
                vec31 = vec31.rotateYaw(-this.rotationYaw * (float)Math.PI / 180.0f);
                vec31 = vec31.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
                if (itemStackIn.getHasSubtypes()) {
                    this.worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05, vec3.zCoord, Item.getIdFromItem(itemStackIn.getItem()), itemStackIn.getMetadata());
                    continue;
                }
                this.worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05, vec3.zCoord, Item.getIdFromItem(itemStackIn.getItem()));
            }
            this.playSound("random.eat", 0.5f + 0.5f * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2f + 1.0f);
        }
    }

    protected void onItemUseFinish() {
        if (this.itemInUse != null) {
            this.updateItemUse(this.itemInUse, 16);
            int i = this.itemInUse.stackSize;
            ItemStack itemstack = this.itemInUse.onItemUseFinish(this.worldObj, this);
            if (itemstack != this.itemInUse || itemstack != null && itemstack.stackSize != i) {
                this.inventory.mainInventory[this.inventory.currentItem] = itemstack;
                if (itemstack.stackSize == 0) {
                    this.inventory.mainInventory[this.inventory.currentItem] = null;
                }
            }
            this.clearItemInUse();
        }
    }

    @Override
    public void handleHealthUpdate(byte id) {
        if (id == 9) {
            this.onItemUseFinish();
        } else if (id == 23) {
            this.hasReducedDebug = false;
        } else if (id == 22) {
            this.hasReducedDebug = true;
        } else {
            super.handleHealthUpdate(id);
        }
    }

    @Override
    protected boolean isMovementBlocked() {
        return this.getHealth() <= 0.0f || this.isPlayerSleeping();
    }

    protected void closeScreen() {
        this.openContainer = this.inventoryContainer;
    }

    @Override
    public void updateRidden() {
        if (!this.worldObj.isRemote && this.isSneaking()) {
            this.mountEntity(null);
            this.setSneaking(false);
        } else {
            double d0 = this.posX;
            double d1 = this.posY;
            double d2 = this.posZ;
            float f = this.rotationYaw;
            float f1 = this.rotationPitch;
            super.updateRidden();
            this.prevCameraYaw = this.cameraYaw;
            this.cameraYaw = 0.0f;
            this.addMountedMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
            if (this.ridingEntity instanceof EntityPig) {
                this.rotationPitch = f1;
                this.rotationYaw = f;
                this.renderYawOffset = ((EntityPig)this.ridingEntity).renderYawOffset;
            }
        }
    }

    @Override
    public void preparePlayerToSpawn() {
        this.setSize(0.6f, 1.8f);
        super.preparePlayerToSpawn();
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Override
    protected void updateEntityActionState() {
        super.updateEntityActionState();
        this.updateArmSwingProgress();
        this.rotationYawHead = this.rotationYaw;
    }

    @Override
    public void onLivingUpdate() {
        if (this.flyToggleTimer > 0) {
            --this.flyToggleTimer;
        }
        if (this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && this.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration")) {
            if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0) {
                this.heal(1.0f);
            }
            if (this.foodStats.needFood() && this.ticksExisted % 10 == 0) {
                this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
            }
        }
        this.inventory.decrementAnimations();
        this.prevCameraYaw = this.cameraYaw;
        super.onLivingUpdate();
        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (!this.worldObj.isRemote) {
            iattributeinstance.setBaseValue(this.capabilities.getWalkSpeed());
        }
        this.jumpMovementFactor = this.speedInAir;
        if (this.isSprinting()) {
            this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3);
        }
        this.setAIMoveSpeed((float)iattributeinstance.getAttributeValue());
        float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float f1 = (float)(Math.atan(-this.motionY * (double)0.2f) * 15.0);
        if (f > 0.1f) {
            f = 0.1f;
        }
        if (!this.onGround || this.getHealth() <= 0.0f) {
            f = 0.0f;
        }
        if (this.onGround || this.getHealth() <= 0.0f) {
            f1 = 0.0f;
        }
        this.cameraYaw += (f - this.cameraYaw) * 0.4f;
        this.cameraPitch += (f1 - this.cameraPitch) * 0.8f;
        if (this.getHealth() > 0.0f && !this.isSpectator()) {
            AxisAlignedBB axisalignedbb = null;
            axisalignedbb = this.ridingEntity != null && !this.ridingEntity.isDead ? this.getEntityBoundingBox().union(this.ridingEntity.getEntityBoundingBox()).expand(1.0, 0.0, 1.0) : this.getEntityBoundingBox().expand(1.0, 0.5, 1.0);
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);
            for (int i = 0; i < list.size(); ++i) {
                Entity entity = list.get(i);
                if (entity.isDead) continue;
                this.collideWithPlayer(entity);
            }
        }
    }

    private void collideWithPlayer(Entity p_71044_1_) {
        p_71044_1_.onCollideWithPlayer(this);
    }

    public int getScore() {
        return this.dataWatcher.getWatchableObjectInt(18);
    }

    public void setScore(int p_85040_1_) {
        this.dataWatcher.updateObject(18, p_85040_1_);
    }

    public void addScore(int p_85039_1_) {
        int i = this.getScore();
        this.dataWatcher.updateObject(18, i + p_85039_1_);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        this.setSize(0.2f, 0.2f);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.1f;
        if (this.getCommandSenderName().equals("Notch")) {
            this.dropItem(new ItemStack(Items.apple, 1), true, false);
        }
        if (!this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            this.inventory.dropAllItems();
        }
        if (cause != null) {
            this.motionX = -MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0f) * 0.1f;
            this.motionZ = -MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0f) * 0.1f;
        } else {
            this.motionZ = 0.0;
            this.motionX = 0.0;
        }
        this.triggerAchievement(StatList.deathsStat);
        this.func_175145_a(StatList.timeSinceDeathStat);
    }

    @Override
    protected String getHurtSound() {
        return "game.player.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "game.player.die";
    }

    @Override
    public void addToPlayerScore(Entity entityIn, int amount) {
        this.addScore(amount);
        Collection<ScoreObjective> collection = this.getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.totalKillCount);
        if (entityIn instanceof EntityPlayer) {
            this.triggerAchievement(StatList.playerKillsStat);
            collection.addAll(this.getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.playerKillCount));
            collection.addAll(this.func_175137_e(entityIn));
        } else {
            this.triggerAchievement(StatList.mobKillsStat);
        }
        for (ScoreObjective scoreobjective : collection) {
            Score score = this.getWorldScoreboard().getValueFromObjective(this.getCommandSenderName(), scoreobjective);
            score.func_96648_a();
        }
    }

    private Collection<ScoreObjective> func_175137_e(Entity p_175137_1_) {
        int j;
        ScorePlayerTeam scoreplayerteam1;
        int i;
        ScorePlayerTeam scoreplayerteam = this.getWorldScoreboard().getPlayersTeam(this.getCommandSenderName());
        if (scoreplayerteam != null && (i = scoreplayerteam.getChatFormat().getColorIndex()) >= 0 && i < IScoreObjectiveCriteria.field_178793_i.length) {
            for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.field_178793_i[i])) {
                Score score = this.getWorldScoreboard().getValueFromObjective(p_175137_1_.getCommandSenderName(), scoreobjective);
                score.func_96648_a();
            }
        }
        if ((scoreplayerteam1 = this.getWorldScoreboard().getPlayersTeam(p_175137_1_.getCommandSenderName())) != null && (j = scoreplayerteam1.getChatFormat().getColorIndex()) >= 0 && j < IScoreObjectiveCriteria.field_178792_h.length) {
            return this.getWorldScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.field_178792_h[j]);
        }
        return Lists.newArrayList();
    }

    public EntityItem dropOneItem(boolean dropAll) {
        return this.dropItem(this.inventory.decrStackSize(this.inventory.currentItem, dropAll && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1), false, true);
    }

    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean unused) {
        return this.dropItem(itemStackIn, false, false);
    }

    public EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        if (droppedItem == null) {
            return null;
        }
        if (droppedItem.stackSize == 0) {
            return null;
        }
        double d0 = this.posY - (double)0.3f + (double)this.getEyeHeight();
        EntityItem entityitem = new EntityItem(this.worldObj, this.posX, d0, this.posZ, droppedItem);
        entityitem.setPickupDelay(40);
        if (traceItem) {
            entityitem.setThrower(this.getCommandSenderName());
        }
        if (dropAround) {
            float f = this.rand.nextFloat() * 0.5f;
            float f1 = this.rand.nextFloat() * (float)Math.PI * 2.0f;
            entityitem.motionX = -MathHelper.sin(f1) * f;
            entityitem.motionZ = MathHelper.cos(f1) * f;
            entityitem.motionY = 0.2f;
        } else {
            float f2 = 0.3f;
            entityitem.motionX = -MathHelper.sin(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f2;
            entityitem.motionZ = MathHelper.cos(this.rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0f * (float)Math.PI) * f2;
            entityitem.motionY = -MathHelper.sin(this.rotationPitch / 180.0f * (float)Math.PI) * f2 + 0.1f;
            float f3 = this.rand.nextFloat() * (float)Math.PI * 2.0f;
            f2 = 0.02f * this.rand.nextFloat();
            entityitem.motionX += Math.cos(f3) * (double)f2;
            entityitem.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1f);
            entityitem.motionZ += Math.sin(f3) * (double)f2;
        }
        this.joinEntityItemWithWorld(entityitem);
        if (traceItem) {
            this.triggerAchievement(StatList.dropStat);
        }
        return entityitem;
    }

    protected void joinEntityItemWithWorld(EntityItem itemIn) {
        this.worldObj.spawnEntityInWorld(itemIn);
    }

    public float getToolDigEfficiency(Block p_180471_1_) {
        float f = this.inventory.getStrVsBlock(p_180471_1_);
        if (f > 1.0f) {
            int i = EnchantmentHelper.getEfficiencyModifier(this);
            ItemStack itemstack = this.inventory.getCurrentItem();
            if (i > 0 && itemstack != null) {
                f += (float)(i * i + 1);
            }
        }
        if (this.isPotionActive(Potion.digSpeed)) {
            f *= 1.0f + (float)(this.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (this.isPotionActive(Potion.digSlowdown)) {
            float f1 = 1.0f;
            switch (this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                case 0: {
                    f1 = 0.3f;
                    break;
                }
                case 1: {
                    f1 = 0.09f;
                    break;
                }
                case 2: {
                    f1 = 0.0027f;
                    break;
                }
                default: {
                    f1 = 8.1E-4f;
                }
            }
            f *= f1;
        }
        if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
            f /= 5.0f;
        }
        if (!this.onGround) {
            f /= 5.0f;
        }
        return f;
    }

    public boolean canHarvestBlock(Block blockToHarvest) {
        return this.inventory.canHeldItemHarvest(blockToHarvest);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.entityUniqueID = EntityPlayer.getUUID(this.gameProfile);
        NBTTagList nbttaglist = tagCompund.getTagList("Inventory", 10);
        this.inventory.readFromNBT(nbttaglist);
        this.inventory.currentItem = tagCompund.getInteger("SelectedItemSlot");
        this.sleeping = tagCompund.getBoolean("Sleeping");
        this.sleepTimer = tagCompund.getShort("SleepTimer");
        this.experience = tagCompund.getFloat("XpP");
        this.experienceLevel = tagCompund.getInteger("XpLevel");
        this.experienceTotal = tagCompund.getInteger("XpTotal");
        this.xpSeed = tagCompund.getInteger("XpSeed");
        if (this.xpSeed == 0) {
            this.xpSeed = this.rand.nextInt();
        }
        this.setScore(tagCompund.getInteger("Score"));
        if (this.sleeping) {
            this.playerLocation = new BlockPos(this);
            this.wakeUpPlayer(true, true, false);
        }
        if (tagCompund.hasKey("SpawnX", 99) && tagCompund.hasKey("SpawnY", 99) && tagCompund.hasKey("SpawnZ", 99)) {
            this.spawnChunk = new BlockPos(tagCompund.getInteger("SpawnX"), tagCompund.getInteger("SpawnY"), tagCompund.getInteger("SpawnZ"));
            this.spawnForced = tagCompund.getBoolean("SpawnForced");
        }
        this.foodStats.readNBT(tagCompund);
        this.capabilities.readCapabilitiesFromNBT(tagCompund);
        if (tagCompund.hasKey("EnderItems", 9)) {
            NBTTagList nbttaglist1 = tagCompund.getTagList("EnderItems", 10);
            this.theInventoryEnderChest.loadInventoryFromNBT(nbttaglist1);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        tagCompound.setInteger("SelectedItemSlot", this.inventory.currentItem);
        tagCompound.setBoolean("Sleeping", this.sleeping);
        tagCompound.setShort("SleepTimer", (short)this.sleepTimer);
        tagCompound.setFloat("XpP", this.experience);
        tagCompound.setInteger("XpLevel", this.experienceLevel);
        tagCompound.setInteger("XpTotal", this.experienceTotal);
        tagCompound.setInteger("XpSeed", this.xpSeed);
        tagCompound.setInteger("Score", this.getScore());
        if (this.spawnChunk != null) {
            tagCompound.setInteger("SpawnX", this.spawnChunk.getX());
            tagCompound.setInteger("SpawnY", this.spawnChunk.getY());
            tagCompound.setInteger("SpawnZ", this.spawnChunk.getZ());
            tagCompound.setBoolean("SpawnForced", this.spawnForced);
        }
        this.foodStats.writeNBT(tagCompound);
        this.capabilities.writeCapabilitiesToNBT(tagCompound);
        tagCompound.setTag("EnderItems", this.theInventoryEnderChest.saveInventoryToNBT());
        ItemStack itemstack = this.inventory.getCurrentItem();
        if (itemstack != null && itemstack.getItem() != null) {
            tagCompound.setTag("SelectedItem", itemstack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (this.capabilities.disableDamage && !source.canHarmInCreative()) {
            return false;
        }
        this.entityAge = 0;
        if (this.getHealth() <= 0.0f) {
            return false;
        }
        if (this.isPlayerSleeping() && !this.worldObj.isRemote) {
            this.wakeUpPlayer(true, true, false);
        }
        if (source.isDifficultyScaled()) {
            if (this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL) {
                amount = 0.0f;
            }
            if (this.worldObj.getDifficulty() == EnumDifficulty.EASY) {
                amount = amount / 2.0f + 1.0f;
            }
            if (this.worldObj.getDifficulty() == EnumDifficulty.HARD) {
                amount = amount * 3.0f / 2.0f;
            }
        }
        if (amount == 0.0f) {
            return false;
        }
        Entity entity = source.getEntity();
        if (entity instanceof EntityArrow && ((EntityArrow)entity).shootingEntity != null) {
            entity = ((EntityArrow)entity).shootingEntity;
        }
        return super.attackEntityFrom(source, amount);
    }

    public boolean canAttackPlayer(EntityPlayer other) {
        Team team = this.getTeam();
        Team team1 = other.getTeam();
        return team == null ? true : (!team.isSameTeam(team1) ? true : team.getAllowFriendlyFire());
    }

    @Override
    protected void damageArmor(float p_70675_1_) {
        this.inventory.damageArmor(p_70675_1_);
    }

    @Override
    public int getTotalArmorValue() {
        return this.inventory.getTotalArmorValue();
    }

    public float getArmorVisibility() {
        int i = 0;
        for (ItemStack itemstack : this.inventory.armorInventory) {
            if (itemstack == null) continue;
            ++i;
        }
        return (float)i / (float)this.inventory.armorInventory.length;
    }

    @Override
    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!this.isEntityInvulnerable(damageSrc)) {
            if (!damageSrc.isUnblockable() && this.isBlocking() && damageAmount > 0.0f) {
                damageAmount = (1.0f + damageAmount) * 0.5f;
            }
            damageAmount = this.applyArmorCalculations(damageSrc, damageAmount);
            float f = damageAmount = this.applyPotionDamageCalculations(damageSrc, damageAmount);
            damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0f);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - damageAmount));
            if (damageAmount != 0.0f) {
                this.addExhaustion(damageSrc.getHungerDamage());
                float f1 = this.getHealth();
                this.setHealth(this.getHealth() - damageAmount);
                this.getCombatTracker().trackDamage(damageSrc, f1, damageAmount);
                if (damageAmount < 3.4028235E37f) {
                    this.addStat(StatList.damageTakenStat, Math.round(damageAmount * 10.0f));
                }
            }
        }
    }

    public void openEditSign(TileEntitySign signTile) {
    }

    public void openEditCommandBlock(CommandBlockLogic cmdBlockLogic) {
    }

    public void displayVillagerTradeGui(IMerchant villager) {
    }

    public void displayGUIChest(IInventory chestInventory) {
    }

    public void displayGUIHorse(EntityHorse horse, IInventory horseInventory) {
    }

    public void displayGui(IInteractionObject guiOwner) {
    }

    public void displayGUIBook(ItemStack bookStack) {
    }

    public boolean interactWith(Entity p_70998_1_) {
        ItemStack itemstack1;
        if (this.isSpectator()) {
            if (p_70998_1_ instanceof IInventory) {
                this.displayGUIChest((IInventory)((Object)p_70998_1_));
            }
            return false;
        }
        ItemStack itemstack = this.getCurrentEquippedItem();
        ItemStack itemStack = itemstack1 = itemstack != null ? itemstack.copy() : null;
        if (!p_70998_1_.interactFirst(this)) {
            if (itemstack != null && p_70998_1_ instanceof EntityLivingBase) {
                if (this.capabilities.isCreativeMode) {
                    itemstack = itemstack1;
                }
                if (itemstack.interactWithEntity(this, (EntityLivingBase)p_70998_1_)) {
                    if (itemstack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                        this.destroyCurrentEquippedItem();
                    }
                    return true;
                }
            }
            return false;
        }
        if (itemstack != null && itemstack == this.getCurrentEquippedItem()) {
            if (itemstack.stackSize <= 0 && !this.capabilities.isCreativeMode) {
                this.destroyCurrentEquippedItem();
            } else if (itemstack.stackSize < itemstack1.stackSize && this.capabilities.isCreativeMode) {
                itemstack.stackSize = itemstack1.stackSize;
            }
        }
        return true;
    }

    public ItemStack getCurrentEquippedItem() {
        return this.inventory.getCurrentItem();
    }

    public void destroyCurrentEquippedItem() {
        this.inventory.setInventorySlotContents(this.inventory.currentItem, null);
    }

    @Override
    public double getYOffset() {
        return -0.35;
    }

    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        if (targetEntity.canAttackWithItem() && !targetEntity.hitByEntity(this)) {
            float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
            int i = 0;
            float f1 = 0.0f;
            f1 = targetEntity instanceof EntityLivingBase ? EnchantmentHelper.func_152377_a(this.getHeldItem(), ((EntityLivingBase)targetEntity).getCreatureAttribute()) : EnchantmentHelper.func_152377_a(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
            i += EnchantmentHelper.getKnockbackModifier(this);
            if (this.isSprinting()) {
                ++i;
            }
            if (f > 0.0f || f1 > 0.0f) {
                boolean flag;
                boolean bl = flag = this.fallDistance > 0.0f && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && targetEntity instanceof EntityLivingBase;
                if (flag && f > 0.0f) {
                    f *= 1.5f;
                }
                f += f1;
                boolean flag1 = false;
                int j = EnchantmentHelper.getFireAspectModifier(this);
                if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                    flag1 = true;
                    targetEntity.setFire(1);
                }
                double d0 = targetEntity.motionX;
                double d1 = targetEntity.motionY;
                double d2 = targetEntity.motionZ;
                boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);
                if (flag2) {
                    IEntityMultiPart ientitymultipart;
                    if (i > 0 && !Acrimony.instance.getModuleManager().getModule(KeepSprint.class).isEnabled()) {
                        this.motionX *= 0.6;
                        this.motionZ *= 0.6;
                        this.setSprinting(false);
                    }
                    if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                        ((EntityPlayerMP)targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                        targetEntity.velocityChanged = false;
                        targetEntity.motionX = d0;
                        targetEntity.motionY = d1;
                        targetEntity.motionZ = d2;
                    }
                    if (flag) {
                        this.onCriticalHit(targetEntity);
                    }
                    if (f1 > 0.0f) {
                        this.onEnchantmentCritical(targetEntity);
                    }
                    if (f >= 18.0f) {
                        this.triggerAchievement(AchievementList.overkill);
                    }
                    this.setLastAttacker(targetEntity);
                    if (targetEntity instanceof EntityLivingBase) {
                        EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, this);
                    }
                    EnchantmentHelper.applyArthropodEnchantments(this, targetEntity);
                    ItemStack itemstack = this.getCurrentEquippedItem();
                    Entity entity = targetEntity;
                    if (targetEntity instanceof EntityDragonPart && (ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj) instanceof EntityLivingBase) {
                        entity = (EntityLivingBase)((Object)ientitymultipart);
                    }
                    if (itemstack != null && entity instanceof EntityLivingBase) {
                        itemstack.hitEntity((EntityLivingBase)entity, this);
                        if (itemstack.stackSize <= 0) {
                            this.destroyCurrentEquippedItem();
                        }
                    }
                    if (targetEntity instanceof EntityLivingBase) {
                        this.addStat(StatList.damageDealtStat, Math.round(f * 10.0f));
                        if (j > 0) {
                            targetEntity.setFire(j * 4);
                        }
                    }
                    this.addExhaustion(0.3f);
                } else if (flag1) {
                    targetEntity.extinguish();
                }
            }
        }
    }

    public void attackEntityNoSlowdown(Entity targetEntity) {
        if (targetEntity.canAttackWithItem() && !targetEntity.hitByEntity(this)) {
            float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
            int i = 0;
            float f1 = 0.0f;
            f1 = targetEntity instanceof EntityLivingBase ? EnchantmentHelper.func_152377_a(this.getHeldItem(), ((EntityLivingBase)targetEntity).getCreatureAttribute()) : EnchantmentHelper.func_152377_a(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
            i += EnchantmentHelper.getKnockbackModifier(this);
            if (this.isSprinting()) {
                ++i;
            }
            if (f > 0.0f || f1 > 0.0f) {
                boolean flag;
                boolean bl = flag = this.fallDistance > 0.0f && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && targetEntity instanceof EntityLivingBase;
                if (flag && f > 0.0f) {
                    f *= 1.5f;
                }
                f += f1;
                boolean flag1 = false;
                int j = EnchantmentHelper.getFireAspectModifier(this);
                if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                    flag1 = true;
                    targetEntity.setFire(1);
                }
                double d0 = targetEntity.motionX;
                double d1 = targetEntity.motionY;
                double d2 = targetEntity.motionZ;
                boolean flag2 = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), f);
                if (flag2) {
                    IEntityMultiPart ientitymultipart;
                    if (i > 0) {
                        targetEntity.addVelocity(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0f) * (float)i * 0.5f, 0.1, MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0f) * (float)i * 0.5f);
                    }
                    if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                        ((EntityPlayerMP)targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                        targetEntity.velocityChanged = false;
                        targetEntity.motionX = d0;
                        targetEntity.motionY = d1;
                        targetEntity.motionZ = d2;
                    }
                    if (flag) {
                        this.onCriticalHit(targetEntity);
                    }
                    if (f1 > 0.0f) {
                        this.onEnchantmentCritical(targetEntity);
                    }
                    if (f >= 18.0f) {
                        this.triggerAchievement(AchievementList.overkill);
                    }
                    this.setLastAttacker(targetEntity);
                    if (targetEntity instanceof EntityLivingBase) {
                        EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, this);
                    }
                    EnchantmentHelper.applyArthropodEnchantments(this, targetEntity);
                    ItemStack itemstack = this.getCurrentEquippedItem();
                    Entity entity = targetEntity;
                    if (targetEntity instanceof EntityDragonPart && (ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj) instanceof EntityLivingBase) {
                        entity = (EntityLivingBase)((Object)ientitymultipart);
                    }
                    if (itemstack != null && entity instanceof EntityLivingBase) {
                        itemstack.hitEntity((EntityLivingBase)entity, this);
                        if (itemstack.stackSize <= 0) {
                            this.destroyCurrentEquippedItem();
                        }
                    }
                    if (targetEntity instanceof EntityLivingBase) {
                        this.addStat(StatList.damageDealtStat, Math.round(f * 10.0f));
                        if (j > 0) {
                            targetEntity.setFire(j * 4);
                        }
                    }
                    this.addExhaustion(0.3f);
                } else if (flag1) {
                    targetEntity.extinguish();
                }
            }
        }
    }

    public void onCriticalHit(Entity entityHit) {
    }

    public void onEnchantmentCritical(Entity entityHit) {
    }

    public void respawnPlayer() {
    }

    @Override
    public void setDead() {
        super.setDead();
        this.inventoryContainer.onContainerClosed(this);
        if (this.openContainer != null) {
            this.openContainer.onContainerClosed(this);
        }
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return !this.sleeping && super.isEntityInsideOpaqueBlock();
    }

    public boolean isUser() {
        return false;
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }

    public EnumStatus trySleep(BlockPos bedLocation) {
        if (!this.worldObj.isRemote) {
            if (this.isPlayerSleeping() || !this.isEntityAlive()) {
                return EnumStatus.OTHER_PROBLEM;
            }
            if (!this.worldObj.provider.isSurfaceWorld()) {
                return EnumStatus.NOT_POSSIBLE_HERE;
            }
            if (this.worldObj.isDaytime()) {
                return EnumStatus.NOT_POSSIBLE_NOW;
            }
            if (Math.abs(this.posX - (double)bedLocation.getX()) > 3.0 || Math.abs(this.posY - (double)bedLocation.getY()) > 2.0 || Math.abs(this.posZ - (double)bedLocation.getZ()) > 3.0) {
                return EnumStatus.TOO_FAR_AWAY;
            }
            double d0 = 8.0;
            double d1 = 5.0;
            List<EntityMob> list = this.worldObj.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)bedLocation.getX() - d0, (double)bedLocation.getY() - d1, (double)bedLocation.getZ() - d0, (double)bedLocation.getX() + d0, (double)bedLocation.getY() + d1, (double)bedLocation.getZ() + d0));
            if (!list.isEmpty()) {
                return EnumStatus.NOT_SAFE;
            }
        }
        if (this.isRiding()) {
            this.mountEntity(null);
        }
        this.setSize(0.2f, 0.2f);
        if (this.worldObj.isBlockLoaded(bedLocation)) {
            EnumFacing enumfacing = this.worldObj.getBlockState(bedLocation).getValue(BlockDirectional.FACING);
            float f = 0.5f;
            float f1 = 0.5f;
            switch (enumfacing) {
                case SOUTH: {
                    f1 = 0.9f;
                    break;
                }
                case NORTH: {
                    f1 = 0.1f;
                    break;
                }
                case WEST: {
                    f = 0.1f;
                    break;
                }
                case EAST: {
                    f = 0.9f;
                }
            }
            this.func_175139_a(enumfacing);
            this.setPosition((float)bedLocation.getX() + f, (float)bedLocation.getY() + 0.6875f, (float)bedLocation.getZ() + f1);
        } else {
            this.setPosition((float)bedLocation.getX() + 0.5f, (float)bedLocation.getY() + 0.6875f, (float)bedLocation.getZ() + 0.5f);
        }
        this.sleeping = true;
        this.sleepTimer = 0;
        this.playerLocation = bedLocation;
        this.motionY = 0.0;
        this.motionZ = 0.0;
        this.motionX = 0.0;
        if (!this.worldObj.isRemote) {
            this.worldObj.updateAllPlayersSleepingFlag();
        }
        return EnumStatus.OK;
    }

    private void func_175139_a(EnumFacing p_175139_1_) {
        this.renderOffsetX = 0.0f;
        this.renderOffsetZ = 0.0f;
        switch (p_175139_1_) {
            case SOUTH: {
                this.renderOffsetZ = -1.8f;
                break;
            }
            case NORTH: {
                this.renderOffsetZ = 1.8f;
                break;
            }
            case WEST: {
                this.renderOffsetX = 1.8f;
                break;
            }
            case EAST: {
                this.renderOffsetX = -1.8f;
            }
        }
    }

    public void wakeUpPlayer(boolean p_70999_1_, boolean updateWorldFlag, boolean setSpawn) {
        this.setSize(0.6f, 1.8f);
        IBlockState iblockstate = this.worldObj.getBlockState(this.playerLocation);
        if (this.playerLocation != null && iblockstate.getBlock() == Blocks.bed) {
            this.worldObj.setBlockState(this.playerLocation, iblockstate.withProperty(BlockBed.OCCUPIED, false), 4);
            BlockPos blockpos = BlockBed.getSafeExitLocation(this.worldObj, this.playerLocation, 0);
            if (blockpos == null) {
                blockpos = this.playerLocation.up();
            }
            this.setPosition((float)blockpos.getX() + 0.5f, (float)blockpos.getY() + 0.1f, (float)blockpos.getZ() + 0.5f);
        }
        this.sleeping = false;
        if (!this.worldObj.isRemote && updateWorldFlag) {
            this.worldObj.updateAllPlayersSleepingFlag();
        }
        int n = this.sleepTimer = p_70999_1_ ? 0 : 100;
        if (setSpawn) {
            this.setSpawnPoint(this.playerLocation, false);
        }
    }

    private boolean isInBed() {
        return this.worldObj.getBlockState(this.playerLocation).getBlock() == Blocks.bed;
    }

    public static BlockPos getBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn) {
        Block block = worldIn.getBlockState(bedLocation).getBlock();
        if (block != Blocks.bed) {
            if (!forceSpawn) {
                return null;
            }
            boolean flag = block.func_181623_g();
            boolean flag1 = worldIn.getBlockState(bedLocation.up()).getBlock().func_181623_g();
            return flag && flag1 ? bedLocation : null;
        }
        return BlockBed.getSafeExitLocation(worldIn, bedLocation, 0);
    }

    public float getBedOrientationInDegrees() {
        if (this.playerLocation != null) {
            EnumFacing enumfacing = this.worldObj.getBlockState(this.playerLocation).getValue(BlockDirectional.FACING);
            switch (enumfacing) {
                case SOUTH: {
                    return 90.0f;
                }
                case NORTH: {
                    return 270.0f;
                }
                case WEST: {
                    return 0.0f;
                }
                case EAST: {
                    return 180.0f;
                }
            }
        }
        return 0.0f;
    }

    @Override
    public boolean isPlayerSleeping() {
        return this.sleeping;
    }

    public boolean isPlayerFullyAsleep() {
        return this.sleeping && this.sleepTimer >= 100;
    }

    public int getSleepTimer() {
        return this.sleepTimer;
    }

    public void addChatComponentMessage(IChatComponent chatComponent) {
    }

    public BlockPos getBedLocation() {
        return this.spawnChunk;
    }

    public boolean isSpawnForced() {
        return this.spawnForced;
    }

    public void setSpawnPoint(BlockPos pos, boolean forced) {
        if (pos != null) {
            this.spawnChunk = pos;
            this.spawnForced = forced;
        } else {
            this.spawnChunk = null;
            this.spawnForced = false;
        }
    }

    public void triggerAchievement(StatBase achievementIn) {
        this.addStat(achievementIn, 1);
    }

    public void addStat(StatBase stat, int amount) {
    }

    public void func_175145_a(StatBase p_175145_1_) {
    }

    @Override
    public void jump() {
        super.jump();
        this.triggerAchievement(StatList.jumpStat);
        if (this.isSprinting()) {
            this.addExhaustion(0.8f);
        } else {
            this.addExhaustion(0.2f);
        }
    }

    @Override
    public void jumpNoEvent() {
        super.jumpNoEvent();
        this.triggerAchievement(StatList.jumpStat);
        if (this.isSprinting()) {
            this.addExhaustion(0.8f);
        } else {
            this.addExhaustion(0.2f);
        }
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;
        if (this.capabilities.isFlying && this.ridingEntity == null) {
            double d3 = this.motionY;
            float f = this.jumpMovementFactor;
            this.jumpMovementFactor = this.capabilities.getFlySpeed() * (float)(this.isSprinting() ? 2 : 1);
            super.moveEntityWithHeading(strafe, forward);
            this.motionY = d3 * 0.6;
            this.jumpMovementFactor = f;
        } else {
            super.moveEntityWithHeading(strafe, forward);
        }
        this.addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
    }

    @Override
    public float getAIMoveSpeed() {
        return (float)this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
    }

    public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
        if (this.ridingEntity == null) {
            if (this.isInsideOfMaterial(Material.water)) {
                int i = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_3_ * p_71000_3_ + p_71000_5_ * p_71000_5_) * 100.0f);
                if (i > 0) {
                    this.addStat(StatList.distanceDoveStat, i);
                    this.addExhaustion(0.015f * (float)i * 0.01f);
                }
            } else if (this.isInWater()) {
                int j = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0f);
                if (j > 0) {
                    this.addStat(StatList.distanceSwumStat, j);
                    this.addExhaustion(0.015f * (float)j * 0.01f);
                }
            } else if (this.isOnLadder()) {
                if (p_71000_3_ > 0.0) {
                    this.addStat(StatList.distanceClimbedStat, (int)Math.round(p_71000_3_ * 100.0));
                }
            } else if (this.onGround) {
                int k = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0f);
                if (k > 0) {
                    this.addStat(StatList.distanceWalkedStat, k);
                    if (this.isSprinting()) {
                        this.addStat(StatList.distanceSprintedStat, k);
                        this.addExhaustion(0.099999994f * (float)k * 0.01f);
                    } else {
                        if (this.isSneaking()) {
                            this.addStat(StatList.distanceCrouchedStat, k);
                        }
                        this.addExhaustion(0.01f * (float)k * 0.01f);
                    }
                }
            } else {
                int l = Math.round(MathHelper.sqrt_double(p_71000_1_ * p_71000_1_ + p_71000_5_ * p_71000_5_) * 100.0f);
                if (l > 25) {
                    this.addStat(StatList.distanceFlownStat, l);
                }
            }
        }
    }

    private void addMountedMovementStat(double p_71015_1_, double p_71015_3_, double p_71015_5_) {
        int i;
        if (this.ridingEntity != null && (i = Math.round(MathHelper.sqrt_double(p_71015_1_ * p_71015_1_ + p_71015_3_ * p_71015_3_ + p_71015_5_ * p_71015_5_) * 100.0f)) > 0) {
            if (this.ridingEntity instanceof EntityMinecart) {
                this.addStat(StatList.distanceByMinecartStat, i);
                if (this.startMinecartRidingCoordinate == null) {
                    this.startMinecartRidingCoordinate = new BlockPos(this);
                } else if (this.startMinecartRidingCoordinate.distanceSq(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) >= 1000000.0) {
                    this.triggerAchievement(AchievementList.onARail);
                }
            } else if (this.ridingEntity instanceof EntityBoat) {
                this.addStat(StatList.distanceByBoatStat, i);
            } else if (this.ridingEntity instanceof EntityPig) {
                this.addStat(StatList.distanceByPigStat, i);
            } else if (this.ridingEntity instanceof EntityHorse) {
                this.addStat(StatList.distanceByHorseStat, i);
            }
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if (!this.capabilities.allowFlying) {
            if (distance >= 2.0f) {
                this.addStat(StatList.distanceFallenStat, (int)Math.round((double)distance * 100.0));
            }
            super.fall(distance, damageMultiplier);
        }
    }

    @Override
    protected void resetHeight() {
        if (!this.isSpectator()) {
            super.resetHeight();
        }
    }

    @Override
    protected String getFallSoundString(int damageValue) {
        return damageValue > 4 ? "game.player.hurt.fall.big" : "game.player.hurt.fall.small";
    }

    @Override
    public void onKillEntity(EntityLivingBase entityLivingIn) {
        EntityList.EntityEggInfo entitylist$entityegginfo;
        if (entityLivingIn instanceof IMob) {
            this.triggerAchievement(AchievementList.killEnemy);
        }
        if ((entitylist$entityegginfo = EntityList.entityEggs.get(EntityList.getEntityID(entityLivingIn))) != null) {
            this.triggerAchievement(entitylist$entityegginfo.field_151512_d);
        }
    }

    @Override
    public void setInWeb() {
        if (!this.capabilities.isFlying) {
            super.setInWeb();
        }
    }

    @Override
    public ItemStack getCurrentArmor(int slotIn) {
        return this.inventory.armorItemInSlot(slotIn);
    }

    public void addExperience(int amount) {
        this.addScore(amount);
        int i = Integer.MAX_VALUE - this.experienceTotal;
        if (amount > i) {
            amount = i;
        }
        this.experience += (float)amount / (float)this.xpBarCap();
        this.experienceTotal += amount;
        while (this.experience >= 1.0f) {
            this.experience = (this.experience - 1.0f) * (float)this.xpBarCap();
            this.addExperienceLevel(1);
            this.experience /= (float)this.xpBarCap();
        }
    }

    public int getXPSeed() {
        return this.xpSeed;
    }

    public void removeExperienceLevel(int levels) {
        this.experienceLevel -= levels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experience = 0.0f;
            this.experienceTotal = 0;
        }
        this.xpSeed = this.rand.nextInt();
    }

    public void addExperienceLevel(int levels) {
        this.experienceLevel += levels;
        if (this.experienceLevel < 0) {
            this.experienceLevel = 0;
            this.experience = 0.0f;
            this.experienceTotal = 0;
        }
        if (levels > 0 && this.experienceLevel % 5 == 0 && (float)this.lastXPSound < (float)this.ticksExisted - 100.0f) {
            float f = this.experienceLevel > 30 ? 1.0f : (float)this.experienceLevel / 30.0f;
            this.worldObj.playSoundAtEntity(this, "random.levelup", f * 0.75f, 1.0f);
            this.lastXPSound = this.ticksExisted;
        }
    }

    public int xpBarCap() {
        return this.experienceLevel >= 30 ? 112 + (this.experienceLevel - 30) * 9 : (this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2);
    }

    public void addExhaustion(float p_71020_1_) {
        if (!this.capabilities.disableDamage && !this.worldObj.isRemote) {
            this.foodStats.addExhaustion(p_71020_1_);
        }
    }

    public FoodStats getFoodStats() {
        return this.foodStats;
    }

    public boolean canEat(boolean ignoreHunger) {
        return (ignoreHunger || this.foodStats.needFood()) && !this.capabilities.disableDamage;
    }

    public boolean shouldHeal() {
        return this.getHealth() > 0.0f && this.getHealth() < this.getMaxHealth();
    }

    public void setItemInUse(ItemStack stack, int duration) {
        if (stack != this.itemInUse) {
            this.itemInUse = stack;
            this.itemInUseCount = duration;
            if (!this.worldObj.isRemote) {
                this.setEating(true);
            }
        }
    }

    public boolean isAllowEdit() {
        return this.capabilities.allowEdit;
    }

    public boolean canPlayerEdit(BlockPos p_175151_1_, EnumFacing p_175151_2_, ItemStack p_175151_3_) {
        if (this.capabilities.allowEdit) {
            return true;
        }
        if (p_175151_3_ == null) {
            return false;
        }
        BlockPos blockpos = p_175151_1_.offset(p_175151_2_.getOpposite());
        Block block = this.worldObj.getBlockState(blockpos).getBlock();
        return p_175151_3_.canPlaceOn(block) || p_175151_3_.canEditBlocks();
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            return 0;
        }
        int i = this.experienceLevel * 7;
        return i > 100 ? 100 : i;
    }

    @Override
    protected boolean isPlayer() {
        return true;
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    public void clonePlayer(EntityPlayer oldPlayer, boolean respawnFromEnd) {
        if (respawnFromEnd) {
            this.inventory.copyInventory(oldPlayer.inventory);
            this.setHealth(oldPlayer.getHealth());
            this.foodStats = oldPlayer.foodStats;
            this.experienceLevel = oldPlayer.experienceLevel;
            this.experienceTotal = oldPlayer.experienceTotal;
            this.experience = oldPlayer.experience;
            this.setScore(oldPlayer.getScore());
            this.field_181016_an = oldPlayer.field_181016_an;
            this.field_181017_ao = oldPlayer.field_181017_ao;
            this.field_181018_ap = oldPlayer.field_181018_ap;
        } else if (this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            this.inventory.copyInventory(oldPlayer.inventory);
            this.experienceLevel = oldPlayer.experienceLevel;
            this.experienceTotal = oldPlayer.experienceTotal;
            this.experience = oldPlayer.experience;
            this.setScore(oldPlayer.getScore());
        }
        this.xpSeed = oldPlayer.xpSeed;
        this.theInventoryEnderChest = oldPlayer.theInventoryEnderChest;
        this.getDataWatcher().updateObject(10, oldPlayer.getDataWatcher().getWatchableObjectByte(10));
    }

    @Override
    protected boolean canTriggerWalking() {
        return !this.capabilities.isFlying;
    }

    public void sendPlayerAbilities() {
    }

    public void setGameType(WorldSettings.GameType gameType) {
    }

    public String getName() {
        return this.gameProfile.getName();
    }

    @Override
    public String getCommandSenderName() {
        return this.gameProfile.getName();
    }

    public InventoryEnderChest getInventoryEnderChest() {
        return this.theInventoryEnderChest;
    }

    @Override
    public ItemStack getEquipmentInSlot(int slotIn) {
        return slotIn == 0 ? this.inventory.getCurrentItem() : this.inventory.armorInventory[slotIn - 1];
    }

    @Override
    public ItemStack getHeldItem() {
        return this.inventory.getCurrentItem();
    }

    @Override
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack) {
        this.inventory.armorInventory[slotIn] = stack;
    }

    @Override
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        if (!this.isInvisible()) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }
        Team team = this.getTeam();
        return team == null || player == null || player.getTeam() != team || !team.getSeeFriendlyInvisiblesEnabled();
    }

    public abstract boolean isSpectator();

    @Override
    public ItemStack[] getInventory() {
        return this.inventory.armorInventory;
    }

    @Override
    public boolean isPushedByWater() {
        return !this.capabilities.isFlying;
    }

    public Scoreboard getWorldScoreboard() {
        return this.worldObj.getScoreboard();
    }

    @Override
    public Team getTeam() {
        return this.getWorldScoreboard().getPlayersTeam(this.getCommandSenderName());
    }

    @Override
    public IChatComponent getDisplayName() {
        ChatComponentText ichatcomponent = new ChatComponentText(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getCommandSenderName()));
        ichatcomponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.getCommandSenderName() + " "));
        ichatcomponent.getChatStyle().setChatHoverEvent(this.getHoverEvent());
        ichatcomponent.getChatStyle().setInsertion(this.getCommandSenderName());
        return ichatcomponent;
    }

    @Override
    public float getEyeHeight() {
        float f = 1.62f;
        if (this.isPlayerSleeping()) {
            f = 0.2f;
        }
        if (this.isSneaking()) {
            f -= 0.08f;
        }
        return f;
    }

    @Override
    public void setAbsorptionAmount(float amount) {
        if (amount < 0.0f) {
            amount = 0.0f;
        }
        this.getDataWatcher().updateObject(17, Float.valueOf(amount));
    }

    @Override
    public float getAbsorptionAmount() {
        return this.getDataWatcher().getWatchableObjectFloat(17);
    }

    public static UUID getUUID(GameProfile profile) {
        UUID uuid = profile.getId();
        if (uuid == null) {
            uuid = EntityPlayer.getOfflineUUID(profile.getName());
        }
        return uuid;
    }

    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
    }

    public boolean canOpen(LockCode code) {
        if (code.isEmpty()) {
            return true;
        }
        ItemStack itemstack = this.getCurrentEquippedItem();
        return itemstack != null && itemstack.hasDisplayName() ? itemstack.getDisplayName().equals(code.getLock()) : false;
    }

    public boolean isWearing(EnumPlayerModelParts p_175148_1_) {
        return (this.getDataWatcher().getWatchableObjectByte(10) & p_175148_1_.getPartMask()) == p_175148_1_.getPartMask();
    }

    @Override
    public boolean sendCommandFeedback() {
        return MinecraftServer.getServer().worldServers[0].getGameRules().getGameRuleBooleanValue("sendCommandFeedback");
    }

    @Override
    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn) {
        if (inventorySlot >= 0 && inventorySlot < this.inventory.mainInventory.length) {
            this.inventory.setInventorySlotContents(inventorySlot, itemStackIn);
            return true;
        }
        int i = inventorySlot - 100;
        if (i >= 0 && i < this.inventory.armorInventory.length) {
            int k = i + 1;
            if (itemStackIn != null && itemStackIn.getItem() != null && (itemStackIn.getItem() instanceof ItemArmor ? EntityLiving.getArmorPosition(itemStackIn) != k : k != 4 || itemStackIn.getItem() != Items.skull && !(itemStackIn.getItem() instanceof ItemBlock))) {
                return false;
            }
            this.inventory.setInventorySlotContents(i + this.inventory.mainInventory.length, itemStackIn);
            return true;
        }
        int j = inventorySlot - 200;
        if (j >= 0 && j < this.theInventoryEnderChest.getSizeInventory()) {
            this.theInventoryEnderChest.setInventorySlotContents(j, itemStackIn);
            return true;
        }
        return false;
    }

    public boolean hasReducedDebug() {
        return this.hasReducedDebug;
    }

    public void setReducedDebug(boolean reducedDebug) {
        this.hasReducedDebug = reducedDebug;
    }

    public static enum EnumStatus {
        OK,
        NOT_POSSIBLE_HERE,
        NOT_POSSIBLE_NOW,
        TOO_FAR_AWAY,
        OTHER_PROBLEM,
        NOT_SAFE;

    }

    public static enum EnumChatVisibility {
        FULL(0, "options.chat.visibility.full"),
        SYSTEM(1, "options.chat.visibility.system"),
        HIDDEN(2, "options.chat.visibility.hidden");

        private static final EnumChatVisibility[] ID_LOOKUP;
        private final int chatVisibility;
        private final String resourceKey;

        private EnumChatVisibility(int id, String resourceKey) {
            this.chatVisibility = id;
            this.resourceKey = resourceKey;
        }

        public int getChatVisibility() {
            return this.chatVisibility;
        }

        public static EnumChatVisibility getEnumChatVisibility(int id) {
            return ID_LOOKUP[id % ID_LOOKUP.length];
        }

        public String getResourceKey() {
            return this.resourceKey;
        }

        static {
            ID_LOOKUP = new EnumChatVisibility[EnumChatVisibility.values().length];
            EnumChatVisibility[] enumChatVisibilityArray = EnumChatVisibility.values();
            int n = enumChatVisibilityArray.length;
            for (int i = 0; i < n; ++i) {
                EnumChatVisibility entityplayer$enumchatvisibility;
                EnumChatVisibility.ID_LOOKUP[entityplayer$enumchatvisibility.chatVisibility] = entityplayer$enumchatvisibility = enumChatVisibilityArray[i];
            }
        }
    }
}

