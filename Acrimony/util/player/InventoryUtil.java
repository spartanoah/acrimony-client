/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.player;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAnvilBlock;

public class InventoryUtil {
    public static boolean isBlockBlacklisted(Item item) {
        return item instanceof ItemAnvilBlock || item.getUnlocalizedName().contains("sand") && !item.getUnlocalizedName().contains("stone") || item.getUnlocalizedName().contains("gravel") || item.getUnlocalizedName().contains("ladder") || item.getUnlocalizedName().contains("tnt") || item.getUnlocalizedName().contains("chest") || item.getUnlocalizedName().contains("web") || item.getUnlocalizedName().contains("noteblock") || item.getUnlocalizedName().replace("-", "").contains("soulsand") || item.getUnlocalizedName().contains("ice");
    }
}

