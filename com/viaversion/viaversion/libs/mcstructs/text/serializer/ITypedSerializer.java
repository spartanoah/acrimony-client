/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer;

public interface ITypedSerializer<T, O> {
    public T serialize(O var1);

    public O deserialize(T var1);
}

