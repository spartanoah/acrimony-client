/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAIFindEntityNearestPlayer
extends EntityAIBase {
    private static final Logger field_179436_a = LogManager.getLogger();
    private EntityLiving field_179434_b;
    private final Predicate<Entity> field_179435_c;
    private final EntityAINearestAttackableTarget.Sorter field_179432_d;
    private EntityLivingBase field_179433_e;

    public EntityAIFindEntityNearestPlayer(EntityLiving p_i45882_1_) {
        this.field_179434_b = p_i45882_1_;
        if (p_i45882_1_ instanceof EntityCreature) {
            field_179436_a.warn("Use NearestAttackableTargetGoal.class for PathfinerMob mobs!");
        }
        this.field_179435_c = new Predicate<Entity>(){

            @Override
            public boolean apply(Entity p_apply_1_) {
                if (!(p_apply_1_ instanceof EntityPlayer)) {
                    return false;
                }
                if (((EntityPlayer)p_apply_1_).capabilities.disableDamage) {
                    return false;
                }
                double d0 = EntityAIFindEntityNearestPlayer.this.func_179431_f();
                if (p_apply_1_.isSneaking()) {
                    d0 *= (double)0.8f;
                }
                if (p_apply_1_.isInvisible()) {
                    float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();
                    if (f < 0.1f) {
                        f = 0.1f;
                    }
                    d0 *= (double)(0.7f * f);
                }
                return (double)p_apply_1_.getDistanceToEntity(EntityAIFindEntityNearestPlayer.this.field_179434_b) > d0 ? false : EntityAITarget.isSuitableTarget(EntityAIFindEntityNearestPlayer.this.field_179434_b, (EntityLivingBase)p_apply_1_, false, true);
            }
        };
        this.field_179432_d = new EntityAINearestAttackableTarget.Sorter(p_i45882_1_);
    }

    @Override
    public boolean shouldExecute() {
        double d0 = this.func_179431_f();
        List<Entity> list = this.field_179434_b.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.field_179434_b.getEntityBoundingBox().expand(d0, 4.0, d0), this.field_179435_c);
        Collections.sort(list, this.field_179432_d);
        if (list.isEmpty()) {
            return false;
        }
        this.field_179433_e = (EntityLivingBase)list.get(0);
        return true;
    }

    @Override
    public boolean continueExecuting() {
        EntityLivingBase entitylivingbase = this.field_179434_b.getAttackTarget();
        if (entitylivingbase == null) {
            return false;
        }
        if (!entitylivingbase.isEntityAlive()) {
            return false;
        }
        if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).capabilities.disableDamage) {
            return false;
        }
        Team team = this.field_179434_b.getTeam();
        Team team1 = entitylivingbase.getTeam();
        if (team != null && team1 == team) {
            return false;
        }
        double d0 = this.func_179431_f();
        return this.field_179434_b.getDistanceSqToEntity(entitylivingbase) > d0 * d0 ? false : !(entitylivingbase instanceof EntityPlayerMP) || !((EntityPlayerMP)entitylivingbase).theItemInWorldManager.isCreative();
    }

    @Override
    public void startExecuting() {
        this.field_179434_b.setAttackTarget(this.field_179433_e);
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        this.field_179434_b.setAttackTarget(null);
        super.startExecuting();
    }

    protected double func_179431_f() {
        IAttributeInstance iattributeinstance = this.field_179434_b.getEntityAttribute(SharedMonsterAttributes.followRange);
        return iattributeinstance == null ? 16.0 : iattributeinstance.getAttributeValue();
    }
}

