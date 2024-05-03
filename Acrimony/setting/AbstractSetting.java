/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.setting;

import java.util.function.Supplier;

public abstract class AbstractSetting {
    private final String name;
    private String displayName;
    private Supplier<Boolean> visibility = () -> true;

    public AbstractSetting(String name) {
        this.name = name;
        this.displayName = name;
    }

    public AbstractSetting(String name, Supplier<Boolean> visibility) {
        this.name = name;
        this.displayName = name;
        this.visibility = visibility;
    }

    public AbstractSetting(String name, String displayName, Supplier<Boolean> visibility) {
        this.name = name;
        this.displayName = displayName;
        this.visibility = visibility;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Supplier<Boolean> getVisibility() {
        return this.visibility;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

