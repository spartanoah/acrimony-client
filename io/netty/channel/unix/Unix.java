/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.unix;

import io.netty.channel.unix.DatagramSocketAddress;
import io.netty.channel.unix.DomainDatagramSocketAddress;
import io.netty.channel.unix.Socket;
import io.netty.util.internal.ClassInitializerUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Unix {
    private static final AtomicBoolean registered = new AtomicBoolean();

    public static void registerInternal(Runnable registerTask) {
        if (registered.compareAndSet(false, true)) {
            registerTask.run();
            Socket.initialize();
        }
    }

    @Deprecated
    public static boolean isAvailable() {
        return false;
    }

    @Deprecated
    public static void ensureAvailability() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public static Throwable unavailabilityCause() {
        return new UnsupportedOperationException();
    }

    private Unix() {
    }

    static {
        ClassInitializerUtil.tryLoadClasses(Unix.class, OutOfMemoryError.class, RuntimeException.class, ClosedChannelException.class, IOException.class, PortUnreachableException.class, DatagramSocketAddress.class, DomainDatagramSocketAddress.class, InetSocketAddress.class);
    }
}

