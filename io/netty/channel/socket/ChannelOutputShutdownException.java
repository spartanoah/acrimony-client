/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.socket;

import java.io.IOException;

public final class ChannelOutputShutdownException
extends IOException {
    private static final long serialVersionUID = 6712549938359321378L;

    public ChannelOutputShutdownException(String msg) {
        super(msg);
    }

    public ChannelOutputShutdownException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

