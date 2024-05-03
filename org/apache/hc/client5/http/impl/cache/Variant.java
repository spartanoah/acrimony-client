/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import org.apache.hc.client5.http.cache.HttpCacheEntry;

class Variant {
    private final String cacheKey;
    private final HttpCacheEntry entry;

    public Variant(String cacheKey, HttpCacheEntry entry) {
        this.cacheKey = cacheKey;
        this.entry = entry;
    }

    public String getCacheKey() {
        return this.cacheKey;
    }

    public HttpCacheEntry getEntry() {
        return this.entry;
    }

    public String toString() {
        return this.cacheKey;
    }
}

