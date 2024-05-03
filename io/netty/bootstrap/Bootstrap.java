/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.bootstrap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;

public final class Bootstrap
extends AbstractBootstrap<Bootstrap, Channel> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Bootstrap.class);
    private volatile SocketAddress remoteAddress;
    private static final Joiner DOT_JOINER = Joiner.on('.');
    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    @VisibleForTesting
    static final Set<String> BLOCKED_SERVERS = Sets.newHashSet();

    public Bootstrap() {
    }

    private Bootstrap(Bootstrap bootstrap) {
        super(bootstrap);
        this.remoteAddress = bootstrap.remoteAddress;
    }

    public Bootstrap remoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public Bootstrap remoteAddress(String inetHost, int inetPort) {
        this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
        return this;
    }

    public Bootstrap remoteAddress(InetAddress inetHost, int inetPort) {
        this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
        return this;
    }

    public ChannelFuture connect() {
        this.validate();
        SocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            throw new IllegalStateException("remoteAddress not set");
        }
        return this.doConnect(remoteAddress, this.localAddress());
    }

    public ChannelFuture connect(String inetHost, int inetPort) {
        return this.connect(new InetSocketAddress(inetHost, inetPort));
    }

    public ChannelFuture connect(InetAddress inetHost, int inetPort) {
        return this.connect(new InetSocketAddress(inetHost, inetPort));
    }

    public ChannelFuture connect(SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        this.validate();
        return this.doConnect(remoteAddress, this.localAddress());
    }

    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        this.validate();
        return this.doConnect(remoteAddress, localAddress);
    }

    private ChannelFuture doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        ChannelFuture future = this.checkAddress(remoteAddress);
        if (future != null) {
            return future;
        }
        final ChannelFuture regFuture = this.initAndRegister();
        final Channel channel = regFuture.channel();
        if (regFuture.cause() != null) {
            return regFuture;
        }
        final ChannelPromise promise = channel.newPromise();
        if (regFuture.isDone()) {
            Bootstrap.doConnect0(regFuture, channel, remoteAddress, localAddress, promise);
        } else {
            regFuture.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Bootstrap.doConnect0(regFuture, channel, remoteAddress, localAddress, promise);
                }
            });
        }
        return promise;
    }

    private static void doConnect0(final ChannelFuture regFuture, final Channel channel, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        channel.eventLoop().execute(new Runnable(){

            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    if (localAddress == null) {
                        channel.connect(remoteAddress, promise);
                    } else {
                        channel.connect(remoteAddress, localAddress, promise);
                    }
                    promise.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    promise.setFailure(regFuture.cause());
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void init(Channel channel) throws Exception {
        Map<ChannelOption<?>, Object> options;
        ChannelPipeline p = channel.pipeline();
        p.addLast(this.handler());
        Map<ChannelOption<?>, Object> map = options = this.options();
        synchronized (map) {
            for (Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
                try {
                    if (channel.config().setOption(e.getKey(), e.getValue())) continue;
                    logger.warn("Unknown channel option: " + e);
                } catch (Throwable t) {
                    logger.warn("Failed to set a channel option: " + channel, t);
                }
            }
        }
        Map<AttributeKey<?>, Object> attrs = this.attrs();
        Map<AttributeKey<?>, Object> map2 = attrs;
        synchronized (map2) {
            for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet()) {
                channel.attr(e.getKey()).set(e.getValue());
            }
        }
    }

    @Override
    public Bootstrap validate() {
        super.validate();
        if (this.handler() == null) {
            throw new IllegalStateException("handler not set");
        }
        return this;
    }

    @Override
    public Bootstrap clone() {
        return new Bootstrap(this);
    }

    @Override
    public String toString() {
        if (this.remoteAddress == null) {
            return super.toString();
        }
        StringBuilder buf = new StringBuilder(super.toString());
        buf.setLength(buf.length() - 1);
        buf.append(", remoteAddress: ");
        buf.append(this.remoteAddress);
        buf.append(')');
        return buf.toString();
    }

    @Nullable
    @VisibleForTesting
    ChannelFuture checkAddress(SocketAddress remoteAddress) {
        InetAddress address;
        if (remoteAddress instanceof InetSocketAddress && (this.isBlockedServer((address = ((InetSocketAddress)remoteAddress).getAddress()).getHostAddress()) || this.isBlockedServer(address.getHostName()))) {
            Object channel = this.channelFactory().newChannel();
            channel.unsafe().closeForcibly();
            SocketException cause = new SocketException("Network is unreachable");
            cause.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
            return new DefaultChannelPromise((Channel)channel, GlobalEventExecutor.INSTANCE).setFailure(cause);
        }
        return null;
    }

    public boolean isBlockedServer(String server) {
        boolean isIp;
        if (server == null || server.isEmpty()) {
            return false;
        }
        while (server.charAt(server.length() - 1) == '.') {
            server = server.substring(0, server.length() - 1);
        }
        if (this.isBlockedServerHostName(server)) {
            return true;
        }
        ArrayList<String> strings = Lists.newArrayList(DOT_SPLITTER.split(server));
        boolean bl = isIp = strings.size() == 4;
        if (isIp) {
            for (String string : strings) {
                try {
                    int part = Integer.parseInt(string);
                    if (part >= 0 && part <= 255) {
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                    // empty catch block
                }
                isIp = false;
                break;
            }
        }
        if (!isIp && this.isBlockedServerHostName("*." + server)) {
            return true;
        }
        while (strings.size() > 1) {
            strings.remove(isIp ? strings.size() - 1 : 0);
            String starredPart = isIp ? DOT_JOINER.join(strings) + ".*" : "*." + DOT_JOINER.join(strings);
            if (!this.isBlockedServerHostName(starredPart)) continue;
            return true;
        }
        return false;
    }

    private boolean isBlockedServerHostName(String server) {
        return BLOCKED_SERVERS.contains(Hashing.sha1().hashBytes(server.toLowerCase().getBytes(Charset.forName("ISO-8859-1"))).toString());
    }

    static {
        try {
            BLOCKED_SERVERS.addAll(IOUtils.readLines(new URL("https://sessionserver.mojang.com/blockedservers").openConnection().getInputStream()));
        } catch (IOException iOException) {
            // empty catch block
        }
    }
}

