/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

final class FilterEntry<T> {
    final Postion postion;
    final String name;
    final T filterHandler;
    final String existing;

    FilterEntry(Postion postion, String name, T filterHandler, String existing) {
        this.postion = postion;
        this.name = name;
        this.filterHandler = filterHandler;
        this.existing = existing;
    }

    static enum Postion {
        BEFORE,
        AFTER,
        REPLACE,
        FIRST,
        LAST;

    }
}

