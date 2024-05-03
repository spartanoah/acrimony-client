/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.setting.impl;

import Acrimony.setting.AbstractSetting;
import java.util.function.Supplier;

public class DoubleSetting
extends AbstractSetting {
    private double value;
    private double min;
    private double max;
    private double increment;

    public DoubleSetting(String name, double value, double min, double max, double increment) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public DoubleSetting(String name, Supplier<Boolean> visibility, double value, double min, double max, double increment) {
        super(name, visibility);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public DoubleSetting(String name, String displayName, Supplier<Boolean> visibility, double value, double min, double max, double increment) {
        super(name, displayName, visibility);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public void setValue(double value) {
        double precision = 1.0 / this.increment;
        this.value = Math.max(this.min, Math.min(this.max, (double)Math.round(value * precision) / precision));
    }

    public String getStringValue() {
        if (this.value % 1.0 == 0.0) {
            return "" + (int)this.value;
        }
        return "" + this.value;
    }

    public double getValue() {
        return this.value;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public double getIncrement() {
        return this.increment;
    }
}

