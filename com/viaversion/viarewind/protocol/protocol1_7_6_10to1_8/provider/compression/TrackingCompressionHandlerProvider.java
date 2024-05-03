/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.compression;

import com.viaversion.viarewind.api.netty.EmptyChannelHandler;
import com.viaversion.viarewind.api.netty.ForwardMessageToByteEncoder;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.CompressionHandlerProvider;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.compression.CompressionDecoder;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.compression.CompressionEncoder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class TrackingCompressionHandlerProvider
extends CompressionHandlerProvider {
    public static final String COMPRESS_HANDLER_NAME = "compress";
    public static final String DECOMPRESS_HANDLER_NAME = "decompress";

    @Override
    public void onHandleLoginCompressionPacket(UserConnection user, int threshold) {
        ChannelPipeline pipeline = user.getChannel().pipeline();
        if (user.isClientSide()) {
            pipeline.addBefore(Via.getManager().getInjector().getEncoderName(), COMPRESS_HANDLER_NAME, this.getEncoder(threshold));
            pipeline.addBefore(Via.getManager().getInjector().getDecoderName(), DECOMPRESS_HANDLER_NAME, this.getDecoder(threshold));
        } else {
            this.setCompressionEnabled(user, true);
        }
    }

    @Override
    public void onTransformPacket(UserConnection user) {
        if (this.isCompressionEnabled(user)) {
            ChannelPipeline pipeline = user.getChannel().pipeline();
            String compressor = null;
            String decompressor = null;
            if (pipeline.get(COMPRESS_HANDLER_NAME) != null) {
                compressor = COMPRESS_HANDLER_NAME;
                decompressor = DECOMPRESS_HANDLER_NAME;
            } else if (pipeline.get("compression-encoder") != null) {
                compressor = "compression-encoder";
                decompressor = "compression-decoder";
            }
            if (compressor == null) {
                throw new IllegalStateException("Couldn't remove compression for 1.7!");
            }
            pipeline.replace(decompressor, decompressor, (ChannelHandler)new EmptyChannelHandler());
            pipeline.replace(compressor, compressor, (ChannelHandler)new ForwardMessageToByteEncoder());
            this.setCompressionEnabled(user, false);
        }
    }

    @Override
    public ChannelHandler getEncoder(int threshold) {
        return new CompressionEncoder(threshold);
    }

    @Override
    public ChannelHandler getDecoder(int threshold) {
        return new CompressionDecoder(threshold);
    }
}

