/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.epoll.SegmentedDatagramPacket;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.unix.IovArray;
import io.netty.channel.unix.Limits;
import io.netty.channel.unix.NativeInetAddress;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

final class NativeDatagramPacketArray {
    private final NativeDatagramPacket[] packets = new NativeDatagramPacket[Limits.UIO_MAX_IOV];
    private final IovArray iovArray = new IovArray();
    private final byte[] ipv4Bytes = new byte[4];
    private final MyMessageProcessor processor = new MyMessageProcessor();
    private int count;

    NativeDatagramPacketArray() {
        for (int i = 0; i < this.packets.length; ++i) {
            this.packets[i] = new NativeDatagramPacket();
        }
    }

    boolean addWritable(ByteBuf buf, int index, int len) {
        return this.add0(buf, index, len, 0, null);
    }

    private boolean add0(ByteBuf buf, int index, int len, int segmentLen, InetSocketAddress recipient) {
        if (this.count == this.packets.length) {
            return false;
        }
        if (len == 0) {
            return true;
        }
        int offset = this.iovArray.count();
        if (offset == Limits.IOV_MAX || !this.iovArray.add(buf, index, len)) {
            return false;
        }
        NativeDatagramPacket p = this.packets[this.count];
        p.init(this.iovArray.memoryAddress(offset), this.iovArray.count() - offset, segmentLen, recipient);
        ++this.count;
        return true;
    }

    void add(ChannelOutboundBuffer buffer, boolean connected, int maxMessagesPerWrite) throws Exception {
        this.processor.connected = connected;
        this.processor.maxMessagesPerWrite = maxMessagesPerWrite;
        buffer.forEachFlushedMessage(this.processor);
    }

    int count() {
        return this.count;
    }

    NativeDatagramPacket[] packets() {
        return this.packets;
    }

    void clear() {
        this.count = 0;
        this.iovArray.clear();
    }

    void release() {
        this.iovArray.release();
    }

    private static InetSocketAddress newAddress(byte[] addr, int addrLen, int port, int scopeId, byte[] ipv4Bytes) throws UnknownHostException {
        InetAddress address;
        if (addrLen == ipv4Bytes.length) {
            System.arraycopy(addr, 0, ipv4Bytes, 0, addrLen);
            address = InetAddress.getByAddress(ipv4Bytes);
        } else {
            address = Inet6Address.getByAddress(null, addr, scopeId);
        }
        return new InetSocketAddress(address, port);
    }

    final class NativeDatagramPacket {
        private long memoryAddress;
        private int count;
        private final byte[] senderAddr = new byte[16];
        private int senderAddrLen;
        private int senderScopeId;
        private int senderPort;
        private final byte[] recipientAddr = new byte[16];
        private int recipientAddrLen;
        private int recipientScopeId;
        private int recipientPort;
        private int segmentSize;

        NativeDatagramPacket() {
        }

        private void init(long memoryAddress, int count, int segmentSize, InetSocketAddress recipient) {
            this.memoryAddress = memoryAddress;
            this.count = count;
            this.segmentSize = segmentSize;
            this.senderScopeId = 0;
            this.senderPort = 0;
            this.senderAddrLen = 0;
            if (recipient == null) {
                this.recipientScopeId = 0;
                this.recipientPort = 0;
                this.recipientAddrLen = 0;
            } else {
                InetAddress address = recipient.getAddress();
                if (address instanceof Inet6Address) {
                    System.arraycopy(address.getAddress(), 0, this.recipientAddr, 0, this.recipientAddr.length);
                    this.recipientScopeId = ((Inet6Address)address).getScopeId();
                } else {
                    NativeInetAddress.copyIpv4MappedIpv6Address(address.getAddress(), this.recipientAddr);
                    this.recipientScopeId = 0;
                }
                this.recipientAddrLen = this.recipientAddr.length;
                this.recipientPort = recipient.getPort();
            }
        }

        DatagramPacket newDatagramPacket(ByteBuf buffer, InetSocketAddress recipient) throws UnknownHostException {
            InetSocketAddress sender = NativeDatagramPacketArray.newAddress(this.senderAddr, this.senderAddrLen, this.senderPort, this.senderScopeId, NativeDatagramPacketArray.this.ipv4Bytes);
            if (this.recipientAddrLen != 0) {
                recipient = NativeDatagramPacketArray.newAddress(this.recipientAddr, this.recipientAddrLen, this.recipientPort, this.recipientScopeId, NativeDatagramPacketArray.this.ipv4Bytes);
            }
            buffer.writerIndex(this.count);
            if (this.segmentSize > 0) {
                return new SegmentedDatagramPacket(buffer, this.segmentSize, recipient, sender);
            }
            return new DatagramPacket(buffer, recipient, sender);
        }
    }

    private final class MyMessageProcessor
    implements ChannelOutboundBuffer.MessageProcessor {
        private boolean connected;
        private int maxMessagesPerWrite;

        private MyMessageProcessor() {
        }

        @Override
        public boolean processMessage(Object msg) {
            boolean added;
            if (msg instanceof DatagramPacket) {
                DatagramPacket packet = (DatagramPacket)msg;
                ByteBuf buf = (ByteBuf)packet.content();
                int segmentSize = 0;
                if (packet instanceof io.netty.channel.unix.SegmentedDatagramPacket) {
                    int seg = ((io.netty.channel.unix.SegmentedDatagramPacket)packet).segmentSize();
                    if (buf.readableBytes() > seg) {
                        segmentSize = seg;
                    }
                }
                added = NativeDatagramPacketArray.this.add0(buf, buf.readerIndex(), buf.readableBytes(), segmentSize, (InetSocketAddress)packet.recipient());
            } else if (msg instanceof ByteBuf && this.connected) {
                ByteBuf buf = (ByteBuf)msg;
                added = NativeDatagramPacketArray.this.add0(buf, buf.readerIndex(), buf.readableBytes(), 0, null);
            } else {
                added = false;
            }
            if (added) {
                --this.maxMessagesPerWrite;
                return this.maxMessagesPerWrite > 0;
            }
            return false;
        }
    }
}

