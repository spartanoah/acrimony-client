/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.kqueue.AbstractKQueueChannel;
import io.netty.channel.kqueue.AbstractKQueueStreamChannel;
import io.netty.channel.kqueue.BsdSocket;
import io.netty.channel.kqueue.KQueueEventLoop;
import io.netty.channel.kqueue.KQueueSocketChannelConfig;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.IovArray;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

public final class KQueueSocketChannel
extends AbstractKQueueStreamChannel
implements SocketChannel {
    private final KQueueSocketChannelConfig config = new KQueueSocketChannelConfig(this);

    public KQueueSocketChannel() {
        super(null, BsdSocket.newSocketStream(), false);
    }

    public KQueueSocketChannel(int fd) {
        super(new BsdSocket(fd));
    }

    KQueueSocketChannel(Channel parent, BsdSocket fd, InetSocketAddress remoteAddress) {
        super(parent, fd, remoteAddress);
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress)super.remoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress)super.localAddress();
    }

    @Override
    public KQueueSocketChannelConfig config() {
        return this.config;
    }

    @Override
    public ServerSocketChannel parent() {
        return (ServerSocketChannel)super.parent();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean doConnect0(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (this.config.isTcpFastOpenConnect()) {
            ByteBuf initialData;
            ChannelOutboundBuffer outbound = this.unsafe().outboundBuffer();
            outbound.addFlush();
            Object curr = outbound.current();
            if (curr instanceof ByteBuf && (initialData = (ByteBuf)curr).isReadable()) {
                IovArray iov = new IovArray(this.config.getAllocator().directBuffer());
                try {
                    iov.add(initialData, initialData.readerIndex(), initialData.readableBytes());
                    int bytesSent = this.socket.connectx((InetSocketAddress)localAddress, (InetSocketAddress)remoteAddress, iov, true);
                    this.writeFilter(true);
                    outbound.removeBytes(Math.abs(bytesSent));
                    boolean bl = bytesSent > 0;
                    return bl;
                } finally {
                    iov.release();
                }
            }
        }
        return super.doConnect0(remoteAddress, localAddress);
    }

    @Override
    protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe() {
        return new KQueueSocketChannelUnsafe();
    }

    private final class KQueueSocketChannelUnsafe
    extends AbstractKQueueStreamChannel.KQueueStreamUnsafe {
        private KQueueSocketChannelUnsafe() {
        }

        @Override
        protected Executor prepareToClose() {
            try {
                if (KQueueSocketChannel.this.isOpen() && KQueueSocketChannel.this.config().getSoLinger() > 0) {
                    ((KQueueEventLoop)KQueueSocketChannel.this.eventLoop()).remove(KQueueSocketChannel.this);
                    return GlobalEventExecutor.INSTANCE;
                }
            } catch (Throwable throwable) {
                // empty catch block
            }
            return null;
        }
    }
}

