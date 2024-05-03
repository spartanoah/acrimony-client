/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.epoll;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollChannelConfig;
import io.netty.channel.epoll.EpollDomainDatagramChannel;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.unix.DomainDatagramChannelConfig;
import java.io.IOException;
import java.util.Map;

public final class EpollDomainDatagramChannelConfig
extends EpollChannelConfig
implements DomainDatagramChannelConfig {
    private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = new FixedRecvByteBufAllocator(2048);
    private boolean activeOnOpen;

    EpollDomainDatagramChannelConfig(EpollDomainDatagramChannel channel) {
        super(channel);
        this.setRecvByteBufAllocator(DEFAULT_RCVBUF_ALLOCATOR);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return this.getOptions(super.getOptions(), ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, ChannelOption.SO_SNDBUF);
    }

    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
            return (T)Boolean.valueOf(this.activeOnOpen);
        }
        if (option == ChannelOption.SO_SNDBUF) {
            return (T)Integer.valueOf(this.getSendBufferSize());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        this.validate(option, value);
        if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
            this.setActiveOnOpen((Boolean)value);
        } else if (option == ChannelOption.SO_SNDBUF) {
            this.setSendBufferSize((Integer)value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    private void setActiveOnOpen(boolean activeOnOpen) {
        if (this.channel.isRegistered()) {
            throw new IllegalStateException("Can only changed before channel was registered");
        }
        this.activeOnOpen = activeOnOpen;
    }

    boolean getActiveOnOpen() {
        return this.activeOnOpen;
    }

    @Override
    public EpollDomainDatagramChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setEpollMode(EpollMode mode) {
        super.setEpollMode(mode);
        return this;
    }

    @Override
    @Deprecated
    public EpollDomainDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    public EpollDomainDatagramChannelConfig setMaxMessagesPerWrite(int maxMessagesPerWrite) {
        super.setMaxMessagesPerWrite(maxMessagesPerWrite);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setSendBufferSize(int sendBufferSize) {
        try {
            ((EpollDomainDatagramChannel)this.channel).socket.setSendBufferSize(sendBufferSize);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public int getSendBufferSize() {
        try {
            return ((EpollDomainDatagramChannel)this.channel).socket.getSendBufferSize();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public EpollDomainDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public EpollDomainDatagramChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }
}

