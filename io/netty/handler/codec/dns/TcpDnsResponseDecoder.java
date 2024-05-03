/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.dns.DefaultDnsResponse;
import io.netty.handler.codec.dns.DnsOpCode;
import io.netty.handler.codec.dns.DnsRecordDecoder;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsResponseDecoder;
import java.net.SocketAddress;

public final class TcpDnsResponseDecoder
extends LengthFieldBasedFrameDecoder {
    private final DnsResponseDecoder<SocketAddress> responseDecoder;

    public TcpDnsResponseDecoder() {
        this(DnsRecordDecoder.DEFAULT, 65536);
    }

    public TcpDnsResponseDecoder(DnsRecordDecoder recordDecoder, int maxFrameLength) {
        super(maxFrameLength, 0, 2, 0, 2);
        this.responseDecoder = new DnsResponseDecoder<SocketAddress>(recordDecoder){

            @Override
            protected DnsResponse newResponse(SocketAddress sender, SocketAddress recipient, int id, DnsOpCode opCode, DnsResponseCode responseCode) {
                return new DefaultDnsResponse(id, opCode, responseCode);
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf)super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        try {
            DnsResponse dnsResponse = this.responseDecoder.decode(ctx.channel().remoteAddress(), ctx.channel().localAddress(), frame.slice());
            return dnsResponse;
        } finally {
            frame.release();
        }
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.copy(index, length);
    }
}

