/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import io.netty.channel.Channel;
import io.netty.channel.unix.DomainDatagramChannelConfig;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.UnixChannel;

public interface DomainDatagramChannel
extends UnixChannel,
Channel {
    @Override
    public DomainDatagramChannelConfig config();

    public boolean isConnected();

    @Override
    public DomainSocketAddress localAddress();

    @Override
    public DomainSocketAddress remoteAddress();
}

