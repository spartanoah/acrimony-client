/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.setting.impl;

import Acrimony.setting.AbstractSetting;
import java.util.function.Supplier;

public class IntegerSetting
extends AbstractSetting {
    private int value;
    private int min;
    private int max;
    private int increment;

    public IntegerSetting(String name, int value, int min, int max, int increment) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public IntegerSetting(String name, Supplier<Boolean> visibility, int value, int min, int max, int increment) {
        super(name, visibility);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public IntegerSetting(String name, String displayName, Supplier<Boolean> visibility, int value, int min, int max, int increment) {
        super(name, displayName, visibility);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public void setValue(int value) {
        double doubleMin = this.min;
        double doubleMax = this.max;
        double doubleIncrement = this.increment;
        double doubleValue = value;
        double precision = 1.0 / doubleIncrement;
        this.value = (int)Math.max(doubleMin, Math.min(doubleMax, (double)Math.round(doubleValue * precision) / precision));
    }

    public int getValue() {
        return this.value;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getIncrement() {
        return this.increment;
    }
}

