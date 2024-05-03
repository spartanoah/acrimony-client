/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.concurrent.locks.Lock;
import org.apache.hc.client5.http.impl.Wire;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

class LoggingIOSession
implements IOSession {
    private final Logger log;
    private final Wire wireLog;
    private final String id;
    private final IOSession session;

    public LoggingIOSession(IOSession session, Logger log, Logger wireLog) {
        this.session = session;
        this.id = session.getId();
        this.log = log;
        this.wireLog = new Wire(wireLog, this.id);
    }

    @Override
    public String getId() {
        return this.session.getId();
    }

    @Override
    public Lock getLock() {
        return this.session.getLock();
    }

    @Override
    public boolean hasCommands() {
        return this.session.hasCommands();
    }

    @Override
    public Command poll() {
        return this.session.poll();
    }

    @Override
    public void enqueue(Command command, Command.Priority priority) {
        this.session.enqueue(command, priority);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} Enqueued {} with priority {}", new Object[]{this.session, command.getClass().getSimpleName(), priority});
        }
    }

    @Override
    public ByteChannel channel() {
        return this.session.channel();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.session.getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.session.getRemoteAddress();
    }

    @Override
    public int getEventMask() {
        return this.session.getEventMask();
    }

    private static String formatOps(int ops) {
        StringBuilder buffer = new StringBuilder(6);
        buffer.append('[');
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
        buffer.append(']');
        return buffer.toString();
    }

    @Override
    public void setEventMask(int ops) {
        this.session.setEventMask(ops);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: Event mask set {}", this.id, this.session, LoggingIOSession.formatOps(ops));
        }
    }

    @Override
    public void setEvent(int op) {
        this.session.setEvent(op);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: Event set {}", this.id, this.session, LoggingIOSession.formatOps(op));
        }
    }

    @Override
    public void clearEvent(int op) {
        this.session.clearEvent(op);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: Event cleared {}", this.id, this.session, LoggingIOSession.formatOps(op));
        }
    }

    @Override
    public boolean isOpen() {
        return this.session.isOpen();
    }

    @Override
    public void close() {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: Close", (Object)this.id, (Object)this.session);
        }
        this.session.close();
    }

    @Override
    public IOSession.Status getStatus() {
        return this.session.getStatus();
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: Close {}", new Object[]{this.id, this.session, closeMode});
        }
        this.session.close(closeMode);
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.session.getSocketTimeout();
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: Set timeout {}", this.id, this.session, timeout);
        }
        this.session.setSocketTimeout(timeout);
    }

    @Override
    public long getLastReadTime() {
        return this.session.getLastReadTime();
    }

    @Override
    public long getLastWriteTime() {
        return this.session.getLastWriteTime();
    }

    @Override
    public void updateReadTime() {
        this.session.updateReadTime();
    }

    @Override
    public void updateWriteTime() {
        this.session.updateWriteTime();
    }

    @Override
    public long getLastEventTime() {
        return this.session.getLastEventTime();
    }

    @Override
    public IOEventHandler getHandler() {
        return this.session.getHandler();
    }

    @Override
    public void upgrade(IOEventHandler handler) {
        this.session.upgrade(handler);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int bytesRead = this.session.channel().read(dst);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: {} bytes read", this.id, this.session, bytesRead);
        }
        if (bytesRead > 0 && this.wireLog.isEnabled()) {
            ByteBuffer b = dst.duplicate();
            int p = b.position();
            b.limit(p);
            b.position(p - bytesRead);
            this.wireLog.input(b);
        }
        return bytesRead;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int byteWritten = this.session.channel().write(src);
        if (this.log.isDebugEnabled()) {
            this.log.debug("{} {}: {} bytes written", this.id, this.session, byteWritten);
        }
        if (byteWritten > 0 && this.wireLog.isEnabled()) {
            ByteBuffer b = src.duplicate();
            int p = b.position();
            b.limit(p);
            b.position(p - byteWritten);
            this.wireLog.output(b);
        }
        return byteWritten;
    }

    public String toString() {
        return this.id + " " + this.session;
    }
}

