/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.resolver.dns.Cache;
import io.netty.resolver.dns.DnsCnameCache;
import io.netty.util.AsciiString;
import io.netty.util.internal.ObjectUtil;
import java.util.List;

public final class DefaultDnsCnameCache
implements DnsCnameCache {
    private final int minTtl;
    private final int maxTtl;
    private final Cache<String> cache = new Cache<String>(){

        @Override
        protected boolean shouldReplaceAll(String entry) {
            return true;
        }

        @Override
        protected boolean equals(String entry, String otherEntry) {
            return AsciiString.contentEqualsIgnoreCase(entry, otherEntry);
        }
    };

    public DefaultDnsCnameCache() {
        this(0, Cache.MAX_SUPPORTED_TTL_SECS);
    }

    public DefaultDnsCnameCache(int minTtl, int maxTtl) {
        this.minTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, ObjectUtil.checkPositiveOrZero(minTtl, "minTtl"));
        this.maxTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, ObjectUtil.checkPositive(maxTtl, "maxTtl"));
        if (minTtl > maxTtl) {
            throw new IllegalArgumentException("minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        }
    }

    @Override
    public String get(String hostname) {
        List<String> cached = this.cache.get(ObjectUtil.checkNotNull(hostname, "hostname"));
        if (cached == null || cached.isEmpty()) {
            return null;
        }
        return cached.get(0);
    }

    @Override
    public void cache(String hostname, String cname, long originalTtl, EventLoop loop) {
        ObjectUtil.checkNotNull(hostname, "hostname");
        ObjectUtil.checkNotNull(cname, "cname");
        ObjectUtil.checkNotNull(loop, "loop");
        this.cache.cache(hostname, cname, Math.max(this.minTtl, (int)Math.min((long)this.maxTtl, originalTtl)), loop);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        return this.cache.clear(ObjectUtil.checkNotNull(hostname, "hostname"));
    }
}

