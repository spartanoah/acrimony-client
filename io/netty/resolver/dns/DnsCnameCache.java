/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.channel.EventLoop;

public interface DnsCnameCache {
    public String get(String var1);

    public void cache(String var1, String var2, long var3, EventLoop var5);

    public void clear();

    public boolean clear(String var1);
}

