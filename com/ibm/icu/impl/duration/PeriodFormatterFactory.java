/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl.duration;

import com.ibm.icu.impl.duration.PeriodFormatter;

public interface PeriodFormatterFactory {
    public PeriodFormatterFactory setLocale(String var1);

    public PeriodFormatterFactory setDisplayLimit(boolean var1);

    public PeriodFormatterFactory setDisplayPastFuture(boolean var1);

    public PeriodFormatterFactory setSeparatorVariant(int var1);

    public PeriodFormatterFactory setUnitVariant(int var1);

    public PeriodFormatterFactory setCountVariant(int var1);

    public PeriodFormatter getFormatter();
}

