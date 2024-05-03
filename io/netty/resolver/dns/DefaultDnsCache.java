/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.resolver.dns.Cache;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

public class DefaultDnsCache
implements DnsCache {
    private final Cache<DefaultDnsCacheEntry> resolveCache = new Cache<DefaultDnsCacheEntry>(){

        @Override
        protected boolean shouldReplaceAll(DefaultDnsCacheEntry entry) {
            return entry.cause() != null;
        }

        @Override
        protected boolean equals(DefaultDnsCacheEntry entry, DefaultDnsCacheEntry otherEntry) {
            if (entry.address() != null) {
                return entry.address().equals(otherEntry.address());
            }
            if (otherEntry.address() != null) {
                return false;
            }
            return entry.cause().equals(otherEntry.cause());
        }
    };
    private final int minTtl;
    private final int maxTtl;
    private final int negativeTtl;

    public DefaultDnsCache() {
        this(0, Cache.MAX_SUPPORTED_TTL_SECS, 0);
    }

    public DefaultDnsCache(int minTtl, int maxTtl, int negativeTtl) {
        this.minTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, ObjectUtil.checkPositiveOrZero(minTtl, "minTtl"));
        this.maxTtl = Math.min(Cache.MAX_SUPPORTED_TTL_SECS, ObjectUtil.checkPositiveOrZero(maxTtl, "maxTtl"));
        if (minTtl > maxTtl) {
            throw new IllegalArgumentException("minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        }
        this.negativeTtl = ObjectUtil.checkPositiveOrZero(negativeTtl, "negativeTtl");
    }

    public int minTtl() {
        return this.minTtl;
    }

    public int maxTtl() {
        return this.maxTtl;
    }

    public int negativeTtl() {
        return this.negativeTtl;
    }

    @Override
    public void clear() {
        this.resolveCache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        ObjectUtil.checkNotNull(hostname, "hostname");
        return this.resolveCache.clear(DefaultDnsCache.appendDot(hostname));
    }

    private static boolean emptyAdditionals(DnsRecord[] additionals) {
        return additionals == null || additionals.length == 0;
    }

    @Override
    public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
        ObjectUtil.checkNotNull(hostname, "hostname");
        if (!DefaultDnsCache.emptyAdditionals(additionals)) {
            return Collections.emptyList();
        }
        return this.resolveCache.get(DefaultDnsCache.appendDot(hostname));
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, InetAddress address, long originalTtl, EventLoop loop) {
        ObjectUtil.checkNotNull(hostname, "hostname");
        ObjectUtil.checkNotNull(address, "address");
        ObjectUtil.checkNotNull(loop, "loop");
        DefaultDnsCacheEntry e = new DefaultDnsCacheEntry(hostname, address);
        if (this.maxTtl == 0 || !DefaultDnsCache.emptyAdditionals(additionals)) {
            return e;
        }
        this.resolveCache.cache(DefaultDnsCache.appendDot(hostname), e, Math.max(this.minTtl, (int)Math.min((long)this.maxTtl, originalTtl)), loop);
        return e;
    }

    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause, EventLoop loop) {
        ObjectUtil.checkNotNull(hostname, "hostname");
        ObjectUtil.checkNotNull(cause, "cause");
        ObjectUtil.checkNotNull(loop, "loop");
        DefaultDnsCacheEntry e = new DefaultDnsCacheEntry(hostname, cause);
        if (this.negativeTtl == 0 || !DefaultDnsCache.emptyAdditionals(additionals)) {
            return e;
        }
        this.resolveCache.cache(DefaultDnsCache.appendDot(hostname), e, this.negativeTtl, loop);
        return e;
    }

    public String toString() {
        return "DefaultDnsCache(minTtl=" + this.minTtl + ", maxTtl=" + this.maxTtl + ", negativeTtl=" + this.negativeTtl + ", cached resolved hostname=" + this.resolveCache.size() + ')';
    }

    private static String appendDot(String hostname) {
        return StringUtil.endsWith((CharSequence)hostname, (char)'.') ? hostname : hostname + '.';
    }

    private static final class DefaultDnsCacheEntry
    implements DnsCacheEntry {
        private final String hostname;
        private final InetAddress address;
        private final Throwable cause;

        DefaultDnsCacheEntry(String hostname, InetAddress address) {
            this.hostname = hostname;
            this.address = address;
            this.cause = null;
        }

        DefaultDnsCacheEntry(String hostname, Throwable cause) {
            this.hostname = hostname;
            this.cause = cause;
            this.address = null;
        }

        @Override
        public InetAddress address() {
            return this.address;
        }

        @Override
        public Throwable cause() {
            return this.cause;
        }

        String hostname() {
            return this.hostname;
        }

        public String toString() {
            if (this.cause != null) {
                return this.hostname + '/' + this.cause;
            }
            return this.address.toString();
        }
    }
}

