/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContextOption;
import io.netty.util.ReferenceCounted;
import java.security.cert.Certificate;
import java.util.Map;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

public abstract class OpenSslContext
extends ReferenceCountedOpenSslContext {
    OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apnCfg, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, Map.Entry<SslContextOption<?>, Object> ... options) throws SSLException {
        super(ciphers, cipherFilter, OpenSslContext.toNegotiator(apnCfg), mode, keyCertChain, clientAuth, protocols, startTls, enableOcsp, false, options);
    }

    OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, Map.Entry<SslContextOption<?>, Object> ... options) throws SSLException {
        super(ciphers, cipherFilter, apn, mode, keyCertChain, clientAuth, protocols, startTls, enableOcsp, false, options);
    }

    @Override
    final SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode) {
        return new OpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode);
    }

    protected final void finalize() throws Throwable {
        super.finalize();
        OpenSsl.releaseIfNeeded((ReferenceCounted)this);
    }
}

