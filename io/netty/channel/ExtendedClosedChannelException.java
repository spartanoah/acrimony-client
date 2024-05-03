/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import java.nio.channels.ClosedChannelException;

final class ExtendedClosedChannelException
extends ClosedChannelException {
    ExtendedClosedChannelException(Throwable cause) {
        if (cause != null) {
            this.initCause(cause);
        }
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}

