/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.hc.core5.http2.HttpVersionPolicy
 *  org.apache.hc.core5.http2.ssl.ApplicationProtocol
 *  org.apache.hc.core5.http2.ssl.H2TlsSupport
 */
package org.apache.hc.client5.http.ssl;

import java.net.SocketAddress;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.TlsSessionValidator;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.ssl.ApplicationProtocol;
import org.apache.hc.core5.http2.ssl.H2TlsSupport;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
abstract class AbstractClientTlsStrategy
implements TlsStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractClientTlsStrategy.class);
    private final SSLContext sslContext;
    private final String[] supportedProtocols;
    private final String[] supportedCipherSuites;
    private final SSLBufferMode sslBufferManagement;
    private final HostnameVerifier hostnameVerifier;
    private final TlsSessionValidator tlsSessionValidator;

    AbstractClientTlsStrategy(SSLContext sslContext, String[] supportedProtocols, String[] supportedCipherSuites, SSLBufferMode sslBufferManagement, HostnameVerifier hostnameVerifier) {
        this.sslContext = Args.notNull(sslContext, "SSL context");
        this.supportedProtocols = supportedProtocols;
        this.supportedCipherSuites = supportedCipherSuites;
        this.sslBufferManagement = sslBufferManagement != null ? sslBufferManagement : SSLBufferMode.STATIC;
        this.hostnameVerifier = hostnameVerifier != null ? hostnameVerifier : HttpsSupport.getDefaultHostnameVerifier();
        this.tlsSessionValidator = new TlsSessionValidator(LOG);
    }

    @Override
    public boolean upgrade(TransportSecurityLayer tlsSession, final HttpHost host, SocketAddress localAddress, SocketAddress remoteAddress, final Object attachment, Timeout handshakeTimeout) {
        tlsSession.startTls(this.sslContext, host, this.sslBufferManagement, new SSLSessionInitializer(){

            @Override
            public void initialize(NamedEndpoint endpoint, SSLEngine sslEngine) {
                HttpVersionPolicy versionPolicy = attachment instanceof HttpVersionPolicy ? (HttpVersionPolicy)attachment : HttpVersionPolicy.NEGOTIATE;
                SSLParameters sslParameters = sslEngine.getSSLParameters();
                if (AbstractClientTlsStrategy.this.supportedProtocols != null) {
                    sslParameters.setProtocols(AbstractClientTlsStrategy.this.supportedProtocols);
                } else if (versionPolicy != HttpVersionPolicy.FORCE_HTTP_1) {
                    sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
                }
                if (AbstractClientTlsStrategy.this.supportedCipherSuites != null) {
                    sslParameters.setCipherSuites(AbstractClientTlsStrategy.this.supportedCipherSuites);
                } else if (versionPolicy == HttpVersionPolicy.FORCE_HTTP_2) {
                    sslParameters.setCipherSuites(TlsCiphers.excludeH2Blacklisted(sslParameters.getCipherSuites()));
                }
                if (versionPolicy != HttpVersionPolicy.FORCE_HTTP_1) {
                    H2TlsSupport.setEnableRetransmissions((SSLParameters)sslParameters, (boolean)false);
                }
                AbstractClientTlsStrategy.this.applyParameters(sslEngine, sslParameters, H2TlsSupport.selectApplicationProtocols((Object)attachment));
                AbstractClientTlsStrategy.this.initializeEngine(sslEngine);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Enabled protocols: {}", (Object)Arrays.asList(sslEngine.getEnabledProtocols()));
                    LOG.debug("Enabled cipher suites:{}", (Object)Arrays.asList(sslEngine.getEnabledCipherSuites()));
                }
            }
        }, new SSLSessionVerifier(){

            @Override
            public TlsDetails verify(NamedEndpoint endpoint, SSLEngine sslEngine) throws SSLException {
                AbstractClientTlsStrategy.this.verifySession(host.getHostName(), sslEngine.getSession());
                TlsDetails tlsDetails = AbstractClientTlsStrategy.this.createTlsDetails(sslEngine);
                String negotiatedCipherSuite = sslEngine.getSession().getCipherSuite();
                if (tlsDetails != null && ApplicationProtocol.HTTP_2.id.equals(tlsDetails.getApplicationProtocol()) && TlsCiphers.isH2Blacklisted(negotiatedCipherSuite)) {
                    throw new SSLHandshakeException("Cipher suite `" + negotiatedCipherSuite + "` does not provide adequate security for HTTP/2");
                }
                return tlsDetails;
            }
        }, handshakeTimeout);
        return true;
    }

    abstract void applyParameters(SSLEngine var1, SSLParameters var2, String[] var3);

    abstract TlsDetails createTlsDetails(SSLEngine var1);

    protected void initializeEngine(SSLEngine sslEngine) {
    }

    protected void verifySession(String hostname, SSLSession sslsession) throws SSLException {
        this.tlsSessionValidator.verifySession(hostname, sslsession, this.hostnameVerifier);
    }
}

