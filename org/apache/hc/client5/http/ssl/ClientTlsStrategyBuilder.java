/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.ReflectionUtils;

public class ClientTlsStrategyBuilder {
    private SSLContext sslContext;
    private String[] tlsVersions;
    private String[] ciphers;
    private SSLBufferMode sslBufferMode;
    private HostnameVerifier hostnameVerifier;
    private Factory<SSLEngine, TlsDetails> tlsDetailsFactory;
    private boolean systemProperties;

    public static ClientTlsStrategyBuilder create() {
        return new ClientTlsStrategyBuilder();
    }

    public ClientTlsStrategyBuilder setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public final ClientTlsStrategyBuilder setTlsVersions(String ... tlslVersions) {
        this.tlsVersions = tlslVersions;
        return this;
    }

    public final ClientTlsStrategyBuilder setTlsVersions(TLS ... tlslVersions) {
        this.tlsVersions = new String[tlslVersions.length];
        for (int i = 0; i < tlslVersions.length; ++i) {
            this.tlsVersions[i] = tlslVersions[i].id;
        }
        return this;
    }

    public final ClientTlsStrategyBuilder setCiphers(String ... ciphers) {
        this.ciphers = ciphers;
        return this;
    }

    public ClientTlsStrategyBuilder setSslBufferMode(SSLBufferMode sslBufferMode) {
        this.sslBufferMode = sslBufferMode;
        return this;
    }

    public ClientTlsStrategyBuilder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public ClientTlsStrategyBuilder setTlsDetailsFactory(Factory<SSLEngine, TlsDetails> tlsDetailsFactory) {
        this.tlsDetailsFactory = tlsDetailsFactory;
        return this;
    }

    public final ClientTlsStrategyBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public TlsStrategy build() {
        String[] tlsVersionsCopy;
        SSLContext sslContextCopy;
        if (this.sslContext != null) {
            sslContextCopy = this.sslContext;
        } else {
            SSLContext sSLContext = sslContextCopy = this.systemProperties ? SSLContexts.createSystemDefault() : SSLContexts.createDefault();
        }
        if (this.tlsVersions != null) {
            tlsVersionsCopy = this.tlsVersions;
        } else {
            Object object = tlsVersionsCopy = this.systemProperties ? HttpsSupport.getSystemProtocols() : null;
        }
        Object ciphersCopy = this.ciphers != null ? this.ciphers : (this.systemProperties ? HttpsSupport.getSystemCipherSuits() : null);
        Factory<SSLEngine, TlsDetails> tlsDetailsFactoryCopy = this.tlsDetailsFactory != null ? this.tlsDetailsFactory : new Factory<SSLEngine, TlsDetails>(){

            @Override
            public TlsDetails create(SSLEngine sslEngine) {
                SSLSession sslSession = sslEngine.getSession();
                String applicationProtocol = ReflectionUtils.callGetter(sslEngine, "ApplicationProtocol", String.class);
                return new TlsDetails(sslSession, applicationProtocol);
            }
        };
        return new DefaultClientTlsStrategy(sslContextCopy, tlsVersionsCopy, (String[])ciphersCopy, this.sslBufferMode != null ? this.sslBufferMode : SSLBufferMode.STATIC, this.hostnameVerifier != null ? this.hostnameVerifier : HttpsSupport.getDefaultHostnameVerifier(), tlsDetailsFactoryCopy);
    }
}

