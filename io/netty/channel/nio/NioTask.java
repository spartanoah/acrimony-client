/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.nio;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface NioTask<C extends SelectableChannel> {
    public void channelReady(C var1, SelectionKey var2) throws Exception;

    public void channelUnregistered(C var1, Throwable var2) throws Exception;
}

