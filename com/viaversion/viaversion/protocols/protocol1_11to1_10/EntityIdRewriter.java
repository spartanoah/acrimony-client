/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_11to1_10;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.util.Key;

public class EntityIdRewriter {
    private static final BiMap<String, String> oldToNewNames = HashBiMap.create();

    private static void rewrite(String oldName, String newName) {
        oldToNewNames.put(oldName, Key.namespaced(newName));
    }

    public static void toClient(CompoundTag tag) {
        EntityIdRewriter.toClient(tag, false);
    }

    public static void toClient(CompoundTag tag, boolean backwards) {
        StringTag idTag = tag.getStringTag("id");
        if (idTag != null) {
            String newName;
            String string = newName = backwards ? (String)oldToNewNames.inverse().get(idTag.getValue()) : (String)oldToNewNames.get(idTag.getValue());
            if (newName != null) {
                idTag.setValue(newName);
            }
        }
    }

    public static void toClientSpawner(CompoundTag tag) {
        EntityIdRewriter.toClientSpawner(tag, false);
    }

    public static void toClientSpawner(CompoundTag tag, boolean backwards) {
        if (tag == null) {
            return;
        }
        CompoundTag spawnDataTag = tag.getCompoundTag("SpawnData");
        if (spawnDataTag != null) {
            EntityIdRewriter.toClient(spawnDataTag, backwards);
        }
    }

    public static void toClientItem(Item item) {
        EntityIdRewriter.toClientItem(item, false);
    }

    public static void toClientItem(Item item, boolean backwards) {
        if (EntityIdRewriter.hasEntityTag(item)) {
            EntityIdRewriter.toClient(item.tag().getCompoundTag("EntityTag"), backwards);
        }
        if (item != null && item.amount() <= 0) {
            item.setAmount(1);
        }
    }

    public static void toServerItem(Item item) {
        EntityIdRewriter.toServerItem(item, false);
    }

    public static void toServerItem(Item item, boolean backwards) {
        if (!EntityIdRewriter.hasEntityTag(item)) {
            return;
        }
        CompoundTag entityTag = item.tag().getCompoundTag("EntityTag");
        StringTag idTag = entityTag.getStringTag("id");
        if (idTag != null) {
            String newName;
            String string = newName = backwards ? (String)oldToNewNames.get(idTag.getValue()) : (String)oldToNewNames.inverse().get(idTag.getValue());
            if (newName != null) {
                idTag.setValue(newName);
            }
        }
    }

    private static boolean hasEntityTag(Item item) {
        if (item == null || item.identifier() != 383) {
            return false;
        }
        CompoundTag tag = item.tag();
        if (tag == null) {
            return false;
        }
        CompoundTag entityTag = tag.getCompoundTag("EntityTag");
        return entityTag != null && entityTag.getStringTag("id") != null;
    }

