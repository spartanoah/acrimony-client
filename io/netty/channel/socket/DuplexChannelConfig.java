/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.socket;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;

public interface DuplexChannelConfig
extends ChannelConfig {
    public boolean isAllowHalfClosure();

    public DuplexChannelConfig setAllowHalfClosure(boolean var1);

    @Override
    @Deprecated
    public DuplexChannelConfig setMaxMessagesPerRead(int var1);

    @Override
    public DuplexChannelConfig setWriteSpinCount(int var1);

    @Override
    public DuplexChannelConfig setAllocator(ByteBufAllocator var1);

    @Override
    public DuplexChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator var1);

    @Override
    public DuplexChannelConfig setAutoRead(boolean var1);

    @Override
    public DuplexChannelConfig setAutoClose(boolean var1);

    @Override
    public DuplexChannelConfig setMessageSizeEstimator(MessageSizeEstimator var1);

    public DuplexChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark var1);
}

