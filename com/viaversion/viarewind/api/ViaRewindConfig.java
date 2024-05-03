/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.api;

import com.viaversion.viaversion.api.configuration.Config;

public interface ViaRewindConfig
extends Config {
    public CooldownIndicator getCooldownIndicator();

    public boolean isReplaceAdventureMode();

    public boolean isReplaceParticles();

    public int getMaxBookPages();

    public int getMaxBookPageSize();

    public boolean isEmulateWorldBorder();

    public String getWorldBorderParticle();

    public static enum CooldownIndicator {
        TITLE,
        ACTION_BAR,
        BOSS_BAR,
        DISABLED;

    }
}

