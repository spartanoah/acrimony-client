/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.bootstrap;

import io.netty.channel.Channel;

public interface ChannelFactory<T extends Channel> {
    public T newChannel();
}

