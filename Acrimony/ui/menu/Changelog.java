/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.menu;

import Acrimony.ui.menu.ChangelogType;

public class Changelog {
    public String[] description;
    public ChangelogType type;

    public Changelog(String[] description, ChangelogType type) {
        this.description = description;
        this.type = type;
    }

    public String[] getDescription() {
        return this.description;
    }

    public ChangelogType getType() {
        return this.type;
    }
}

