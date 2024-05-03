/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.ssl.H2TlsSupport
 */
package org.apache.hc.client5.http.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.apache.hc.client5.http.ssl.AbstractClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.ssl.H2TlsSupport;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContexts;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultClientTlsStrategy
extends AbstractClientTlsStrategy {
    private final Factory<SSLEngine, TlsDetails> tlsDetailsFactory;

    public static TlsStrategy getDefault() {
        return new DefaultClientTlsStrategy(SSLContexts.createDefault(), HttpsSupport.getDefaultHostnameVerifier());
    }

    public static TlsStrategy getSystemDefault() {
        return new DefaultClientTlsStrategy(SSLContexts.createSystemDefault(), HttpsSupport.getSystemProtocols(), HttpsSupport.getSystemCipherSuits(), SSLBufferMode.STATIC, HttpsSupport.getDefaultHostnameVerifier());
    }

    public DefaultClientTlsStrategy(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, SSLBufferMode sslBufferManagement, HostnameVerifier hostnameVerifier, Factory<SSLEngine, TlsDetails> tlsDetailsFactory) {
        super(sslContext, supportedProtocols, supportedCipherSuites, sslBufferManagement, hostnameVerifier);
        this.tlsDetailsFactory = tlsDetailsFactory;
    }

    public DefaultClientTlsStrategy(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, SSLBufferMode sslBufferManagement, HostnameVerifier hostnameVerifier) {
        this(sslContext, supportedProtocols, supportedCipherSuites, sslBufferManagement, hostnameVerifier, null);
    }

    public DefaultClientTlsStrategy(SSLContext sslcontext, HostnameVerifier hostnameVerifier) {
        this(sslcontext, null, null, SSLBufferMode.STATIC, hostnameVerifier, null);
    }

    public DefaultClientTlsStrategy(SSLContext sslcontext) {
        this(sslcontext, HttpsSupport.getDefaultHostnameVerifier());
    }

    @Override
    void applyParameters(SSLEngine sslEngine, SSLParameters sslParameters, String[] appProtocols) {
        H2TlsSupport.setApplicationProtocols((SSLParameters)sslParameters, (String[])appProtocols);
        sslEngine.setSSLParameters(sslParameters);
    }

    @Override
    TlsDetails createTlsDetails(SSLEngine sslEngine) {
        return this.tlsDetailsFactory != null ? this.tlsDetailsFactory.create(sslEngine) : null;
    }
}

