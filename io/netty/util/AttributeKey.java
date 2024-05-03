/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util;

import io.netty.util.UniqueName;
import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.ConcurrentMap;

public final class AttributeKey<T>
extends UniqueName {
    private static final ConcurrentMap<String, Boolean> names = PlatformDependent.newConcurrentHashMap();

    public static <T> AttributeKey<T> valueOf(String name) {
        return new AttributeKey<T>(name);
    }

    @Deprecated
    public AttributeKey(String name) {
        super(names, name, new Object[0]);
    }
}

