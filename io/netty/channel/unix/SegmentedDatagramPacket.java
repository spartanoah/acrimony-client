/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;

public class SegmentedDatagramPacket
extends DatagramPacket {
    private final int segmentSize;

    public SegmentedDatagramPacket(ByteBuf data, int segmentSize, InetSocketAddress recipient) {
        super(data, recipient);
        this.segmentSize = ObjectUtil.checkPositive(segmentSize, "segmentSize");
    }

    public SegmentedDatagramPacket(ByteBuf data, int segmentSize, InetSocketAddress recipient, InetSocketAddress sender) {
        super(data, recipient, sender);
        this.segmentSize = ObjectUtil.checkPositive(segmentSize, "segmentSize");
    }

    public int segmentSize() {
        return this.segmentSize;
    }

    @Override
    public SegmentedDatagramPacket copy() {
        return new SegmentedDatagramPacket(((ByteBuf)this.content()).copy(), this.segmentSize, (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    @Override
    public SegmentedDatagramPacket duplicate() {
        return new SegmentedDatagramPacket(((ByteBuf)this.content()).duplicate(), this.segmentSize, (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    public SegmentedDatagramPacket retainedDuplicate() {
        return new SegmentedDatagramPacket(((ByteBuf)this.content()).retainedDuplicate(), this.segmentSize, (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
    }

    public SegmentedDatagramPacket replace(ByteBuf content) {
        return new SegmentedDatagramPacket(content, this.segmentSize, (InetSocketAddress)this.recipient(), (InetSocketAddress)this.sender());
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

    public SegmentedDatagramPacket touch() {
        super.touch();
        return this;
    }

    public SegmentedDatagramPacket touch(Object hint) {
        super.touch(hint);
        return this;
    }
}

