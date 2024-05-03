/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;

public interface EventLoop
extends EventExecutor,
EventLoopGroup {
    @Override
    public EventLoopGroup parent();
}

