/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.util.Args;

public final class RegistryBuilder<I> {
    private final Map<String, I> items = new HashMap<String, I>();

    public static <I> RegistryBuilder<I> create() {
        return new RegistryBuilder<I>();
    }

    RegistryBuilder() {
    }

    public RegistryBuilder<I> register(String id, I item) {
        Args.notEmpty(id, "ID");
        Args.notNull(item, "Item");
        this.items.put(id.toLowerCase(Locale.ROOT), item);
        return this;
    }

    public Registry<I> build() {
        return new Registry<I>(this.items);
    }

    public String toString() {
        return this.items.toString();
    }
}

