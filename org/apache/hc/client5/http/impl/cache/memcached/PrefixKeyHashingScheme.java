/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache.memcached;

import org.apache.hc.client5.http.impl.cache.memcached.KeyHashingScheme;

public final class PrefixKeyHashingScheme
implements KeyHashingScheme {
    private final String prefix;
    private final KeyHashingScheme backingScheme;

    public PrefixKeyHashingScheme(String prefix, KeyHashingScheme backingScheme) {
        this.prefix = prefix;
        this.backingScheme = backingScheme;
    }

    @Override
    public String hash(String storageKey) {
        return this.prefix + this.backingScheme.hash(storageKey);
    }
}

