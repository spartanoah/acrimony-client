/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.stats;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.IChatComponent;

public class StatBasic
extends StatBase {
    public StatBasic(String statIdIn, IChatComponent statNameIn, IStatType typeIn) {
        super(statIdIn, statNameIn, typeIn);
    }

    public StatBasic(String statIdIn, IChatComponent statNameIn) {
        super(statIdIn, statNameIn);
    }

    @Override
    public StatBase registerStat() {
        super.registerStat();
        StatList.generalStats.add(this);
        return this;
    }
}

