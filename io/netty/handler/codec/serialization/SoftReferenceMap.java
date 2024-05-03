/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.serialization;

import io.netty.handler.codec.serialization.ReferenceMap;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;

final class SoftReferenceMap<K, V>
extends ReferenceMap<K, V> {
    SoftReferenceMap(Map<K, Reference<V>> delegate) {
        super(delegate);
    }

    @Override
    Reference<V> fold(V value) {
        return new SoftReference<V>(value);
    }
}

