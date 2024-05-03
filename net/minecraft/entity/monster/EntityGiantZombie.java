/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.monster;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityGiantZombie
extends EntityMob {
    public EntityGiantZombie(World worldIn) {
        super(worldIn);
        this.setSize(this.width * 6.0f, this.height * 6.0f);
    }

    @Override
    public float getEyeHeight() {
        return 10.440001f;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5);
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(50.0);
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return this.worldObj.getLightBrightness(pos) - 0.5f;
    }
}

