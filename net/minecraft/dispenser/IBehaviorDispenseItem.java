/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.dispenser;

import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;

public interface IBehaviorDispenseItem {
    public static final IBehaviorDispenseItem itemDispenseBehaviorProvider = new IBehaviorDispenseItem(){

        @Override
        public ItemStack dispense(IBlockSource source, ItemStack stack) {
            return stack;
        }
    };

    public ItemStack dispense(IBlockSource var1, ItemStack var2);
}

