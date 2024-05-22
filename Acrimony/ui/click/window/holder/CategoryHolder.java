/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.ui.click.window.holder;

import Acrimony.module.Category;
import Acrimony.ui.click.dropdown.holder.ModuleHolder;
import java.util.ArrayList;

public class CategoryHolder {
    private final Category category;
    private final ArrayList<ModuleHolder> modules;

    public CategoryHolder(Category category, ArrayList<ModuleHolder> modules) {
        this.category = category;
        this.modules = modules;
    }

    public Category getCategory() {
        return this.category;
    }

    public ArrayList<ModuleHolder> getModules() {
        return this.modules;
    }
}

