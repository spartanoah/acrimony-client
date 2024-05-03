/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.channel.Channel;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.kqueue.AbstractKQueueChannel;
import io.netty.channel.kqueue.BsdSocket;
import java.io.IOException;

abstract class AbstractKQueueDatagramChannel
extends AbstractKQueueChannel {
    private static final ChannelMetadata METADATA = new ChannelMetadata(true);

    AbstractKQueueDatagramChannel(Channel parent, BsdSocket fd, boolean active) {
        super(parent, fd, active);
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    protected abstract boolean doWriteMessage(Object var1) throws Exception;

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        Object msg;
        int maxMessagesPerWrite = this.maxMessagesPerWrite();
        while (maxMessagesPerWrite > 0 && (msg = in.current()) != null) {
            try {
                boolean done = false;
                for (int i = this.config().getWriteSpinCount(); i > 0; --i) {
                    if (!this.doWriteMessage(msg)) continue;
                    done = true;
                    break;
                }
                if (!done) break;
                in.remove();
                --maxMessagesPerWrite;
            } catch (IOException e) {
                --maxMessagesPerWrite;
                in.remove(e);
            }
        }
        this.writeFilter(!in.isEmpty());
    }
}

