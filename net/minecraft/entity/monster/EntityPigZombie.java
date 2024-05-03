/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.monster;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityPigZombie
extends EntityZombie {
    private static final UUID ATTACK_SPEED_BOOST_MODIFIER_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier ATTACK_SPEED_BOOST_MODIFIER = new AttributeModifier(ATTACK_SPEED_BOOST_MODIFIER_UUID, "Attacking speed boost", 0.05, 0).setSaved(false);
    private int angerLevel;
    private int randomSoundDelay;
    private UUID angerTargetUUID;

    public EntityPigZombie(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = true;
    }

    @Override
    public void setRevengeTarget(EntityLivingBase livingBase) {
        super.setRevengeTarget(livingBase);
        if (livingBase != null) {
            this.angerTargetUUID = livingBase.getUniqueID();
        }
    }

    @Override
    protected void applyEntityAI() {
        this.targetTasks.addTask(1, new AIHurtByAggressor(this));
        this.targetTasks.addTask(2, new AITargetAggressor(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(reinforcementChance).setBaseValue(0.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23f);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    protected void updateAITasks() {
        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (this.isAngry()) {
            if (!this.isChild() && !iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
                iattributeinstance.applyModifier(ATTACK_SPEED_BOOST_MODIFIER);
            }
            --this.angerLevel;
        } else if (iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER)) {
            iattributeinstance.removeModifier(ATTACK_SPEED_BOOST_MODIFIER);
        }
        if (this.randomSoundDelay > 0 && --this.randomSoundDelay == 0) {
            this.playSound("mob.zombiepig.zpigangry", this.getSoundVolume() * 2.0f, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2f + 1.0f) * 1.8f);
        }
        if (this.angerLevel > 0 && this.angerTargetUUID != null && this.getAITarget() == null) {
            EntityPlayer entityplayer = this.worldObj.getPlayerEntityByUUID(this.angerTargetUUID);
            this.setRevengeTarget(entityplayer);
            this.attackingPlayer = entityplayer;
            this.recentlyHit = this.getRevengeTimer();
        }
        super.updateAITasks();
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    @Override
    public boolean isNotColliding() {
        return this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.worldObj.isAnyLiquid(this.getEntityBoundingBox());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setShort("Anger", (short)this.angerLevel);
        if (this.angerTargetUUID != null) {
            tagCompound.setString("HurtBy", this.angerTargetUUID.toString());
        } else {
            tagCompound.setString("HurtBy", "");
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.angerLevel = tagCompund.getShort("Anger");
        String s = tagCompund.getString("HurtBy");
        if (s.length() > 0) {
            this.angerTargetUUID = UUID.fromString(s);
            EntityPlayer entityplayer = this.worldObj.getPlayerEntityByUUID(this.angerTargetUUID);
            this.setRevengeTarget(entityplayer);
            if (entityplayer != null) {
                this.attackingPlayer = entityplayer;
                this.recentlyHit = this.getRevengeTimer();
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        Entity entity = source.getEntity();
        if (entity instanceof EntityPlayer) {
            this.becomeAngryAt(entity);
        }
        return super.attackEntityFrom(source, amount);
    }

    private void becomeAngryAt(Entity p_70835_1_) {
        this.angerLevel = 400 + this.rand.nextInt(400);
        this.randomSoundDelay = this.rand.nextInt(40);
        if (p_70835_1_ instanceof EntityLivingBase) {
            this.setRevengeTarget((EntityLivingBase)p_70835_1_);
        }
    }

    public boolean isAngry() {
        return this.angerLevel > 0;
    }

    @Override
    protected String getLivingSound() {
        return "mob.zombiepig.zpig";
    }

    @Override
    protected String getHurtSound() {
        return "mob.zombiepig.zpighurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.zombiepig.zpigdeath";
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
        int i = this.rand.nextInt(2 + p_70628_2_);
        for (int j = 0; j < i; ++j) {
            this.dropItem(Items.rotten_flesh, 1);
        }
        i = this.rand.nextInt(2 + p_70628_2_);
        for (int k = 0; k < i; ++k) {
            this.dropItem(Items.gold_nugget, 1);
        }
    }

    @Override
    public boolean interact(EntityPlayer player) {
        return false;
    }

    @Override
    protected void addRandomDrop() {
        this.dropItem(Items.gold_ingot, 1);
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        this.setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        super.onInitialSpawn(difficulty, livingdata);
        this.setVillager(false);
        return livingdata;
    }

    static class AIHurtByAggressor
    extends EntityAIHurtByTarget {
        public AIHurtByAggressor(EntityPigZombie p_i45828_1_) {
            super((EntityCreature)p_i45828_1_, true, new Class[0]);
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn) {
            super.setEntityAttackTarget(creatureIn, entityLivingBaseIn);
            if (creatureIn instanceof EntityPigZombie) {
                ((EntityPigZombie)creatureIn).becomeAngryAt(entityLivingBaseIn);
            }
        }
    }

    static class AITargetAggressor
    extends EntityAINearestAttackableTarget<EntityPlayer> {
        public AITargetAggressor(EntityPigZombie p_i45829_1_) {
            super((EntityCreature)p_i45829_1_, EntityPlayer.class, true);
        }

        @Override
        public boolean shouldExecute() {
            return ((EntityPigZombie)this.taskOwner).isAngry() && super.shouldExecute();
        }
    }
}

