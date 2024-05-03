/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.util.concurrent.locks.Lock;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.SocketModalCloseable;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.util.Identifiable;
import org.apache.hc.core5.util.Timeout;

@Internal
public interface IOSession
extends ByteChannel,
SocketModalCloseable,
Identifiable {
    public IOEventHandler getHandler();

    public void upgrade(IOEventHandler var1);

    public Lock getLock();

    public void enqueue(Command var1, Command.Priority var2);

    public boolean hasCommands();

    public Command poll();

    public ByteChannel channel();

    public SocketAddress getRemoteAddress();

    public SocketAddress getLocalAddress();

    public int getEventMask();

    public void setEventMask(int var1);

    public void setEvent(int var1);

    public void clearEvent(int var1);

    @Override
    public void close();

    public Status getStatus();

    @Override
    public Timeout getSocketTimeout();

    @Override
    public void setSocketTimeout(Timeout var1);

    public long getLastReadTime();

    public long getLastWriteTime();

    public long getLastEventTime();

    public void updateReadTime();

    public void updateWriteTime();

    public static enum Status {
        ACTIVE,
        CLOSING,
        CLOSED;

    }
}

