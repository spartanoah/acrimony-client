/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor.ssl;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLManagedBuffer;
import org.apache.hc.core5.reactor.ssl.SSLMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Asserts;
import org.apache.hc.core5.util.ReflectionUtils;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
@Internal
public class SSLIOSession
implements IOSession {
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private final NamedEndpoint targetEndpoint;
    private final IOSession session;
    private final SSLEngine sslEngine;
    private final SSLManagedBuffer inEncrypted;
    private final SSLManagedBuffer outEncrypted;
    private final SSLManagedBuffer inPlain;
    private final SSLSessionInitializer initializer;
    private final SSLSessionVerifier verifier;
    private final Callback<SSLIOSession> connectedCallback;
    private final Callback<SSLIOSession> disconnectedCallback;
    private final Timeout connectTimeout;
    private final SSLMode sslMode;
    private final AtomicInteger outboundClosedCount;
    private int appEventMask;
    private volatile boolean endOfStream;
    private volatile IOSession.Status status = IOSession.Status.ACTIVE;
    private volatile boolean initialized;
    private volatile Timeout socketTimeout;
    private TlsDetails tlsDetails;

    public SSLIOSession(NamedEndpoint targetEndpoint, IOSession session, SSLMode sslMode, SSLContext sslContext, SSLBufferMode sslBufferMode, SSLSessionInitializer initializer, SSLSessionVerifier verifier, Callback<SSLIOSession> connectedCallback, Callback<SSLIOSession> disconnectedCallback, Timeout connectTimeout) {
        Args.notNull(session, "IO session");
        Args.notNull(sslContext, "SSL context");
        this.targetEndpoint = targetEndpoint;
        this.session = session;
        this.sslMode = sslMode;
        this.initializer = initializer;
        this.verifier = verifier;
        this.connectedCallback = connectedCallback;
        this.disconnectedCallback = disconnectedCallback;
        this.appEventMask = session.getEventMask();
        this.sslEngine = this.sslMode == SSLMode.CLIENT && targetEndpoint != null ? sslContext.createSSLEngine(targetEndpoint.getHostName(), targetEndpoint.getPort()) : sslContext.createSSLEngine();
        SSLSession sslSession = this.sslEngine.getSession();
        int netBufferSize = sslSession.getPacketBufferSize();
        this.inEncrypted = SSLManagedBuffer.create(sslBufferMode, netBufferSize);
        this.outEncrypted = SSLManagedBuffer.create(sslBufferMode, netBufferSize);
        int appBufferSize = sslSession.getApplicationBufferSize();
        this.inPlain = SSLManagedBuffer.create(sslBufferMode, appBufferSize);
        this.outboundClosedCount = new AtomicInteger(0);
        this.connectTimeout = connectTimeout;
    }

    private IOEventHandler ensureHandler() {
        IOEventHandler handler = this.session.getHandler();
        Asserts.notNull(handler, "IO event handler");
        return handler;
    }

    @Override
    public IOEventHandler getHandler() {
        return new IOEventHandler(){

            @Override
            public void connected(IOSession protocolSession) throws IOException {
                if (!SSLIOSession.this.initialized) {
                    SSLIOSession.this.initialize();
                }
            }

            @Override
            public void inputReady(IOSession protocolSession, ByteBuffer src) throws IOException {
                if (!SSLIOSession.this.initialized) {
                    SSLIOSession.this.initialize();
                }
                SSLIOSession.this.receiveEncryptedData();
                SSLIOSession.this.doHandshake();
                SSLIOSession.this.decryptData();
                SSLIOSession.this.updateEventMask();
            }

            @Override
            public void outputReady(IOSession protocolSession) throws IOException {
                if (!SSLIOSession.this.initialized) {
                    SSLIOSession.this.initialize();
                }
                SSLIOSession.this.encryptData();
                SSLIOSession.this.sendEncryptedData();
                SSLIOSession.this.doHandshake();
                SSLIOSession.this.updateEventMask();
            }

            @Override
            public void timeout(IOSession protocolSession, Timeout timeout) throws IOException {
                if (SSLIOSession.this.sslEngine.isInboundDone() && !SSLIOSession.this.sslEngine.isInboundDone()) {
                    SSLIOSession.this.close(CloseMode.IMMEDIATE);
                }
                SSLIOSession.this.ensureHandler().timeout(SSLIOSession.this, timeout);
            }

            @Override
            public void exception(IOSession protocolSession, Exception cause) {
                IOEventHandler handler = SSLIOSession.this.session.getHandler();
                if (handler != null) {
                    handler.exception(SSLIOSession.this, cause);
                }
            }

            @Override
            public void disconnected(IOSession protocolSession) {
                IOEventHandler handler = SSLIOSession.this.session.getHandler();
                if (handler != null) {
                    handler.disconnected(SSLIOSession.this);
                }
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void initialize() throws SSLException {
        Asserts.check(!this.initialized, "SSL I/O session already initialized");
        this.socketTimeout = this.session.getSocketTimeout();
        if (this.connectTimeout != null) {
            this.session.setSocketTimeout(this.connectTimeout);
        }
        this.session.getLock().lock();
        try {
            if (this.status.compareTo(IOSession.Status.CLOSING) >= 0) {
                return;
            }
            switch (this.sslMode) {
                case CLIENT: {
                    this.sslEngine.setUseClientMode(true);
                    break;
                }
                case SERVER: {
                    this.sslEngine.setUseClientMode(false);
                }
            }
            if (this.initializer != null) {
                this.initializer.initialize(this.targetEndpoint, this.sslEngine);
            }
            this.initialized = true;
            this.sslEngine.beginHandshake();
            this.inEncrypted.release();
            this.outEncrypted.release();
            this.doHandshake();
        } finally {
            this.session.getLock().unlock();
        }
    }

    private SSLException convert(RuntimeException ex) {
        Throwable cause = ex.getCause();
        if (cause == null) {
            cause = ex;
        }
        return new SSLException(cause);
    }

    private SSLEngineResult doWrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        try {
            return this.sslEngine.wrap(src, dst);
        } catch (RuntimeException ex) {
            throw this.convert(ex);
        }
    }

    private SSLEngineResult doUnwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        try {
            return this.sslEngine.unwrap(src, dst);
        } catch (RuntimeException ex) {
            throw this.convert(ex);
        }
    }

    private void doRunTask() throws SSLException {
        try {
            Runnable r = this.sslEngine.getDelegatedTask();
            if (r != null) {
                r.run();
            }
        } catch (RuntimeException ex) {
            throw this.convert(ex);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doHandshake() throws SSLException {
        boolean handshaking = true;
        SSLEngineResult result = null;
        block15: while (handshaking) {
            SSLEngineResult.HandshakeStatus handshakeStatus = this.sslEngine.getHandshakeStatus();
            if (handshakeStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && this.outboundClosedCount.get() > 0) {
                handshakeStatus = SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
            switch (handshakeStatus) {
                case NEED_WRAP: {
                    this.session.getLock().lock();
                    try {
                        ByteBuffer outEncryptedBuf = this.outEncrypted.acquire();
                        result = this.doWrap(EMPTY_BUFFER, outEncryptedBuf);
                        if (result.getStatus() == SSLEngineResult.Status.OK && result.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_WRAP) continue block15;
                        handshaking = false;
                        continue block15;
                    } finally {
                        this.session.getLock().unlock();
                        continue block15;
                    }
                }
                case NEED_UNWRAP: {
                    ByteBuffer inEncryptedBuf = this.inEncrypted.acquire();
                    ByteBuffer inPlainBuf = this.inPlain.acquire();
                    inEncryptedBuf.flip();
                    try {
                        result = this.doUnwrap(inEncryptedBuf, inPlainBuf);
                    } finally {
                        inEncryptedBuf.compact();
                    }
                    try {
                        if (!inEncryptedBuf.hasRemaining() && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                            throw new SSLException("Input buffer is full");
                        }
                    } finally {
                        if (inEncryptedBuf.position() == 0) {
                            this.inEncrypted.release();
                        }
                    }
                    if (this.status.compareTo(IOSession.Status.CLOSING) >= 0) {
                        this.inPlain.release();
                    }
                    if (result.getStatus() == SSLEngineResult.Status.OK) break;
                    handshaking = false;
                    break;
                }
                case NEED_TASK: {
                    this.doRunTask();
                    break;
                }
                case NOT_HANDSHAKING: {
                    handshaking = false;
                }
            }
        }
        if (result != null && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            this.session.setSocketTimeout(this.socketTimeout);
            if (this.verifier != null) {
                this.tlsDetails = this.verifier.verify(this.targetEndpoint, this.sslEngine);
            }
            if (this.tlsDetails == null) {
                SSLSession sslSession = this.sslEngine.getSession();
                String applicationProtocol = ReflectionUtils.callGetter(this.sslEngine, "ApplicationProtocol", String.class);
                this.tlsDetails = new TlsDetails(sslSession, applicationProtocol);
            }
            if (this.connectedCallback != null) {
                this.connectedCallback.execute(this);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateEventMask() {
        this.session.getLock().lock();
        try {
            int oldMask;
            if (this.status == IOSession.Status.ACTIVE && (this.endOfStream || this.sslEngine.isInboundDone())) {
                this.status = IOSession.Status.CLOSING;
            }
            if (this.status == IOSession.Status.CLOSING && !this.outEncrypted.hasData()) {
                this.sslEngine.closeOutbound();
                this.outboundClosedCount.incrementAndGet();
            }
            if (this.status == IOSession.Status.CLOSING && this.sslEngine.isOutboundDone() && (this.endOfStream || this.sslEngine.isInboundDone())) {
                this.status = IOSession.Status.CLOSED;
            }
            if (this.status.compareTo(IOSession.Status.CLOSING) <= 0 && this.endOfStream && this.sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                this.status = IOSession.Status.CLOSED;
            }
            if (this.status == IOSession.Status.CLOSED) {
                this.session.close();
                if (this.disconnectedCallback != null) {
                    this.disconnectedCallback.execute(this);
                }
                return;
            }
            int newMask = oldMask = this.session.getEventMask();
            switch (this.sslEngine.getHandshakeStatus()) {
                case NEED_WRAP: {
                    newMask = 5;
                    break;
                }
                case NEED_UNWRAP: {
                    newMask = 1;
                    break;
                }
                case NOT_HANDSHAKING: {
                    newMask = this.appEventMask;
                    break;
                }
                case NEED_TASK: {
                    break;
                }
            }
            if (this.endOfStream && !this.inPlain.hasData()) {
                newMask &= 0xFFFFFFFE;
            }
            if (this.outEncrypted.hasData()) {
                newMask |= 4;
            }
            if (oldMask != newMask) {
                this.session.setEventMask(newMask);
            }
        } finally {
            this.session.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int sendEncryptedData() throws IOException {
        this.session.getLock().lock();
        try {
            if (!this.outEncrypted.hasData()) {
                int n = this.session.write(EMPTY_BUFFER);
                return n;
            }
            ByteBuffer outEncryptedBuf = this.outEncrypted.acquire();
            if (this.status == IOSession.Status.CLOSED) {
                outEncryptedBuf.clear();
            }
            int bytesWritten = 0;
            if (outEncryptedBuf.position() > 0) {
                outEncryptedBuf.flip();
                try {
                    bytesWritten = this.session.write(outEncryptedBuf);
                } finally {
                    outEncryptedBuf.compact();
                }
            }
            if (outEncryptedBuf.position() == 0) {
                this.outEncrypted.release();
            }
            int n = bytesWritten;
            return n;
        } finally {
            this.session.getLock().unlock();
        }
    }

    private int receiveEncryptedData() throws IOException {
        if (this.endOfStream) {
            return -1;
        }
        ByteBuffer inEncryptedBuf = this.inEncrypted.acquire();
        int bytesRead = this.session.read(inEncryptedBuf);
        if (inEncryptedBuf.position() == 0) {
            this.inEncrypted.release();
        }
        if (bytesRead == -1) {
            this.endOfStream = true;
        }
        return bytesRead;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void decryptData() throws IOException {
        SSLEngineResult.HandshakeStatus handshakeStatus = this.sslEngine.getHandshakeStatus();
        if ((handshakeStatus == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING || handshakeStatus == SSLEngineResult.HandshakeStatus.FINISHED) && this.inEncrypted.hasData()) {
            ByteBuffer inEncryptedBuf = this.inEncrypted.acquire();
            inEncryptedBuf.flip();
            try {
                while (inEncryptedBuf.hasRemaining()) {
                    ByteBuffer inPlainBuf = this.inPlain.acquire();
                    try {
                        SSLEngineResult result = this.doUnwrap(inEncryptedBuf, inPlainBuf);
                        if (!inEncryptedBuf.hasRemaining() && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                            throw new SSLException("Unable to complete SSL handshake");
                        }
                        if (this.sslEngine.isInboundDone()) {
                            this.endOfStream = true;
                        }
                        if (inPlainBuf.hasRemaining()) {
                            inPlainBuf.flip();
                            try {
                                this.ensureHandler().inputReady(this, inPlainBuf.hasRemaining() ? inPlainBuf : null);
                            } finally {
                                inPlainBuf.clear();
                            }
                        }
                        if (result.getStatus() == SSLEngineResult.Status.OK) continue;
                        if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW && this.endOfStream) {
                            throw new SSLException("Unable to decrypt incoming data due to unexpected end of stream");
                        }
                        break;
                    } finally {
                        this.inPlain.release();
                    }
                }
            } finally {
                inEncryptedBuf.compact();
                if (inEncryptedBuf.position() == 0) {
                    this.inEncrypted.release();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void encryptData() throws IOException {
        boolean appReady;
        this.session.getLock().lock();
        try {
            appReady = (this.appEventMask & 4) > 0 && this.status == IOSession.Status.ACTIVE && this.sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        } finally {
            this.session.getLock().unlock();
        }
        if (appReady) {
            this.ensureHandler().outputReady(this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        Args.notNull(src, "Byte buffer");
        this.session.getLock().lock();
        try {
            if (this.status != IOSession.Status.ACTIVE) {
                throw new ClosedChannelException();
            }
            if (!this.initialized) {
                int n = 0;
                return n;
            }
            ByteBuffer outEncryptedBuf = this.outEncrypted.acquire();
            SSLEngineResult result = this.doWrap(src, outEncryptedBuf);
            int n = result.bytesConsumed();
            return n;
        } finally {
            this.session.getLock().unlock();
        }
    }

    @Override
    public int read(ByteBuffer dst) {
        return this.endOfStream ? -1 : 0;
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
    public void upgrade(IOEventHandler handler) {
        this.session.upgrade(handler);
    }

    public TlsDetails getTlsDetails() {
        return this.tlsDetails;
    }

    @Override
    public boolean isOpen() {
        return this.status == IOSession.Status.ACTIVE && this.session.isOpen();
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
        this.session.getLock().lock();
        try {
            if (closeMode == CloseMode.GRACEFUL) {
                if (this.status.compareTo(IOSession.Status.CLOSING) >= 0) {
                    return;
                }
                this.status = IOSession.Status.CLOSING;
                if (this.session.getSocketTimeout().isDisabled()) {
                    this.session.setSocketTimeout(Timeout.ofMilliseconds(1000L));
                }
                try {
                    this.updateEventMask();
                } catch (CancelledKeyException ex) {
                    this.session.close(CloseMode.GRACEFUL);
                } catch (Exception ex) {
                    this.session.close(CloseMode.IMMEDIATE);
                }
            } else {
                if (this.status == IOSession.Status.CLOSED) {
                    return;
                }
                this.inEncrypted.release();
                this.outEncrypted.release();
                this.inPlain.release();
                this.status = IOSession.Status.CLOSED;
                this.session.close(closeMode);
            }
        } finally {
            this.session.getLock().unlock();
        }
    }

    @Override
    public IOSession.Status getStatus() {
        return this.status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void enqueue(Command command, Command.Priority priority) {
        this.session.getLock().lock();
        try {
            this.session.enqueue(command, priority);
            this.setEvent(4);
        } finally {
            this.session.getLock().unlock();
        }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getEventMask() {
        this.session.getLock().lock();
        try {
            int n = this.appEventMask;
            return n;
        } finally {
            this.session.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEventMask(int ops) {
        this.session.getLock().lock();
        try {
            this.appEventMask = ops;
            this.updateEventMask();
        } finally {
            this.session.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEvent(int op) {
        this.session.getLock().lock();
        try {
            this.appEventMask |= op;
            this.updateEventMask();
        } finally {
            this.session.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void clearEvent(int op) {
        this.session.getLock().lock();
        try {
            this.appEventMask &= ~op;
            this.updateEventMask();
        } finally {
            this.session.getLock().unlock();
        }
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.session.getSocketTimeout();
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        this.socketTimeout = timeout;
        if (this.sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            this.session.setSocketTimeout(timeout);
        }
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
    public long getLastReadTime() {
        return this.session.getLastReadTime();
    }

    @Override
    public long getLastWriteTime() {
        return this.session.getLastWriteTime();
    }

    @Override
    public long getLastEventTime() {
        return this.session.getLastEventTime();
    }

    private static void formatOps(StringBuilder buffer, int ops) {
        if ((ops & 1) > 0) {
            buffer.append('r');
        }
        if ((ops & 4) > 0) {
            buffer.append('w');
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        this.session.getLock().lock();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.session);
            buffer.append("[");
            buffer.append((Object)this.status);
            buffer.append("][");
            SSLIOSession.formatOps(buffer, this.appEventMask);
            buffer.append("][");
            buffer.append((Object)this.sslEngine.getHandshakeStatus());
            if (this.sslEngine.isInboundDone()) {
                buffer.append("][inbound done][");
            }
            if (this.sslEngine.isOutboundDone()) {
                buffer.append("][outbound done][");
            }
            if (this.endOfStream) {
                buffer.append("][EOF][");
            }
            buffer.append("][");
            buffer.append(!this.inEncrypted.hasData() ? 0 : this.inEncrypted.acquire().position());
            buffer.append("][");
            buffer.append(!this.inPlain.hasData() ? 0 : this.inPlain.acquire().position());
            buffer.append("][");
            buffer.append(!this.outEncrypted.hasData() ? 0 : this.outEncrypted.acquire().position());
            buffer.append("]");
            String string = buffer.toString();
            return string;
        } finally {
            this.session.getLock().unlock();
        }
    }
}

