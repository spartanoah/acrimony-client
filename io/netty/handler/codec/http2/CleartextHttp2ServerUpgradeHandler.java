/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.util.internal.ObjectUtil;
import java.util.List;

public final class CleartextHttp2ServerUpgradeHandler
extends ByteToMessageDecoder {
    private static final ByteBuf CONNECTION_PREFACE = Unpooled.unreleasableBuffer(Http2CodecUtil.connectionPrefaceBuf());
    private final HttpServerCodec httpServerCodec;
    private final HttpServerUpgradeHandler httpServerUpgradeHandler;
    private final ChannelHandler http2ServerHandler;

    public CleartextHttp2ServerUpgradeHandler(HttpServerCodec httpServerCodec, HttpServerUpgradeHandler httpServerUpgradeHandler, ChannelHandler http2ServerHandler) {
        this.httpServerCodec = ObjectUtil.checkNotNull(httpServerCodec, "httpServerCodec");
        this.httpServerUpgradeHandler = ObjectUtil.checkNotNull(httpServerUpgradeHandler, "httpServerUpgradeHandler");
        this.http2ServerHandler = ObjectUtil.checkNotNull(http2ServerHandler, "http2ServerHandler");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addAfter(ctx.name(), null, this.httpServerUpgradeHandler).addAfter(ctx.name(), null, this.httpServerCodec);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int prefaceLength = CONNECTION_PREFACE.readableBytes();
        int bytesRead = Math.min(in.readableBytes(), prefaceLength);
        if (!ByteBufUtil.equals((ByteBuf)CONNECTION_PREFACE, (int)CONNECTION_PREFACE.readerIndex(), (ByteBuf)in, (int)in.readerIndex(), (int)bytesRead)) {
            ctx.pipeline().remove(this);
        } else if (bytesRead == prefaceLength) {
            ctx.pipeline().remove(this.httpServerCodec).remove(this.httpServerUpgradeHandler);
            ctx.pipeline().addAfter(ctx.name(), null, this.http2ServerHandler);
            ctx.pipeline().remove(this);
            ctx.fireUserEventTriggered(PriorKnowledgeUpgradeEvent.INSTANCE);
        }
    }

    public static final class PriorKnowledgeUpgradeEvent {
        private static final PriorKnowledgeUpgradeEvent INSTANCE = new PriorKnowledgeUpgradeEvent();

        private PriorKnowledgeUpgradeEvent() {
        }
    }
}

