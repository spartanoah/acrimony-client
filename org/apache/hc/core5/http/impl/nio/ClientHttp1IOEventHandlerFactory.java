/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.nio.ClientHttp1IOEventHandler;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class ClientHttp1IOEventHandlerFactory
implements IOEventHandlerFactory {
    private final ClientHttp1StreamDuplexerFactory streamDuplexerFactory;
    private final TlsStrategy tlsStrategy;
    private final Timeout handshakeTimeout;

    public ClientHttp1IOEventHandlerFactory(ClientHttp1StreamDuplexerFactory streamDuplexerFactory, TlsStrategy tlsStrategy, Timeout handshakeTimeout) {
        this.streamDuplexerFactory = Args.notNull(streamDuplexerFactory, "Stream duplexer factory");
        this.tlsStrategy = tlsStrategy;
        this.handshakeTimeout = handshakeTimeout;
    }

    @Override
    public IOEventHandler createHandler(ProtocolIOSession ioSession, Object attachment) {
        HttpHost host;
        if (this.tlsStrategy != null && ioSession.getInitialEndpoint() instanceof HttpHost && URIScheme.HTTPS.same((host = (HttpHost)ioSession.getInitialEndpoint()).getSchemeName())) {
            this.tlsStrategy.upgrade(ioSession, host, ioSession.getLocalAddress(), ioSession.getRemoteAddress(), attachment, this.handshakeTimeout);
        }
        return new ClientHttp1IOEventHandler(this.streamDuplexerFactory.create(ioSession));
    }
}

