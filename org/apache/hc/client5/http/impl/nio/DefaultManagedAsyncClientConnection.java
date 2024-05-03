/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import org.apache.hc.client5.http.nio.ManagedAsyncClientConnection;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.nio.command.ShutdownCommand;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.Command;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.util.Identifiable;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefaultManagedAsyncClientConnection
implements ManagedAsyncClientConnection,
Identifiable {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultManagedAsyncClientConnection.class);
    private final IOSession ioSession;
    private final Timeout socketTimeout;
    private final AtomicBoolean closed;

    public DefaultManagedAsyncClientConnection(IOSession ioSession) {
        this.ioSession = ioSession;
        this.socketTimeout = ioSession.getSocketTimeout();
        this.closed = new AtomicBoolean();
    }

    @Override
    public String getId() {
        return this.ioSession.getId();
    }

    @Override
    public void close(CloseMode closeMode) {
        if (this.closed.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: Shutdown connection {}", (Object)this.getId(), (Object)closeMode);
            }
            this.ioSession.close(closeMode);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.closed.compareAndSet(false, true)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: Close connection", (Object)this.getId());
            }
            this.ioSession.enqueue(new ShutdownCommand(CloseMode.GRACEFUL), Command.Priority.IMMEDIATE);
        }
    }

    @Override
    public boolean isOpen() {
        return this.ioSession.isOpen();
    }

    @Override
    public void setSocketTimeout(Timeout timeout) {
        this.ioSession.setSocketTimeout(timeout);
    }

    @Override
    public Timeout getSocketTimeout() {
        return this.ioSession.getSocketTimeout();
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
    public EndpointDetails getEndpointDetails() {
        IOEventHandler handler = this.ioSession.getHandler();
        if (handler instanceof HttpConnection) {
            return ((HttpConnection)((Object)handler)).getEndpointDetails();
        }
        return null;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        IOEventHandler handler = this.ioSession.getHandler();
        if (handler instanceof HttpConnection) {
            return ((HttpConnection)((Object)handler)).getProtocolVersion();
        }
        return HttpVersion.DEFAULT;
    }

    @Override
    public void startTls(SSLContext sslContext, NamedEndpoint endpoint, SSLBufferMode sslBufferMode, SSLSessionInitializer initializer, SSLSessionVerifier verifier, Timeout handshakeTimeout) throws UnsupportedOperationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: start TLS", (Object)this.getId());
        }
        if (!(this.ioSession instanceof TransportSecurityLayer)) {
            throw new UnsupportedOperationException("TLS upgrade not supported");
        }
        ((TransportSecurityLayer)((Object)this.ioSession)).startTls(sslContext, endpoint, sslBufferMode, initializer, verifier, handshakeTimeout);
    }

    @Override
    public TlsDetails getTlsDetails() {
        return this.ioSession instanceof TransportSecurityLayer ? ((TransportSecurityLayer)((Object)this.ioSession)).getTlsDetails() : null;
    }

    @Override
    public SSLSession getSSLSession() {
        TlsDetails tlsDetails = this.getTlsDetails();
        return tlsDetails != null ? tlsDetails.getSSLSession() : null;
    }

    @Override
    public void submitCommand(Command command, Command.Priority priority) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: {} with {} priority", new Object[]{this.getId(), command.getClass().getSimpleName(), priority});
        }
        this.ioSession.enqueue(command, Command.Priority.IMMEDIATE);
    }

    @Override
    public void passivate() {
        this.ioSession.setSocketTimeout(Timeout.ZERO_MILLISECONDS);
    }

    @Override
    public void activate() {
        this.ioSession.setSocketTimeout(this.socketTimeout);
    }
}

