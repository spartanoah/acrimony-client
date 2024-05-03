/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.kqueue.AbstractKQueueChannel;
import io.netty.channel.kqueue.KQueueChannelConfig;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.socket.DuplexChannelConfig;
import io.netty.channel.unix.DomainSocketChannelConfig;
import io.netty.channel.unix.DomainSocketReadMode;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.util.internal.ObjectUtil;
import java.io.IOException;
import java.util.Map;

public final class KQueueDomainSocketChannelConfig
extends KQueueChannelConfig
implements DomainSocketChannelConfig,
DuplexChannelConfig {
    private volatile DomainSocketReadMode mode = DomainSocketReadMode.BYTES;
    private volatile boolean allowHalfClosure;

    KQueueDomainSocketChannelConfig(AbstractKQueueChannel channel) {
        super(channel);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return this.getOptions(super.getOptions(), UnixChannelOption.DOMAIN_SOCKET_READ_MODE, ChannelOption.ALLOW_HALF_CLOSURE, ChannelOption.SO_SNDBUF, ChannelOption.SO_RCVBUF);
    }

    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == UnixChannelOption.DOMAIN_SOCKET_READ_MODE) {
            return (T)((Object)this.getReadMode());
        }
        if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
            return (T)Boolean.valueOf(this.isAllowHalfClosure());
        }
        if (option == ChannelOption.SO_SNDBUF) {
            return (T)Integer.valueOf(this.getSendBufferSize());
        }
        if (option == ChannelOption.SO_RCVBUF) {
            return (T)Integer.valueOf(this.getReceiveBufferSize());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        this.validate(option, value);
        if (option == UnixChannelOption.DOMAIN_SOCKET_READ_MODE) {
            this.setReadMode((DomainSocketReadMode)((Object)value));
        } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
            this.setAllowHalfClosure((Boolean)value);
        } else if (option == ChannelOption.SO_SNDBUF) {
            this.setSendBufferSize((Integer)value);
        } else if (option == ChannelOption.SO_RCVBUF) {
            this.setReceiveBufferSize((Integer)value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    @Override
    public KQueueDomainSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess) {
        super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
        return this;
    }

    @Override
    @Deprecated
    public KQueueDomainSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }

    @Override
    @Deprecated
    public KQueueDomainSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    @Deprecated
    public KQueueDomainSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public KQueueDomainSocketChannelConfig setReadMode(DomainSocketReadMode mode) {
        this.mode = ObjectUtil.checkNotNull(mode, "mode");
        return this;
    }

    @Override
    public DomainSocketReadMode getReadMode() {
        return this.mode;
    }

    public int getSendBufferSize() {
        try {
            return ((KQueueDomainSocketChannel)this.channel).socket.getSendBufferSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KQueueDomainSocketChannelConfig setSendBufferSize(int sendBufferSize) {
        try {
            ((KQueueDomainSocketChannel)this.channel).socket.setSendBufferSize(sendBufferSize);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getReceiveBufferSize() {
        try {
            return ((KQueueDomainSocketChannel)this.channel).socket.getReceiveBufferSize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public KQueueDomainSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        try {
            ((KQueueDomainSocketChannel)this.channel).socket.setReceiveBufferSize(receiveBufferSize);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAllowHalfClosure() {
        return this.allowHalfClosure;
    }

    @Override
    public KQueueDomainSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure) {
        this.allowHalfClosure = allowHalfClosure;
        return this;
    }
}

