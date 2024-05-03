/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.bungee.handlers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bungee.util.BungeePipelineUtil;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@ChannelHandler.Sharable
public class BungeeEncodeHandler
extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;
    private boolean handledCompression;

    public BungeeEncodeHandler(UserConnection info) {
        this.info = info;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            throw CancelEncoderException.generate(null);
        }
        if (!this.info.checkClientboundPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!this.info.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }
        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            boolean needsCompress = this.handleCompressionOrder(ctx, transformedBuf);
            this.info.transformClientbound(transformedBuf, CancelEncoderException::generate);
            if (needsCompress) {
                this.recompress(ctx, transformedBuf);
            }
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf buf) {
        boolean needsCompress = false;
        if (!this.handledCompression && ctx.pipeline().names().indexOf("compress") > ctx.pipeline().names().indexOf("via-encoder")) {
            ByteBuf decompressed = BungeePipelineUtil.decompress(ctx, buf);
            if (buf != decompressed) {
                try {
                    buf.clear().writeBytes(decompressed);
                } finally {
                    decompressed.release();
                }
            }
            ChannelHandler decoder = ctx.pipeline().get("via-decoder");
            ChannelHandler encoder = ctx.pipeline().get("via-encoder");
            ctx.pipeline().remove(decoder);
            ctx.pipeline().remove(encoder);
            ctx.pipeline().addAfter("decompress", "via-decoder", decoder);
            ctx.pipeline().addAfter("compress", "via-encoder", encoder);
            needsCompress = true;
            this.handledCompression = true;
        }
        return needsCompress;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void recompress(ChannelHandlerContext ctx, ByteBuf buf) {
        ByteBuf compressed = BungeePipelineUtil.compress(ctx, buf);
        try {
            buf.clear().writeBytes(compressed);
        } finally {
            compressed.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CancelCodecException) {
            return;
        }
        super.exceptionCaught(ctx, cause);
    }
}

