/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.dns.DnsMessageUtil;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.util.List;

@ChannelHandler.Sharable
public class DatagramDnsResponseEncoder
extends MessageToMessageEncoder<AddressedEnvelope<DnsResponse, InetSocketAddress>> {
    private final DnsRecordEncoder recordEncoder;

    public DatagramDnsResponseEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    public DatagramDnsResponseEncoder(DnsRecordEncoder recordEncoder) {
        this.recordEncoder = ObjectUtil.checkNotNull(recordEncoder, "recordEncoder");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<DnsResponse, InetSocketAddress> in, List<Object> out) throws Exception {
        InetSocketAddress recipient = in.recipient();
        DnsResponse response = in.content();
        ByteBuf buf = this.allocateBuffer(ctx, in);
        DnsMessageUtil.encodeDnsResponse(this.recordEncoder, response, buf);
        out.add(new DatagramPacket(buf, recipient, null));
    }

    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, AddressedEnvelope<DnsResponse, InetSocketAddress> msg) throws Exception {
        return ctx.alloc().ioBuffer(1024);
    }
}

