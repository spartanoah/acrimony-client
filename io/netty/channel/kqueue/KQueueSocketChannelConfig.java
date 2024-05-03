/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.kqueue.KQueueChannelConfig;
import io.netty.channel.kqueue.KQueueChannelOption;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.util.Map;

public final class KQueueSocketChannelConfig
extends KQueueChannelConfig
implements SocketChannelConfig {
    private volatile boolean allowHalfClosure;
    private volatile boolean tcpFastopen;

    KQueueSocketChannelConfig(KQueueSocketChannel channel) {
        super(channel);
        if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
            this.setTcpNoDelay(true);
        }
        this.calculateMaxBytesPerGatheringWrite();
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return this.getOptions(super.getOptions(), ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE, KQueueChannelOption.SO_SNDLOWAT, KQueueChannelOption.TCP_NOPUSH);
    }

    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == ChannelOption.SO_RCVBUF) {
            return (T)Integer.valueOf(this.getReceiveBufferSize());
        }
        if (option == ChannelOption.SO_SNDBUF) {
            return (T)Integer.valueOf(this.getSendBufferSize());
        }
        if (option == ChannelOption.TCP_NODELAY) {
            return (T)Boolean.valueOf(this.isTcpNoDelay());
        }
        if (option == ChannelOption.SO_KEEPALIVE) {
            return (T)Boolean.valueOf(this.isKeepAlive());
        }
        if (option == ChannelOption.SO_REUSEADDR) {
            return (T)Boolean.valueOf(this.isReuseAddress());
        }
        if (option == ChannelOption.SO_LINGER) {
            return (T)Integer.valueOf(this.getSoLinger());
        }
        if (option == ChannelOption.IP_TOS) {
            return (T)Integer.valueOf(this.getTrafficClass());
        }
        if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
            return (T)Boolean.valueOf(this.isAllowHalfClosure());
        }
        if (option == KQueueChannelOption.SO_SNDLOWAT) {
            return (T)Integer.valueOf(this.getSndLowAt());
        }
        if (option == KQueueChannelOption.TCP_NOPUSH) {
            return (T)Boolean.valueOf(this.isTcpNoPush());
        }
        if (option == ChannelOption.TCP_FASTOPEN_CONNECT) {
            return (T)Boolean.valueOf(this.isTcpFastOpenConnect());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        this.validate(option, value);
        if (option == ChannelOption.SO_RCVBUF) {
            this.setReceiveBufferSize((Integer)value);
        } else if (option == ChannelOption.SO_SNDBUF) {
            this.setSendBufferSize((Integer)value);
        } else if (option == ChannelOption.TCP_NODELAY) {
            this.setTcpNoDelay((Boolean)value);
        } else if (option == ChannelOption.SO_KEEPALIVE) {
            this.setKeepAlive((Boolean)value);
        } else if (option == ChannelOption.SO_REUSEADDR) {
            this.setReuseAddress((Boolean)value);
        } else if (option == ChannelOption.SO_LINGER) {
            this.setSoLinger((Integer)value);
        } else if (option == ChannelOption.IP_TOS) {
            this.setTrafficClass((Integer)value);
        } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
            this.setAllowHalfClosure((Boolean)value);
        } else if (option == KQueueChannelOption.SO_SNDLOWAT) {
            this.setSndLowAt((Integer)value);
        } else if (option == KQueueChannelOption.TCP_NOPUSH) {
            this.setTcpNoPush((Boolean)value);
        } else if (option == ChannelOption.TCP_FASTOPEN_CONNECT) {
            this.setTcpFastOpenConnect((Boolean)value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    @Override
    public int getReceiveBufferSize() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.getReceiveBufferSize();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public int getSendBufferSize() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.getSendBufferSize();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public int getSoLinger() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.getSoLinger();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public int getTrafficClass() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.getTrafficClass();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public boolean isKeepAlive() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.isKeepAlive();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public boolean isReuseAddress() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.isReuseAddress();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public boolean isTcpNoDelay() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.isTcpNoDelay();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    public int getSndLowAt() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.getSndLowAt();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    public void setSndLowAt(int sndLowAt) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setSndLowAt(sndLowAt);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    public boolean isTcpNoPush() {
        try {
            return ((KQueueSocketChannel)this.channel).socket.isTcpNoPush();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    public void setTcpNoPush(boolean tcpNoPush) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setTcpNoPush(tcpNoPush);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setKeepAlive(boolean keepAlive) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setKeepAlive(keepAlive);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setReceiveBufferSize(receiveBufferSize);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setReuseAddress(boolean reuseAddress) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setReuseAddress(reuseAddress);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setSendBufferSize(int sendBufferSize) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setSendBufferSize(sendBufferSize);
            this.calculateMaxBytesPerGatheringWrite();
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setSoLinger(int soLinger) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setSoLinger(soLinger);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setTcpNoDelay(tcpNoDelay);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public KQueueSocketChannelConfig setTrafficClass(int trafficClass) {
        try {
            ((KQueueSocketChannel)this.channel).socket.setTrafficClass(trafficClass);
            return this;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    public boolean isAllowHalfClosure() {
        return this.allowHalfClosure;
    }

    public KQueueSocketChannelConfig setTcpFastOpenConnect(boolean fastOpenConnect) {
        this.tcpFastopen = fastOpenConnect;
        return this;
    }

    public boolean isTcpFastOpenConnect() {
        return this.tcpFastopen;
    }

    @Override
    public KQueueSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess) {
        super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure) {
        this.allowHalfClosure = allowHalfClosure;
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    @Deprecated
    public KQueueSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    @Deprecated
    public KQueueSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    @Deprecated
    public KQueueSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public KQueueSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }

    private void calculateMaxBytesPerGatheringWrite() {
        int newSendBufferSize = this.getSendBufferSize() << 1;
        if (newSendBufferSize > 0) {
            this.setMaxBytesPerGatheringWrite(this.getSendBufferSize() << 1);
        }
    }
}

