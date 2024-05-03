/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.rewriter;

import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import java.util.HashMap;
import java.util.Map;

public class PotionMappings {
    public static final Map<String, String> POTION_NAME_INDEX = new HashMap<String, String>();
    public static final Map<String, Integer> POTION_NAME_TO_ID = new HashMap<String, Integer>();

    static {
        POTION_NAME_TO_ID.putAll(ItemRewriter.POTION_NAME_TO_ID);
        POTION_NAME_TO_ID.put("luck", 8203);
        POTION_NAME_INDEX.put("water", "\u00a7rWater Bottle");
        POTION_NAME_INDEX.put("mundane", "\u00a7rMundane Potion");
        POTION_NAME_INDEX.put("thick", "\u00a7rThick Potion");
        POTION_NAME_INDEX.put("awkward", "\u00a7rAwkward Potion");
        POTION_NAME_INDEX.put("water_splash", "\u00a7rSplash Water Bottle");
        POTION_NAME_INDEX.put("mundane_splash", "\u00a7rMundane Splash Potion");
        POTION_NAME_INDEX.put("thick_splash", "\u00a7rThick Splash Potion");
        POTION_NAME_INDEX.put("awkward_splash", "\u00a7rAwkward Splash Potion");
        POTION_NAME_INDEX.put("water_lingering", "\u00a7rLingering Water Bottle");
        POTION_NAME_INDEX.put("mundane_lingering", "\u00a7rMundane Lingering Potion");
        POTION_NAME_INDEX.put("thick_lingering", "\u00a7rThick Lingering Potion");
        POTION_NAME_INDEX.put("awkward_lingering", "\u00a7rAwkward Lingering Potion");
        POTION_NAME_INDEX.put("night_vision_lingering", "\u00a7rLingering Potion of Night Vision");
        POTION_NAME_INDEX.put("long_night_vision_lingering", "\u00a7rLingering Potion of Night Vision");
        POTION_NAME_INDEX.put("invisibility_lingering", "\u00a7rLingering Potion of Invisibility");
        POTION_NAME_INDEX.put("long_invisibility_lingering", "\u00a7rLingering Potion of Invisibility");
        POTION_NAME_INDEX.put("leaping_lingering", "\u00a7rLingering Potion of Leaping");
        POTION_NAME_INDEX.put("long_leaping_lingering", "\u00a7rLingering Potion of Leaping");
        POTION_NAME_INDEX.put("strong_leaping_lingering", "\u00a7rLingering Potion of Leaping");
        POTION_NAME_INDEX.put("fire_resistance_lingering", "\u00a7rLingering Potion of Fire Resistance");
        POTION_NAME_INDEX.put("long_fire_resistance_lingering", "\u00a7rLingering Potion of Fire Resistance");
        POTION_NAME_INDEX.put("swiftness_lingering", "\u00a7rLingering Potion of Swiftness");
        POTION_NAME_INDEX.put("long_swiftness_lingering", "\u00a7rLingering Potion of Swiftness");
        POTION_NAME_INDEX.put("strong_swiftness_lingering", "\u00a7rLingering Potion of Swiftness");
        POTION_NAME_INDEX.put("slowness_lingering", "\u00a7rLingering Potion of Slowness");
        POTION_NAME_INDEX.put("long_slowness_lingering", "\u00a7rLingering Potion of Slowness");
        POTION_NAME_INDEX.put("water_breathing_lingering", "\u00a7rLingering Potion of Water Breathing");
        POTION_NAME_INDEX.put("long_water_breathing_lingering", "\u00a7rLingering Potion of Water Breathing");
        POTION_NAME_INDEX.put("healing_lingering", "\u00a7rLingering Potion of Healing");
        POTION_NAME_INDEX.put("strong_healing_lingering", "\u00a7rLingering Potion of Healing");
        POTION_NAME_INDEX.put("harming_lingering", "\u00a7rLingering Potion of Harming");
        POTION_NAME_INDEX.put("strong_harming_lingering", "\u00a7rLingering Potion of Harming");
        POTION_NAME_INDEX.put("poison_lingering", "\u00a7rLingering Potion of Poisen");
        POTION_NAME_INDEX.put("long_poison_lingering", "\u00a7rLingering Potion of Poisen");
        POTION_NAME_INDEX.put("strong_poison_lingering", "\u00a7rLingering Potion of Poisen");
        POTION_NAME_INDEX.put("regeneration_lingering", "\u00a7rLingering Potion of Regeneration");
        POTION_NAME_INDEX.put("long_regeneration_lingering", "\u00a7rLingering Potion of Regeneration");
        POTION_NAME_INDEX.put("strong_regeneration_lingering", "\u00a7rLingering Potion of Regeneration");
        POTION_NAME_INDEX.put("strength_lingering", "\u00a7rLingering Potion of Strength");
        POTION_NAME_INDEX.put("long_strength_lingering", "\u00a7rLingering Potion of Strength");
        POTION_NAME_INDEX.put("strong_strength_lingering", "\u00a7rLingering Potion of Strength");
        POTION_NAME_INDEX.put("weakness_lingering", "\u00a7rLingering Potion of Weakness");
        POTION_NAME_INDEX.put("long_weakness_lingering", "\u00a7rLingering Potion of Weakness");
        POTION_NAME_INDEX.put("luck_lingering", "\u00a7rLingering Potion of Luck");
        POTION_NAME_INDEX.put("luck", "\u00a7rPotion of Luck");
        POTION_NAME_INDEX.put("luck_splash", "\u00a7rSplash Potion of Luck");
    }
}

