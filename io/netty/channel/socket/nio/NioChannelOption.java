/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.socket.nio;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.SuppressJava6Requirement;
import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.Channel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Set;

@SuppressJava6Requirement(reason="Usage explicit by the user")
public final class NioChannelOption<T>
extends ChannelOption<T> {
    private final SocketOption<T> option;

    private NioChannelOption(SocketOption<T> option) {
        super(option.name());
        this.option = option;
    }

    public static <T> ChannelOption<T> of(SocketOption<T> option) {
        return new NioChannelOption<T>(option);
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    static <T> boolean setOption(Channel jdkChannel, NioChannelOption<T> option, T value) {
        NetworkChannel channel = (NetworkChannel)jdkChannel;
        if (!channel.supportedOptions().contains(option.option)) {
            return false;
        }
        if (channel instanceof ServerSocketChannel && option.option == StandardSocketOptions.IP_TOS) {
            return false;
        }
        try {
            channel.setOption(option.option, value);
            return true;
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    static <T> T getOption(Channel jdkChannel, NioChannelOption<T> option) {
        NetworkChannel channel = (NetworkChannel)jdkChannel;
        if (!channel.supportedOptions().contains(option.option)) {
            return null;
        }
        if (channel instanceof ServerSocketChannel && option.option == StandardSocketOptions.IP_TOS) {
            return null;
        }
        try {
            return channel.getOption(option.option);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    static ChannelOption[] getOptions(Channel jdkChannel) {
        NetworkChannel channel = (NetworkChannel)jdkChannel;
        Set<SocketOption<?>> supportedOpts = channel.supportedOptions();
        if (channel instanceof ServerSocketChannel) {
            ArrayList extraOpts = new ArrayList(supportedOpts.size());
            for (SocketOption<?> opt : supportedOpts) {
                if (opt == StandardSocketOptions.IP_TOS) continue;
                extraOpts.add(new NioChannelOption(opt));
            }
            return extraOpts.toArray(new ChannelOption[0]);
        }
        ChannelOption[] extraOpts = new ChannelOption[supportedOpts.size()];
        int i = 0;
        for (SocketOption<?> opt : supportedOpts) {
            extraOpts[i++] = new NioChannelOption(opt);
        }
        return extraOpts;
    }
}

