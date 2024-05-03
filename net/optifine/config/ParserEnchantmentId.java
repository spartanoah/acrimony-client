/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.config;

import net.minecraft.enchantment.Enchantment;
import net.optifine.config.IParserInt;

public class ParserEnchantmentId
implements IParserInt {
    @Override
    public int parse(String str, int defVal) {
        Enchantment enchantment = Enchantment.getEnchantmentByLocation(str);
        return enchantment == null ? defVal : enchantment.effectId;
    }
}

