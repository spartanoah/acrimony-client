/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.profiler;

import net.minecraft.profiler.PlayerUsageSnooper;

public interface IPlayerUsage {
    public void addServerStatsToSnooper(PlayerUsageSnooper var1);

    public void addServerTypeToSnooper(PlayerUsageSnooper var1);

    public boolean isSnooperEnabled();
}

