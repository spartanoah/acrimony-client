/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.setting.impl;

import Acrimony.setting.AbstractSetting;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class EnumModeSetting<T extends Enum>
extends AbstractSetting {
    private int index;
    private List<T> list;

    public EnumModeSetting(String name, T t, T ... list) {
        super(name);
        this.list = Arrays.asList(list);
        this.setMode(t);
    }

    public EnumModeSetting(String name, Supplier<Boolean> visibility, T t, T ... list) {
        super(name, visibility);
        this.list = Arrays.asList(list);
        this.setMode(t);
    }

    public T getMode() {
        return (T)((Enum)this.list.get(this.index));
    }

    public void setMode(T mode) {
        this.index = this.list.indexOf(mode);
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

    public List<T> getList() {
        return this.list;
    }
}

