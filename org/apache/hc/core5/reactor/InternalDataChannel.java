/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import javax.net.ssl.SSLContext;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.reactor.InternalChannel;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLIOSession;
import org.apache.hc.core5.reactor.ssl.SSLMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.Timeout;

final class InternalDataChannel
extends InternalChannel
implements ProtocolIOSession {
    private final IOSession ioSession;
    private final NamedEndpoint initialEndpoint;
    private final IOSessionListener sessionListener;
    private final AtomicReference<SSLIOSession> tlsSessionRef;
    private final Queue<InternalDataChannel> closedSessions;
    private final AtomicBoolean connected;
    private final AtomicBoolean closed;

    InternalDataChannel(IOSession ioSession, NamedEndpoint initialEndpoint, IOSessionListener sessionListener, Queue<InternalDataChannel> closedSessions) {
        this.ioSession = ioSession;
        this.initialEndpoint = initialEndpoint;
        this.closedSessions = closedSessions;
        this.sessionListener = sessionListener;
        this.tlsSessionRef = new AtomicReference<Object>(null);
        this.connected = new AtomicBoolean(false);
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public String getId() {
        return this.ioSession.getId();
    }

    @Override
    public NamedEndpoint getInitialEndpoint() {
        return this.initialEndpoint;
    }

    @Override
    public IOEventHandler getHandler() {
        return this.ioSession.getHandler();
    }

    @Override
    public void upgrade(IOEventHandler handler) {
        this.ioSession.upgrade(handler);
    }

    private IOEventHandler ensureHandler(IOSession session) {
        IOEventHandler handler = session.getHandler();
        Asserts.notNull(handler, "IO event handler");
        return handler;
    }

    @Override
    void onIOEvent(int readyOps) throws IOException {
        IOEventHandler handler;
        IOSession currentSession;
        SSLIOSession tlsSession = this.tlsSessionRef.get();
        IOSession iOSession = currentSession = tlsSession != null ? tlsSession : this.ioSession;
        if ((readyOps & 8) != 0) {
            currentSession.clearEvent(8);
            if (tlsSession == null && this.connected.compareAndSet(false, true)) {
                if (this.sessionListener != null) {
                    this.sessionListener.connected(this);
                }
                handler = this.ensureHandler(currentSession);
                handler.connected(this);
            }
        }
        if ((readyOps & 1) != 0) {
            currentSession.updateReadTime();
            if (this.sessionListener != null) {
                this.sessionListener.inputReady(this);
            }
            handler = this.ensureHandler(currentSession);
            handler.inputReady(this, null);
        }
        if ((readyOps & 4) != 0 || (this.ioSession.getEventMask() & 4) != 0) {
            currentSession.updateWriteTime();
            if (this.sessionListener != null) {
                this.sessionListener.outputReady(this);
            }
            handler = this.ensureHandler(currentSession);
            handler.outputReady(this);
        }
    }

    @Override
    Timeout getTimeout() {
        return this.ioSession.getSocketTimeout();
    }

    @Override
    void onTimeout(Timeout timeout) throws IOException {
        SSLIOSession tlsSession;
        if (this.sessionListener != null) {
            this.sessionListener.timeout(this);
        }
        IOSession currentSession = (tlsSession = this.tlsSessionRef.get()) != null ? tlsSession : this.ioSession;
        IOEventHandler handler = this.ensureHandler(currentSession);
        handler.timeout(this, timeout);
    }

    @Override
    void onException(Exception cause) {
        SSLIOSession tlsSession;
        IOSession currentSession;
        IOEventHandler handler;
        if (this.sessionListener != null) {
            this.sessionListener.exception(this, cause);
        }
        if ((handler = (currentSession = (tlsSession = this.tlsSessionRef.get()) != null ? tlsSession : this.ioSession).getHandler()) != null) {
            handler.exception(this, cause);
        }
    }

    void disconnected() {
        SSLIOSession tlsSession;
        IOSession currentSession;
        IOEventHandler handler;
        if (this.sessionListener != null) {
            this.sessionListener.disconnected(this);
        }
        if ((handler = (currentSession = (tlsSession = this.tlsSessionRef.get()) != null ? tlsSession : this.ioSession).getHandler()) != null) {
            handler.disconnected(this);
        }
    }

    @Override
    public void startTls(SSLContext sslContext, NamedEndpoint endpoint, SSLBufferMode sslBufferMode, SSLSessionInitializer initializer, SSLSessionVerifier verifier, Timeout handshakeTimeout) {
        if (this.tlsSessionRef.compareAndSet(null, new SSLIOSession(endpoint != null ? endpoint : this.initialEndpoint, this.ioSession, this.initialEndpoint != null ? SSLMode.CLIENT : SSLMode.SERVER, sslContext, sslBufferMode, initializer, verifier, new Callback<SSLIOSession>(){

            @Override
            public void execute(SSLIOSession sslSession) {
                if (InternalDataChannel.this.connected.compareAndSet(false, true)) {
                    IOEventHandler handler = InternalDataChannel.this.ensureHandler(InternalDataChannel.this.ioSession);
                    try {
                        if (InternalDataChannel.this.sessionListener != null) {
                            InternalDataChannel.this.sessionListener.connected(InternalDataChannel.this);
                        }
                        handler.connected(InternalDataChannel.this);
                    } catch (Exception ex) {
                        if (InternalDataChannel.this.sessionListener != null) {
                            InternalDataChannel.this.sessionListener.exception(InternalDataChannel.this, ex);
                        }
                        handler.exception(InternalDataChannel.this, ex);
                    }
                }
            }
        }, new Callback<SSLIOSession>(){

            @Override
            public void execute(SSLIOSession sslSession) {
                if (InternalDataChannel.this.closed.compareAndSet(false, true)) {
                    InternalDataChannel.this.closedSessions.add(InternalDataChannel.this);
                }
            }
        }, handshakeTimeout))) {
            if (this.sessionListener != null) {
                this.sessionListener.startTls(this);
            }
        } else {
            throw new IllegalStateException("TLS already activated");
        }
    }

    @Override
    public TlsDetails getTlsDetails() {
        SSLIOSession sslIoSession = this.tlsSessionRef.get();
        return sslIoSession != null ? sslIoSession.getTlsDetails() : null;
    }

    @Override
    public Lock getLock() {
        return this.ioSession.getLock();
    }

    private IOSession getSessionImpl() {
        SSLIOSession tlsSession = this.tlsSessionRef.get();
        return tlsSession != null ? tlsSession : this.ioSession;
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close(CloseMode closeMode) {
        if (closeMode == CloseMode.IMMEDIATE) {
            this.closed.set(true);
            this.getSessionImpl().close(closeMode);
        } else if (this.closed.compareAndSet(false, true)) {
            try {
                this.getSessionImpl().close(closeMode);
            } finally {
                this.closedSessions.add(this);
            }
        }
    }

    @Override
    public IOSession.Status getStatus() {
        return this.getSessionImpl().getStatus();
    }

    @Override
    public boolean isOpen() {
        return this.getSessionImpl().isOpen();
    }

    @Override
    public void enqueue(Command command, Command.Priority priority) {
        this.getSessionImpl().enqueue(command, priority);
    }

    @Override
    public boolean hasCommands() {
        return this.getSessionImpl().hasCommands();
    }

    @Override
    public Command poll() {
        return this.getSessionImpl().poll();
    }

    @Override
    public ByteChannel channel() {
        return this.getSessionImpl().channel();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return this.ioSession.getRemoteAddress();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.ioSession.getLocalAddress();
    }

    @Override
    public int getEventMask() {
        return this.getSessionImpl().getEventMask();
    }

    @Override
    public void setEventMask(int ops) {
        this.getSessionImpl().setEventMask(ops);
    }

    @Override
    public void setEvent(int op) {
        this.getSessionImpl().setEvent(op);
    }

    @Override
    public void clearEvent(int op) {
        this.getSessionImpl().clearEvent(op);
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.ioSession.getSocketTimeout();
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        this.ioSession.setSocketTimeout(timeout);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return this.getSessionImpl().read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return this.getSessionImpl().write(src);
    }

    @Override
    public void updateReadTime() {
        this.ioSession.updateReadTime();
    }

    @Override
    public void updateWriteTime() {
        this.ioSession.updateWriteTime();
    }

    @Override
    public long getLastReadTime() {
        return this.ioSession.getLastReadTime();
    }

    @Override
    public long getLastWriteTime() {
        return this.ioSession.getLastWriteTime();
    }

    @Override
    public long getLastEventTime() {
        return this.ioSession.getLastEventTime();
    }

    public String toString() {
        SSLIOSession tlsSession = this.tlsSessionRef.get();
        if (tlsSession != null) {
            return tlsSession.toString();
        }
        return this.ioSession.toString();
    }
}

