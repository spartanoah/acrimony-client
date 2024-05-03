/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.ApplicationProtocolAccessor;
import io.netty.handler.ssl.ExtendedOpenSslSession;
import io.netty.handler.ssl.JdkSslEngine;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import java.net.Socket;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509ExtendedTrustManager;

@SuppressJava6Requirement(reason="Usage guarded by java version check")
final class OpenSslTlsv13X509ExtendedTrustManager
extends X509ExtendedTrustManager {
    private final X509ExtendedTrustManager tm;

    private OpenSslTlsv13X509ExtendedTrustManager(X509ExtendedTrustManager tm) {
        this.tm = tm;
    }

    static X509ExtendedTrustManager wrap(X509ExtendedTrustManager tm) {
        if (!SslProvider.isTlsv13Supported((SslProvider)SslProvider.JDK) && SslProvider.isTlsv13Supported((SslProvider)SslProvider.OPENSSL)) {
            return new OpenSslTlsv13X509ExtendedTrustManager(tm);
        }
        return tm;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        this.tm.checkClientTrusted(x509Certificates, s, socket);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        this.tm.checkServerTrusted(x509Certificates, s, socket);
    }

    private static SSLEngine wrapEngine(final SSLEngine engine) {
        final SSLSession session = engine.getHandshakeSession();
        if (session != null && "TLSv1.3".equals(session.getProtocol())) {
            return new JdkSslEngine(engine){

                @Override
                public String getNegotiatedApplicationProtocol() {
                    if (engine instanceof ApplicationProtocolAccessor) {
                        return ((ApplicationProtocolAccessor)((Object)engine)).getNegotiatedApplicationProtocol();
                    }
                    return super.getNegotiatedApplicationProtocol();
                }

                @Override
                public SSLSession getHandshakeSession() {
                    if (PlatformDependent.javaVersion() >= 7 && session instanceof ExtendedOpenSslSession) {
                        final ExtendedOpenSslSession extendedOpenSslSession = (ExtendedOpenSslSession)session;
                        return new ExtendedOpenSslSession(extendedOpenSslSession){

                            @Override
                            public List getRequestedServerNames() {
                                return extendedOpenSslSession.getRequestedServerNames();
                            }

                            @Override
                            public String[] getPeerSupportedSignatureAlgorithms() {
                                return extendedOpenSslSession.getPeerSupportedSignatureAlgorithms();
                            }

                            @Override
                            public String getProtocol() {
                                return "TLSv1.2";
                            }
                        };
                    }
                    return new SSLSession(){

                        @Override
                        public byte[] getId() {
                            return session.getId();
                        }

                        @Override
                        public SSLSessionContext getSessionContext() {
                            return session.getSessionContext();
                        }

                        @Override
                        public long getCreationTime() {
                            return session.getCreationTime();
                        }

                        @Override
                        public long getLastAccessedTime() {
                            return session.getLastAccessedTime();
                        }

                        @Override
                        public void invalidate() {
                            session.invalidate();
                        }

                        @Override
                        public boolean isValid() {
                            return session.isValid();
                        }

                        @Override
                        public void putValue(String s, Object o) {
                            session.putValue(s, o);
                        }

                        @Override
                        public Object getValue(String s) {
                            return session.getValue(s);
                        }

                        @Override
                        public void removeValue(String s) {
                            session.removeValue(s);
                        }

                        @Override
                        public String[] getValueNames() {
                            return session.getValueNames();
                        }

                        @Override
                        public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
                            return session.getPeerCertificates();
                        }

                        @Override
                        public Certificate[] getLocalCertificates() {
                            return session.getLocalCertificates();
                        }

                        @Override
                        public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
                            return session.getPeerCertificateChain();
                        }

                        @Override
                        public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
                            return session.getPeerPrincipal();
                        }

                        @Override
                        public Principal getLocalPrincipal() {
                            return session.getLocalPrincipal();
                        }

                        @Override
                        public String getCipherSuite() {
                            return session.getCipherSuite();
                        }

                        @Override
                        public String getProtocol() {
                            return "TLSv1.2";
                        }

                        @Override
                        public String getPeerHost() {
                            return session.getPeerHost();
                        }

                        @Override
                        public int getPeerPort() {
                            return session.getPeerPort();
                        }

                        @Override
                        public int getPacketBufferSize() {
                            return session.getPacketBufferSize();
                        }

                        @Override
                        public int getApplicationBufferSize() {
                            return session.getApplicationBufferSize();
                        }
                    };
                }
            };
        }
        return engine;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        this.tm.checkClientTrusted(x509Certificates, s, OpenSslTlsv13X509ExtendedTrustManager.wrapEngine(sslEngine));
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        this.tm.checkServerTrusted(x509Certificates, s, OpenSslTlsv13X509ExtendedTrustManager.wrapEngine(sslEngine));
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        this.tm.checkClientTrusted(x509Certificates, s);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        this.tm.checkServerTrusted(x509Certificates, s);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.tm.getAcceptedIssuers();
    }
}

