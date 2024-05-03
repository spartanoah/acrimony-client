/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public interface IMerchant {
    public void setCustomer(EntityPlayer var1);

    public EntityPlayer getCustomer();

    public MerchantRecipeList getRecipes(EntityPlayer var1);

    public void setRecipes(MerchantRecipeList var1);

    public void useRecipe(MerchantRecipe var1);

    public void verifySellingItem(ItemStack var1);

    public IChatComponent getDisplayName();
}

