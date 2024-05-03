/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQueryEncoder;
import io.netty.handler.codec.dns.DnsRecordEncoder;

@ChannelHandler.Sharable
public final class TcpDnsQueryEncoder
extends MessageToByteEncoder<DnsQuery> {
    private final DnsQueryEncoder encoder;

    public TcpDnsQueryEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    public TcpDnsQueryEncoder(DnsRecordEncoder recordEncoder) {
        this.encoder = new DnsQueryEncoder(recordEncoder);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DnsQuery msg, ByteBuf out) throws Exception {
        out.writerIndex(out.writerIndex() + 2);
        this.encoder.encode(msg, out);
        out.setShort(0, out.readableBytes() - 2);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, DnsQuery msg, boolean preferDirect) {
        if (preferDirect) {
            return ctx.alloc().ioBuffer(1024);
        }
        return ctx.alloc().heapBuffer(1024);
    }
}

