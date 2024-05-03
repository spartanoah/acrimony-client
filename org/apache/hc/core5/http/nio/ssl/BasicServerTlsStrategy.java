/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.ssl;

import java.net.SocketAddress;
import javax.net.ssl.SSLContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.SecurePortStrategy;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.ssl.TlsSupport;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

public class BasicServerTlsStrategy
implements TlsStrategy {
    private final SSLContext sslContext;
    private final SecurePortStrategy securePortStrategy;
    private final SSLBufferMode sslBufferMode;
    private final SSLSessionInitializer initializer;
    private final SSLSessionVerifier verifier;

    public BasicServerTlsStrategy(SSLContext sslContext, SecurePortStrategy securePortStrategy, SSLBufferMode sslBufferMode, SSLSessionInitializer initializer, SSLSessionVerifier verifier) {
        this.sslContext = Args.notNull(sslContext, "SSL context");
        this.securePortStrategy = securePortStrategy;
        this.sslBufferMode = sslBufferMode;
        this.initializer = initializer;
        this.verifier = verifier;
    }

    public BasicServerTlsStrategy(SSLContext sslContext, SecurePortStrategy securePortStrategy, SSLSessionInitializer initializer, SSLSessionVerifier verifier) {
        this(sslContext, securePortStrategy, null, initializer, verifier);
    }

    public BasicServerTlsStrategy(SSLContext sslContext, SecurePortStrategy securePortStrategy, SSLSessionVerifier verifier) {
        this(sslContext, securePortStrategy, null, null, verifier);
    }

    public BasicServerTlsStrategy(SSLContext sslContext, SecurePortStrategy securePortStrategy) {
        this(sslContext, securePortStrategy, null, null, null);
    }

    public BasicServerTlsStrategy(SecurePortStrategy securePortStrategy) {
        this(SSLContexts.createSystemDefault(), securePortStrategy);
    }

    @Override
    public boolean upgrade(TransportSecurityLayer tlsSession, HttpHost host, SocketAddress localAddress, SocketAddress remoteAddress, Object attachment, Timeout handshakeTimeout) {
        if (this.securePortStrategy != null && this.securePortStrategy.isSecure(localAddress)) {
            tlsSession.startTls(this.sslContext, host, this.sslBufferMode, TlsSupport.enforceStrongSecurity(this.initializer), this.verifier, handshakeTimeout);
            return true;
        }
        return false;
    }
}

