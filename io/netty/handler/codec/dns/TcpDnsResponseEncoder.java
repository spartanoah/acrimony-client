/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.dns.DnsMessageUtil;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.internal.ObjectUtil;
import java.util.List;

@ChannelHandler.Sharable
public final class TcpDnsResponseEncoder
extends MessageToMessageEncoder<DnsResponse> {
    private final DnsRecordEncoder encoder;

    public TcpDnsResponseEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    public TcpDnsResponseEncoder(DnsRecordEncoder encoder) {
        this.encoder = ObjectUtil.checkNotNull(encoder, "encoder");
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DnsResponse response, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().ioBuffer(1024);
        buf.writerIndex(buf.writerIndex() + 2);
        DnsMessageUtil.encodeDnsResponse(this.encoder, response, buf);
        buf.setShort(0, buf.readableBytes() - 2);
        out.add(buf);
    }
}