    static {
        EntityIdRewriter.rewrite("AreaEffectCloud", "area_effect_cloud");
        EntityIdRewriter.rewrite("ArmorStand", "armor_stand");
        EntityIdRewriter.rewrite("Arrow", "arrow");
        EntityIdRewriter.rewrite("Bat", "bat");
        EntityIdRewriter.rewrite("Blaze", "blaze");
        EntityIdRewriter.rewrite("Boat", "boat");
        EntityIdRewriter.rewrite("CaveSpider", "cave_spider");
        EntityIdRewriter.rewrite("Chicken", "chicken");
        EntityIdRewriter.rewrite("Cow", "cow");
        EntityIdRewriter.rewrite("Creeper", "creeper");
        EntityIdRewriter.rewrite("Donkey", "donkey");
        EntityIdRewriter.rewrite("DragonFireball", "dragon_fireball");
        EntityIdRewriter.rewrite("ElderGuardian", "elder_guardian");
        EntityIdRewriter.rewrite("EnderCrystal", "ender_crystal");
        EntityIdRewriter.rewrite("EnderDragon", "ender_dragon");
        EntityIdRewriter.rewrite("Enderman", "enderman");
        EntityIdRewriter.rewrite("Endermite", "endermite");
        EntityIdRewriter.rewrite("EntityHorse", "horse");
        EntityIdRewriter.rewrite("EyeOfEnderSignal", "eye_of_ender_signal");
        EntityIdRewriter.rewrite("FallingSand", "falling_block");
        EntityIdRewriter.rewrite("Fireball", "fireball");
        EntityIdRewriter.rewrite("FireworksRocketEntity", "fireworks_rocket");
        EntityIdRewriter.rewrite("Ghast", "ghast");
        EntityIdRewriter.rewrite("Giant", "giant");
        EntityIdRewriter.rewrite("Guardian", "guardian");
        EntityIdRewriter.rewrite("Husk", "husk");
        EntityIdRewriter.rewrite("Item", "item");
        EntityIdRewriter.rewrite("ItemFrame", "item_frame");
        EntityIdRewriter.rewrite("LavaSlime", "magma_cube");
        EntityIdRewriter.rewrite("LeashKnot", "leash_knot");
        EntityIdRewriter.rewrite("MinecartChest", "chest_minecart");
        EntityIdRewriter.rewrite("MinecartCommandBlock", "commandblock_minecart");
        EntityIdRewriter.rewrite("MinecartFurnace", "furnace_minecart");
        EntityIdRewriter.rewrite("MinecartHopper", "hopper_minecart");
        EntityIdRewriter.rewrite("MinecartRideable", "minecart");
        EntityIdRewriter.rewrite("MinecartSpawner", "spawner_minecart");
        EntityIdRewriter.rewrite("MinecartTNT", "tnt_minecart");
        EntityIdRewriter.rewrite("Mule", "mule");
        EntityIdRewriter.rewrite("MushroomCow", "mooshroom");
        EntityIdRewriter.rewrite("Ozelot", "ocelot");
        EntityIdRewriter.rewrite("Painting", "painting");
        EntityIdRewriter.rewrite("Pig", "pig");
        EntityIdRewriter.rewrite("PigZombie", "zombie_pigman");
        EntityIdRewriter.rewrite("PolarBear", "polar_bear");
        EntityIdRewriter.rewrite("PrimedTnt", "tnt");
        EntityIdRewriter.rewrite("Rabbit", "rabbit");
        EntityIdRewriter.rewrite("Sheep", "sheep");
        EntityIdRewriter.rewrite("Shulker", "shulker");
        EntityIdRewriter.rewrite("ShulkerBullet", "shulker_bullet");
        EntityIdRewriter.rewrite("Silverfish", "silverfish");
        EntityIdRewriter.rewrite("Skeleton", "skeleton");
        EntityIdRewriter.rewrite("SkeletonHorse", "skeleton_horse");
        EntityIdRewriter.rewrite("Slime", "slime");
        EntityIdRewriter.rewrite("SmallFireball", "small_fireball");
        EntityIdRewriter.rewrite("Snowball", "snowball");
        EntityIdRewriter.rewrite("SnowMan", "snowman");
        EntityIdRewriter.rewrite("SpectralArrow", "spectral_arrow");
        EntityIdRewriter.rewrite("Spider", "spider");
        EntityIdRewriter.rewrite("Squid", "squid");
        EntityIdRewriter.rewrite("Stray", "stray");
        EntityIdRewriter.rewrite("ThrownEgg", "egg");
        EntityIdRewriter.rewrite("ThrownEnderpearl", "ender_pearl");
        EntityIdRewriter.rewrite("ThrownExpBottle", "xp_bottle");
        EntityIdRewriter.rewrite("ThrownPotion", "potion");
        EntityIdRewriter.rewrite("Villager", "villager");
        EntityIdRewriter.rewrite("VillagerGolem", "villager_golem");
        EntityIdRewriter.rewrite("Witch", "witch");
        EntityIdRewriter.rewrite("WitherBoss", "wither");
        EntityIdRewriter.rewrite("WitherSkeleton", "wither_skeleton");
        EntityIdRewriter.rewrite("WitherSkull", "wither_skull");
        EntityIdRewriter.rewrite("Wolf", "wolf");
        EntityIdRewriter.rewrite("XPOrb", "xp_orb");
        EntityIdRewriter.rewrite("Zombie", "zombie");
        EntityIdRewriter.rewrite("ZombieHorse", "zombie_horse");
        EntityIdRewriter.rewrite("ZombieVillager", "zombie_villager");
    }
}

