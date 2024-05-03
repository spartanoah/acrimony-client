/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.resolver.dns.AuthoritativeDnsServerCache;
import io.netty.resolver.dns.DnsServerAddressStream;
import java.net.InetSocketAddress;

public final class NoopAuthoritativeDnsServerCache
implements AuthoritativeDnsServerCache {
    public static final NoopAuthoritativeDnsServerCache INSTANCE = new NoopAuthoritativeDnsServerCache();

    private NoopAuthoritativeDnsServerCache() {
    }

    @Override
    public DnsServerAddressStream get(String hostname) {
        return null;
    }

    @Override
    public void cache(String hostname, InetSocketAddress address, long originalTtl, EventLoop loop) {
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean clear(String hostname) {
        return false;
    }
}

