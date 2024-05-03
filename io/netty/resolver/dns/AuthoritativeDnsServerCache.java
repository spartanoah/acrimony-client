/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;
import io.netty.resolver.dns.DnsServerAddressStream;
import java.net.InetSocketAddress;

public interface AuthoritativeDnsServerCache {
    public DnsServerAddressStream get(String var1);

    public void cache(String var1, InetSocketAddress var2, long var3, EventLoop var5);

    public void clear();

    public boolean clear(String var1);
}

