/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

public final class ChannelMetadata {
    private final boolean hasDisconnect;

    public ChannelMetadata(boolean hasDisconnect) {
        this.hasDisconnect = hasDisconnect;
    }

    public boolean hasDisconnect() {
        return this.hasDisconnect;
    }
}

