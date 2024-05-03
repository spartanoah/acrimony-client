/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.resolver.dns.AuthoritativeDnsServerCache;
import io.netty.resolver.dns.DnsCache;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.SequentialDnsServerAddressStream;
import io.netty.util.internal.ObjectUtil;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

final class AuthoritativeDnsServerCacheAdapter
implements AuthoritativeDnsServerCache {
    private static final DnsRecord[] EMPTY = new DnsRecord[0];
    private final DnsCache cache;

    AuthoritativeDnsServerCacheAdapter(DnsCache cache) {
        this.cache = ObjectUtil.checkNotNull(cache, "cache");
    }

    @Override
    public DnsServerAddressStream get(String hostname) {
        List<? extends DnsCacheEntry> entries = this.cache.get(hostname, EMPTY);
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        if (entries.get(0).cause() != null) {
            return null;
        }
        ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>(entries.size());
        int i = 0;
        do {
            InetAddress addr = entries.get(i).address();
            addresses.add(new InetSocketAddress(addr, 53));
        } while (++i < entries.size());
        return new SequentialDnsServerAddressStream(addresses, 0);
    }

    @Override
    public void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop) {
        if (!address.isUnresolved()) {
            this.cache.cache(hostname, EMPTY, address.getAddress(), originalTtl, loop);
        }
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        return this.cache.clear(hostname);
    }
}

