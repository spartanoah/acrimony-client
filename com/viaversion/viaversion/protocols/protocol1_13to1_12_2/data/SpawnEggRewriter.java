/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.util.Key;
import java.util.Optional;

public class SpawnEggRewriter {
    private static final BiMap<String, Integer> spawnEggs = HashBiMap.create();

    private static void registerSpawnEgg(String name) {
        spawnEggs.put(Key.namespaced(name), spawnEggs.size());
    }

    public static int getSpawnEggId(String entityIdentifier) {
        if (!spawnEggs.containsKey(entityIdentifier)) {
            return -1;
        }
        return 0x17F0000 | (Integer)spawnEggs.get(entityIdentifier) & 0xFFFF;
    }

    public static Optional<String> getEntityId(int spawnEggId) {
        if (spawnEggId >> 16 != 383) {
            return Optional.empty();
        }
        return Optional.ofNullable(spawnEggs.inverse().get(spawnEggId & 0xFFFF));
    }

    static {
        SpawnEggRewriter.registerSpawnEgg("bat");
        SpawnEggRewriter.registerSpawnEgg("blaze");
        SpawnEggRewriter.registerSpawnEgg("cave_spider");
        SpawnEggRewriter.registerSpawnEgg("chicken");
        SpawnEggRewriter.registerSpawnEgg("cow");
        SpawnEggRewriter.registerSpawnEgg("creeper");
        SpawnEggRewriter.registerSpawnEgg("donkey");
        SpawnEggRewriter.registerSpawnEgg("elder_guardian");
        SpawnEggRewriter.registerSpawnEgg("enderman");
        SpawnEggRewriter.registerSpawnEgg("endermite");
        SpawnEggRewriter.registerSpawnEgg("evocation_illager");
        SpawnEggRewriter.registerSpawnEgg("ghast");
        SpawnEggRewriter.registerSpawnEgg("guardian");
        SpawnEggRewriter.registerSpawnEgg("horse");
        SpawnEggRewriter.registerSpawnEgg("husk");
        SpawnEggRewriter.registerSpawnEgg("llama");
        SpawnEggRewriter.registerSpawnEgg("magma_cube");
        SpawnEggRewriter.registerSpawnEgg("mooshroom");
        SpawnEggRewriter.registerSpawnEgg("mule");
        SpawnEggRewriter.registerSpawnEgg("ocelot");
        SpawnEggRewriter.registerSpawnEgg("parrot");
        SpawnEggRewriter.registerSpawnEgg("pig");
        SpawnEggRewriter.registerSpawnEgg("polar_bear");
        SpawnEggRewriter.registerSpawnEgg("rabbit");
        SpawnEggRewriter.registerSpawnEgg("sheep");
        SpawnEggRewriter.registerSpawnEgg("shulker");
        SpawnEggRewriter.registerSpawnEgg("silverfish");
        SpawnEggRewriter.registerSpawnEgg("skeleton");
        SpawnEggRewriter.registerSpawnEgg("skeleton_horse");
        SpawnEggRewriter.registerSpawnEgg("slime");
        SpawnEggRewriter.registerSpawnEgg("spider");
        SpawnEggRewriter.registerSpawnEgg("squid");
        SpawnEggRewriter.registerSpawnEgg("stray");
        SpawnEggRewriter.registerSpawnEgg("vex");
        SpawnEggRewriter.registerSpawnEgg("villager");
        SpawnEggRewriter.registerSpawnEgg("vindication_illager");
        SpawnEggRewriter.registerSpawnEgg("witch");
        SpawnEggRewriter.registerSpawnEgg("wither_skeleton");
        SpawnEggRewriter.registerSpawnEgg("wolf");
        SpawnEggRewriter.registerSpawnEgg("zombie");
        SpawnEggRewriter.registerSpawnEgg("zombie_horse");
        SpawnEggRewriter.registerSpawnEgg("zombie_pigman");
        SpawnEggRewriter.registerSpawnEgg("zombie_villager");
    }
}

