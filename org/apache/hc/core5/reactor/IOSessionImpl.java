/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

class IOSessionImpl
implements IOSession {
    private static final AtomicLong COUNT = new AtomicLong(0L);
    private final SelectionKey key;
    private final SocketChannel channel;
    private final Deque<Command> commandQueue;
    private final Lock lock;
    private final String id;
    private final AtomicReference<IOEventHandler> handlerRef;
    private final AtomicReference<IOSession.Status> status;
    private volatile Timeout socketTimeout;
    private volatile long lastReadTime;
    private volatile long lastWriteTime;
    private volatile long lastEventTime;

    public IOSessionImpl(String type, SelectionKey key, SocketChannel socketChannel) {
        long currentTimeMillis;
        this.key = Args.notNull(key, "Selection key");
        this.channel = Args.notNull(socketChannel, "Socket channel");
        this.commandQueue = new ConcurrentLinkedDeque<Command>();
        this.lock = new ReentrantLock();
        this.socketTimeout = Timeout.DISABLED;
        this.id = String.format(type + "-%08X", COUNT.getAndIncrement());
        this.handlerRef = new AtomicReference();
        this.status = new AtomicReference<IOSession.Status>(IOSession.Status.ACTIVE);
        this.lastReadTime = currentTimeMillis = System.currentTimeMillis();
        this.lastWriteTime = currentTimeMillis;
        this.lastEventTime = currentTimeMillis;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public IOEventHandler getHandler() {
        return this.handlerRef.get();
    }

    @Override
    public void upgrade(IOEventHandler handler) {
        this.handlerRef.set(handler);
    }

    @Override
    public Lock getLock() {
        return this.lock;
    }

    @Override
    public void enqueue(Command command, Command.Priority priority) {
        if (priority == Command.Priority.IMMEDIATE) {
            this.commandQueue.addFirst(command);
        } else {
            this.commandQueue.add(command);
        }
        this.setEvent(4);
    }

    @Override
    public boolean hasCommands() {
        return !this.commandQueue.isEmpty();
    }

    @Override
    public Command poll() {
        return this.commandQueue.poll();
    }

    @Override
    public ByteChannel channel() {
        return this.channel;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.channel.socket().getLocalSocketAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.channel.socket().getRemoteSocketAddress();
    }

    @Override
    public int getEventMask() {
        return this.key.interestOps();
    }

    @Override
    public void setEventMask(int newValue) {
        if (this.isStatusClosed()) {
            return;
        }
        this.key.interestOps(newValue);
        this.key.selector().wakeup();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEvent(int op) {
        if (this.isStatusClosed()) {
            return;
        }
        this.lock.lock();
        try {
            this.key.interestOps(this.key.interestOps() | op);
        } finally {
            this.lock.unlock();
        }
        this.key.selector().wakeup();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearEvent(int op) {
        if (this.isStatusClosed()) {
            return;
        }
        this.lock.lock();
        try {
            this.key.interestOps(this.key.interestOps() & ~op);
        } finally {
            this.lock.unlock();
        }
        this.key.selector().wakeup();
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.socketTimeout;
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        this.socketTimeout = Timeout.defaultsToDisabled(timeout);
        this.lastEventTime = System.currentTimeMillis();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return this.channel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return this.channel.write(src);
    }

    @Override
    public void updateReadTime() {
        this.lastEventTime = this.lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void updateWriteTime() {
        this.lastEventTime = this.lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public long getLastReadTime() {
        return this.lastReadTime;
    }

    @Override
    public long getLastWriteTime() {
        return this.lastWriteTime;
    }

    @Override
    public long getLastEventTime() {
        return this.lastEventTime;
    }

    @Override
    public IOSession.Status getStatus() {
        return this.status.get();
    }

    private boolean isStatusClosed() {
        return this.status.get() == IOSession.Status.CLOSED;
    }

    @Override
    public boolean isOpen() {
        return this.status.get() == IOSession.Status.ACTIVE && this.channel.isOpen();
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.status.compareAndSet(IOSession.Status.ACTIVE, IOSession.Status.CLOSED)) {
            if (closeMode == CloseMode.IMMEDIATE) {
                try {
                    this.channel.socket().setSoLinger(true, 0);
                } catch (SocketException socketException) {
                    // empty catch block
                }
            }
            this.key.cancel();
            this.key.attach(null);
            Closer.closeQuietly(this.key.channel());
            if (this.key.selector().isOpen()) {
                this.key.selector().wakeup();
            }
        }
    }

    private static void formatOps(StringBuilder buffer, int ops) {
        if ((ops & 1) > 0) {
            buffer.append('r');
        }
        if ((ops & 4) > 0) {
            buffer.append('w');
        }
        if ((ops & 0x10) > 0) {
            buffer.append('a');
        }
        if ((ops & 8) > 0) {
            buffer.append('c');
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.id).append("[");
        buffer.append(this.status);
        buffer.append("][");
        if (this.key.isValid()) {
            IOSessionImpl.formatOps(buffer, this.key.interestOps());
            buffer.append(":");
            IOSessionImpl.formatOps(buffer, this.key.readyOps());
        }
        buffer.append("]");
        return buffer.toString();
    }
}

