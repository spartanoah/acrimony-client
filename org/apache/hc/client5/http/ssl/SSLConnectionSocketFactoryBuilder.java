/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;

public class SSLConnectionSocketFactoryBuilder {
    private SSLContext sslContext;
    private String[] tlsVersions;
    private String[] ciphers;
    private HostnameVerifier hostnameVerifier;
    private boolean systemProperties;

    public static SSLConnectionSocketFactoryBuilder create() {
        return new SSLConnectionSocketFactoryBuilder();
    }

    public SSLConnectionSocketFactoryBuilder setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public final SSLConnectionSocketFactoryBuilder setTlsVersions(String ... tlslVersions) {
        this.tlsVersions = tlslVersions;
        return this;
    }

    public final SSLConnectionSocketFactoryBuilder setTlsVersions(TLS ... tlslVersions) {
        this.tlsVersions = new String[tlslVersions.length];
        for (int i = 0; i < tlslVersions.length; ++i) {
            this.tlsVersions[i] = tlslVersions[i].id;
        }
        return this;
    }

    public final SSLConnectionSocketFactoryBuilder setCiphers(String ... ciphers) {
        this.ciphers = ciphers;
        return this;
    }

    public SSLConnectionSocketFactoryBuilder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public final SSLConnectionSocketFactoryBuilder useSystemProperties() {
        this.systemProperties = true;
        return this;
    }

    public SSLConnectionSocketFactory build() {
        String[] tlsVersionsCopy;
        SSLSocketFactory socketFactory = this.sslContext != null ? this.sslContext.getSocketFactory() : (this.systemProperties ? (SSLSocketFactory)SSLSocketFactory.getDefault() : SSLContexts.createDefault().getSocketFactory());
        if (this.tlsVersions != null) {
            tlsVersionsCopy = this.tlsVersions;
        } else {
            Object object = tlsVersionsCopy = this.systemProperties ? HttpsSupport.getSystemProtocols() : null;
        }
        Object ciphersCopy = this.ciphers != null ? this.ciphers : (this.systemProperties ? HttpsSupport.getSystemCipherSuits() : null);
        return new SSLConnectionSocketFactory(socketFactory, tlsVersionsCopy, (String[])ciphersCopy, this.hostnameVerifier != null ? this.hostnameVerifier : HttpsSupport.getDefaultHostnameVerifier());
    }
}

