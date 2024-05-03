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
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQueryEncoder;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import java.net.InetSocketAddress;
import java.util.List;

@ChannelHandler.Sharable
public class DatagramDnsQueryEncoder
extends MessageToMessageEncoder<AddressedEnvelope<DnsQuery, InetSocketAddress>> {
    private final DnsQueryEncoder encoder;

    public DatagramDnsQueryEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    public DatagramDnsQueryEncoder(DnsRecordEncoder recordEncoder) {
        this.encoder = new DnsQueryEncoder(recordEncoder);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<DnsQuery, InetSocketAddress> in, List<Object> out) throws Exception {
        InetSocketAddress recipient = in.recipient();
        DnsQuery query = in.content();
        ByteBuf buf = this.allocateBuffer(ctx, in);
        boolean success = false;
        try {
            this.encoder.encode(query, buf);
            success = true;
        } finally {
            if (!success) {
                buf.release();
            }
        }
        out.add(new DatagramPacket(buf, recipient, null));
    }

    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, AddressedEnvelope<DnsQuery, InetSocketAddress> msg) throws Exception {
        return ctx.alloc().ioBuffer(1024);
    }
}

