/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.setting.impl;

import Acrimony.setting.AbstractSetting;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting
extends AbstractSetting {
    private int index;
    private List<String> list;

    public ModeSetting(String name, String t, String ... list) {
        super(name);
        this.list = Arrays.asList(list);
        this.setMode(t);
    }

    public ModeSetting(String name, Supplier<Boolean> visibility, String t, String ... list) {
        super(name, visibility);
        this.list = Arrays.asList(list);
        this.setMode(t);
    }

    public ModeSetting(String name, String displayName, Supplier<Boolean> visibility, String t, String ... list) {
        super(name, displayName, visibility);
        this.list = Arrays.asList(list);
        this.setMode(t);
    }

    public String getMode() {
        if (this.index >= this.list.size() || this.index < 0) {
            this.index = 0;
        }
        return this.list.get(this.index);
    }

    public void setMode(String mode) {
        this.index = this.list.indexOf(mode);
    }

    public boolean is(String mode) {
        if (this.index >= this.list.size() || this.index < 0) {
            this.index = 0;
        }
        return this.list.get(this.index).equals(mode);
    }

    public void increment() {
        this.index = this.index < this.list.size() - 1 ? ++this.index : 0;
    }

    public void decrement() {
        this.index = this.index > 0 ? --this.index : this.list.size() - 1;
    }

    public int getIndex() {
        return this.index;
    }

    public List<String> getList() {
        return this.list;
    }
}

