/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.setting.impl;

import Acrimony.setting.AbstractSetting;
import java.util.function.Supplier;

public class BooleanSetting
extends AbstractSetting {
    private boolean enabled;
    private boolean shownInColor;

    public BooleanSetting(String name, boolean defaultState) {
        super(name);
        this.enabled = defaultState;
    }

    public BooleanSetting(String name, Supplier<Boolean> visibility, boolean defaultState) {
        super(name, visibility);
        this.enabled = defaultState;
    }

    public BooleanSetting(String name, String displayName, Supplier<Boolean> visibility, boolean defaultState) {
        super(name, displayName, visibility);
        this.enabled = defaultState;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isShownInColor() {
        return this.shownInColor;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setShownInColor(boolean shownInColor) {
        this.shownInColor = shownInColor;
    }
}

