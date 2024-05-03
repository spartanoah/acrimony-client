/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.velocity.handlers;

import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import com.viaversion.viaversion.velocity.handlers.VelocityDecodeHandler;
import com.viaversion.viaversion.velocity.handlers.VelocityEncodeHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import java.lang.reflect.Method;

public class VelocityChannelInitializer
extends ChannelInitializer<Channel> {
    public static final String MINECRAFT_ENCODER = "minecraft-encoder";
    public static final String MINECRAFT_DECODER = "minecraft-decoder";
    public static final String VIA_ENCODER = "via-encoder";
    public static final String VIA_DECODER = "via-decoder";
    public static final Object COMPRESSION_ENABLED_EVENT;
    private static final Method INIT_CHANNEL;
    private final ChannelInitializer<?> original;
    private final boolean clientSide;

    public VelocityChannelInitializer(ChannelInitializer<?> original, boolean clientSide) {
        this.original = original;
        this.clientSide = clientSide;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        INIT_CHANNEL.invoke(this.original, channel);
        UserConnectionImpl user = new UserConnectionImpl(channel, this.clientSide);
        new ProtocolPipelineImpl(user);
        channel.pipeline().addBefore(MINECRAFT_ENCODER, VIA_ENCODER, new VelocityEncodeHandler(user));
        channel.pipeline().addBefore(MINECRAFT_DECODER, VIA_DECODER, new VelocityDecodeHandler(user));
    }

    static {
        try {
            INIT_CHANNEL = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL.setAccessible(true);
            Class<?> eventClass = Class.forName("com.velocitypowered.proxy.protocol.VelocityConnectionEvent");
            COMPRESSION_ENABLED_EVENT = eventClass.getDeclaredField("COMPRESSION_ENABLED").get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

