/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.util;

import java.util.concurrent.ConcurrentHashMap;

public final class InternCache
extends ConcurrentHashMap<String, String> {
    private static final long serialVersionUID = 1L;
    private static final int MAX_ENTRIES = 180;
    public static final InternCache instance = new InternCache();
    private final Object lock = new Object();

    private InternCache() {
        super(180, 0.8f, 4);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String intern(String input) {
        String result = (String)this.get(input);
        if (result != null) {
            return result;
        }
        if (this.size() >= 180) {
            Object object = this.lock;
            synchronized (object) {
                if (this.size() >= 180) {
                    this.clear();
                }
            }
        }
        result = input.intern();
        this.put(result, result);
        return result;
    }
}

