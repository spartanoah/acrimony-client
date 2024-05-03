/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.ssl;

import java.net.SocketAddress;
import javax.net.ssl.SSLContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.ssl.TlsSupport;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

public class BasicClientTlsStrategy
implements TlsStrategy {
    private final SSLContext sslContext;
    private final SSLBufferMode sslBufferMode;
    private final SSLSessionInitializer initializer;
    private final SSLSessionVerifier verifier;

    public BasicClientTlsStrategy(SSLContext sslContext, SSLBufferMode sslBufferMode, SSLSessionInitializer initializer, SSLSessionVerifier verifier) {
        this.sslContext = Args.notNull(sslContext, "SSL context");
        this.sslBufferMode = sslBufferMode;
        this.initializer = initializer;
        this.verifier = verifier;
    }

    public BasicClientTlsStrategy(SSLContext sslContext, SSLSessionInitializer initializer, SSLSessionVerifier verifier) {
        this(sslContext, null, initializer, verifier);
    }

    public BasicClientTlsStrategy(SSLContext sslContext, SSLSessionVerifier verifier) {
        this(sslContext, null, null, verifier);
    }

    public BasicClientTlsStrategy(SSLContext sslContext) {
        this(sslContext, null, null, null);
    }

    public BasicClientTlsStrategy() {
        this(SSLContexts.createSystemDefault());
    }

    @Override
    public boolean upgrade(TransportSecurityLayer tlsSession, HttpHost host, SocketAddress localAddress, SocketAddress remoteAddress, Object attachment, Timeout handshakeTimeout) {
        String scheme;
        String string = scheme = host != null ? host.getSchemeName() : null;
        if (URIScheme.HTTPS.same(scheme)) {
            tlsSession.startTls(this.sslContext, host, this.sslBufferMode, TlsSupport.enforceStrongSecurity(this.initializer), this.verifier, handshakeTimeout);
            return true;
        }
        return false;
    }
}

