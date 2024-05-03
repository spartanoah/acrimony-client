/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model;

import java.util.HashMap;

public final class ParticleIndex1_7_6_10
extends Enum<ParticleIndex1_7_6_10> {
    public static final /* enum */ ParticleIndex1_7_6_10 EXPLOSION_NORMAL;
    public static final /* enum */ ParticleIndex1_7_6_10 EXPLOSION_LARGE;
    public static final /* enum */ ParticleIndex1_7_6_10 EXPLOSION_HUGE;
    public static final /* enum */ ParticleIndex1_7_6_10 FIREWORKS_SPARK;
    public static final /* enum */ ParticleIndex1_7_6_10 WATER_BUBBLE;
    public static final /* enum */ ParticleIndex1_7_6_10 WATER_SPLASH;
    public static final /* enum */ ParticleIndex1_7_6_10 WATER_WAKE;
    public static final /* enum */ ParticleIndex1_7_6_10 SUSPENDED;
    public static final /* enum */ ParticleIndex1_7_6_10 SUSPENDED_DEPTH;
    public static final /* enum */ ParticleIndex1_7_6_10 CRIT;
    public static final /* enum */ ParticleIndex1_7_6_10 CRIT_MAGIC;
    public static final /* enum */ ParticleIndex1_7_6_10 SMOKE_NORMAL;
    public static final /* enum */ ParticleIndex1_7_6_10 SMOKE_LARGE;
    public static final /* enum */ ParticleIndex1_7_6_10 SPELL;
    public static final /* enum */ ParticleIndex1_7_6_10 SPELL_INSTANT;
    public static final /* enum */ ParticleIndex1_7_6_10 SPELL_MOB;
    public static final /* enum */ ParticleIndex1_7_6_10 SPELL_MOB_AMBIENT;
    public static final /* enum */ ParticleIndex1_7_6_10 SPELL_WITCH;
    public static final /* enum */ ParticleIndex1_7_6_10 DRIP_WATER;
    public static final /* enum */ ParticleIndex1_7_6_10 DRIP_LAVA;
    public static final /* enum */ ParticleIndex1_7_6_10 VILLAGER_ANGRY;
    public static final /* enum */ ParticleIndex1_7_6_10 VILLAGER_HAPPY;
    public static final /* enum */ ParticleIndex1_7_6_10 TOWN_AURA;
    public static final /* enum */ ParticleIndex1_7_6_10 NOTE;
    public static final /* enum */ ParticleIndex1_7_6_10 PORTAL;
    public static final /* enum */ ParticleIndex1_7_6_10 ENCHANTMENT_TABLE;
    public static final /* enum */ ParticleIndex1_7_6_10 FLAME;
    public static final /* enum */ ParticleIndex1_7_6_10 LAVA;
    public static final /* enum */ ParticleIndex1_7_6_10 FOOTSTEP;
    public static final /* enum */ ParticleIndex1_7_6_10 CLOUD;
    public static final /* enum */ ParticleIndex1_7_6_10 REDSTONE;
    public static final /* enum */ ParticleIndex1_7_6_10 SNOWBALL;
    public static final /* enum */ ParticleIndex1_7_6_10 SNOW_SHOVEL;
    public static final /* enum */ ParticleIndex1_7_6_10 SLIME;
    public static final /* enum */ ParticleIndex1_7_6_10 HEART;
    public static final /* enum */ ParticleIndex1_7_6_10 BARRIER;
    public static final /* enum */ ParticleIndex1_7_6_10 ICON_CRACK;
    public static final /* enum */ ParticleIndex1_7_6_10 BLOCK_CRACK;
    public static final /* enum */ ParticleIndex1_7_6_10 BLOCK_DUST;
    public static final /* enum */ ParticleIndex1_7_6_10 WATER_DROP;
    public static final /* enum */ ParticleIndex1_7_6_10 ITEM_TAKE;
    public static final /* enum */ ParticleIndex1_7_6_10 MOB_APPEARANCE;
    public final String name;
    public final int extra;
    private static final HashMap<String, ParticleIndex1_7_6_10> particleMap;
    private static final /* synthetic */ ParticleIndex1_7_6_10[] $VALUES;

    public static ParticleIndex1_7_6_10[] values() {
        return (ParticleIndex1_7_6_10[])$VALUES.clone();
    }

    public static ParticleIndex1_7_6_10 valueOf(String name) {
        return Enum.valueOf(ParticleIndex1_7_6_10.class, name);
    }

    private ParticleIndex1_7_6_10(String name) {
        this(name, 0);
    }

    private ParticleIndex1_7_6_10(String name, int extra) {
        this.name = name;
        this.extra = extra;
    }

    public static ParticleIndex1_7_6_10 find(String part) {
        return particleMap.get(part);
    }

    public static ParticleIndex1_7_6_10 find(int id) {
        if (id < 0) {
            return null;
        }
        ParticleIndex1_7_6_10[] values = ParticleIndex1_7_6_10.values();
        return id >= values.length ? null : values[id];
    }

    static {
        ParticleIndex1_7_6_10[] particles;
        EXPLOSION_NORMAL = new ParticleIndex1_7_6_10("explode");
        EXPLOSION_LARGE = new ParticleIndex1_7_6_10("largeexplode");
        EXPLOSION_HUGE = new ParticleIndex1_7_6_10("hugeexplosion");
        FIREWORKS_SPARK = new ParticleIndex1_7_6_10("fireworksSpark");
        WATER_BUBBLE = new ParticleIndex1_7_6_10("bubble");
        WATER_SPLASH = new ParticleIndex1_7_6_10("splash");
        WATER_WAKE = new ParticleIndex1_7_6_10("wake");
        SUSPENDED = new ParticleIndex1_7_6_10("suspended");
        SUSPENDED_DEPTH = new ParticleIndex1_7_6_10("depthsuspend");
        CRIT = new ParticleIndex1_7_6_10("crit");
        CRIT_MAGIC = new ParticleIndex1_7_6_10("magicCrit");
        SMOKE_NORMAL = new ParticleIndex1_7_6_10("smoke");
        SMOKE_LARGE = new ParticleIndex1_7_6_10("largesmoke");
        SPELL = new ParticleIndex1_7_6_10("spell");
        SPELL_INSTANT = new ParticleIndex1_7_6_10("instantSpell");
        SPELL_MOB = new ParticleIndex1_7_6_10("mobSpell");
        SPELL_MOB_AMBIENT = new ParticleIndex1_7_6_10("mobSpellAmbient");
        SPELL_WITCH = new ParticleIndex1_7_6_10("witchMagic");
        DRIP_WATER = new ParticleIndex1_7_6_10("dripWater");
        DRIP_LAVA = new ParticleIndex1_7_6_10("dripLava");
        VILLAGER_ANGRY = new ParticleIndex1_7_6_10("angryVillager");
        VILLAGER_HAPPY = new ParticleIndex1_7_6_10("happyVillager");
        TOWN_AURA = new ParticleIndex1_7_6_10("townaura");
        NOTE = new ParticleIndex1_7_6_10("note");
        PORTAL = new ParticleIndex1_7_6_10("portal");
        ENCHANTMENT_TABLE = new ParticleIndex1_7_6_10("enchantmenttable");
        FLAME = new ParticleIndex1_7_6_10("flame");
        LAVA = new ParticleIndex1_7_6_10("lava");
        FOOTSTEP = new ParticleIndex1_7_6_10("footstep");
        CLOUD = new ParticleIndex1_7_6_10("cloud");
        REDSTONE = new ParticleIndex1_7_6_10("reddust");
        SNOWBALL = new ParticleIndex1_7_6_10("snowballpoof");
        SNOW_SHOVEL = new ParticleIndex1_7_6_10("snowshovel");
        SLIME = new ParticleIndex1_7_6_10("slime");
        HEART = new ParticleIndex1_7_6_10("heart");
        BARRIER = new ParticleIndex1_7_6_10("barrier");
        ICON_CRACK = new ParticleIndex1_7_6_10("iconcrack", 2);
        BLOCK_CRACK = new ParticleIndex1_7_6_10("blockcrack", 1);
        BLOCK_DUST = new ParticleIndex1_7_6_10("blockdust", 1);
        WATER_DROP = new ParticleIndex1_7_6_10("droplet");
        ITEM_TAKE = new ParticleIndex1_7_6_10("take");
        MOB_APPEARANCE = new ParticleIndex1_7_6_10("mobappearance");
        $VALUES = new ParticleIndex1_7_6_10[]{EXPLOSION_NORMAL, EXPLOSION_LARGE, EXPLOSION_HUGE, FIREWORKS_SPARK, WATER_BUBBLE, WATER_SPLASH, WATER_WAKE, SUSPENDED, SUSPENDED_DEPTH, CRIT, CRIT_MAGIC, SMOKE_NORMAL, SMOKE_LARGE, SPELL, SPELL_INSTANT, SPELL_MOB, SPELL_MOB_AMBIENT, SPELL_WITCH, DRIP_WATER, DRIP_LAVA, VILLAGER_ANGRY, VILLAGER_HAPPY, TOWN_AURA, NOTE, PORTAL, ENCHANTMENT_TABLE, FLAME, LAVA, FOOTSTEP, CLOUD, REDSTONE, SNOWBALL, SNOW_SHOVEL, SLIME, HEART, BARRIER, ICON_CRACK, BLOCK_CRACK, BLOCK_DUST, WATER_DROP, ITEM_TAKE, MOB_APPEARANCE};
        particleMap = new HashMap();
        for (ParticleIndex1_7_6_10 particle : particles = ParticleIndex1_7_6_10.values()) {
            particleMap.put(particle.name, particle);
        }
    }
}

