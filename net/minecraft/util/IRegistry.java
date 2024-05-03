/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.util;

public interface IRegistry<K, V>
extends Iterable<V> {
    public V getObject(K var1);

    public void putObject(K var1, V var2);
}

