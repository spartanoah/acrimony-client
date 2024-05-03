/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraftforge.client.model;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;

public interface ISmartItemModel
extends IBakedModel {
    public IBakedModel handleItemState(ItemStack var1);
}

