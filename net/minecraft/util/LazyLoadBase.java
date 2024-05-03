/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

public abstract class LazyLoadBase<T> {
    private T value;
    private boolean isLoaded = false;

    public T getValue() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.value = this.load();
        }
        return this.value;
    }

    protected abstract T load();
}

