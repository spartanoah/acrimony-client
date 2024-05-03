/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.Native;
import java.net.InetSocketAddress;

@Deprecated
public final class SegmentedDatagramPacket
extends io.netty.channel.unix.SegmentedDatagramPacket {
    public SegmentedDatagramPacket(ByteBuf data, int segmentSize, InetSocketAddress recipient) {
        super(data, segmentSize, recipient);
        SegmentedDatagramPacket.checkIsSupported();
    }

    public SegmentedDatagramPacket(ByteBuf data, int segmentSize, InetSocketAddress recipient, InetSocketAddress sender) {
        super(data, segmentSize, recipient, sender);
        SegmentedDatagramPacket.checkIsSupported();
    }

    public static boolean isSupported() {
        return Epoll.isAvailable() && Native.IS_SUPPORTING_SENDMMSG && Native.IS_SUPPORTING_UDP_SEGMENT;
    }

    @Override
    public SegmentedDatagramPacket copy() {
        return new SegmentedDatagramPacket(((ByteBuf)this.content()).copy(), this.segmentSize(), (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    @Override
    public SegmentedDatagramPacket duplicate() {
        return new SegmentedDatagramPacket(((ByteBuf)this.content()).duplicate(), this.segmentSize(), (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    @Override
    public SegmentedDatagramPacket retainedDuplicate() {
        return new SegmentedDatagramPacket(((ByteBuf)this.content()).retainedDuplicate(), this.segmentSize(), (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    @Override
    public SegmentedDatagramPacket replace(ByteBuf content) {
        return new SegmentedDatagramPacket(content, this.segmentSize(), (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    @Override
    public SegmentedDatagramPacket retain() {
        super.retain();
        return this;
    }

    @Override
    public SegmentedDatagramPacket retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public SegmentedDatagramPacket touch() {
        super.touch();
        return this;
    }

    @Override
    public SegmentedDatagramPacket touch(Object hint) {
        super.touch(hint);
        return this;
    }

    private static void checkIsSupported() {
        if (!SegmentedDatagramPacket.isSupported()) {
            throw new IllegalStateException();
        }
    }
}

