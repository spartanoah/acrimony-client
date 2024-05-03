/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RenderPotion
extends RenderSnowball<EntityPotion> {
    public RenderPotion(RenderManager renderManagerIn, RenderItem itemRendererIn) {
        super(renderManagerIn, Items.potionitem, itemRendererIn);
    }

    @Override
    public ItemStack func_177082_d(EntityPotion entityIn) {
        return new ItemStack(this.field_177084_a, 1, entityIn.getPotionDamage());
    }
}

