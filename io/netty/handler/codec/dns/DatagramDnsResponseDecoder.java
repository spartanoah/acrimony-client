/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DnsOpCode;
import io.netty.handler.codec.dns.DnsRecordDecoder;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsResponseDecoder;
import java.net.InetSocketAddress;
import java.util.List;

@ChannelHandler.Sharable
public class DatagramDnsResponseDecoder
extends MessageToMessageDecoder<DatagramPacket> {
    private final DnsResponseDecoder<InetSocketAddress> responseDecoder;

    public DatagramDnsResponseDecoder() {
        this(DnsRecordDecoder.DEFAULT);
    }

    public DatagramDnsResponseDecoder(DnsRecordDecoder recordDecoder) {
        this.responseDecoder = new DnsResponseDecoder<InetSocketAddress>(recordDecoder){

            @Override
            protected DnsResponse newResponse(InetSocketAddress sender, InetSocketAddress recipient, int id, DnsOpCode opCode, DnsResponseCode responseCode) {
                return new DatagramDnsResponse(sender, recipient, id, opCode, responseCode);
            }
        };
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
        try {
            out.add(this.decodeResponse(ctx, packet));
        } catch (IndexOutOfBoundsException e) {
            throw new CorruptedFrameException("Unable to decode response", e);
        }
    }

    protected DnsResponse decodeResponse(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        return this.responseDecoder.decode((InetSocketAddress)packet.sender(), (InetSocketAddress)packet.recipient(), (ByteBuf)packet.content());
    }
}

