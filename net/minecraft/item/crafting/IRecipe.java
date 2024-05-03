/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRecipe {
    public boolean matches(InventoryCrafting var1, World var2);

    public ItemStack getCraftingResult(InventoryCrafting var1);

    public int getRecipeSize();

    public ItemStack getRecipeOutput();

    public ItemStack[] getRemainingItems(InventoryCrafting var1);
}

