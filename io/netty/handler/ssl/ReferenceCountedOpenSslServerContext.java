/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.CertificateCallback
 *  io.netty.internal.tcnative.CertificateVerifier
 *  io.netty.internal.tcnative.SSLContext
 *  io.netty.internal.tcnative.SniHostNameMatcher
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslCachingX509KeyManagerFactory;
import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslKeyMaterialManager;
import io.netty.handler.ssl.OpenSslKeyMaterialProvider;
import io.netty.handler.ssl.OpenSslServerSessionContext;
import io.netty.handler.ssl.OpenSslSessionTicketKey;
import io.netty.handler.ssl.OpenSslTlsv13X509ExtendedTrustManager;
import io.netty.handler.ssl.OpenSslX509KeyManagerFactory;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslContextOption;
import io.netty.internal.tcnative.CertificateCallback;
import io.netty.internal.tcnative.CertificateVerifier;
import io.netty.internal.tcnative.SSLContext;
import io.netty.internal.tcnative.SniHostNameMatcher;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

public final class ReferenceCountedOpenSslServerContext
extends ReferenceCountedOpenSslContext {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslServerContext.class);
    private static final byte[] ID = new byte[]{110, 101, 116, 116, 121};
    private final OpenSslServerSessionContext sessionContext;

    ReferenceCountedOpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, String keyStore, Map.Entry<SslContextOption<?>, Object> ... options) throws SSLException {
        this(trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, ciphers, cipherFilter, ReferenceCountedOpenSslServerContext.toNegotiator(apn), sessionCacheSize, sessionTimeout, clientAuth, protocols, startTls, enableOcsp, keyStore, options);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ReferenceCountedOpenSslServerContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, String keyStore, Map.Entry<SslContextOption<?>, Object> ... options) throws SSLException {
        super(ciphers, cipherFilter, apn, 1, keyCertChain, clientAuth, protocols, startTls, enableOcsp, true, options);
        boolean success = false;
        try {
            this.sessionContext = ReferenceCountedOpenSslServerContext.newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, keyStore, sessionCacheSize, sessionTimeout);
            if (SERVER_ENABLE_SESSION_TICKET) {
                this.sessionContext.setTicketKeys(new OpenSslSessionTicketKey[0]);
            }
            success = true;
        } finally {
            if (!success) {
                this.release();
            }
        }
    }

    @Override
    public OpenSslServerSessionContext sessionContext() {
        return this.sessionContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OpenSslServerSessionContext newSessionContext(ReferenceCountedOpenSslContext thiz, long ctx, OpenSslEngineMap engineMap, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, String keyStore, long sessionCacheSize, long sessionTimeout) throws SSLException {
        OpenSslKeyMaterialProvider keyMaterialProvider = null;
        try {
            try {
                SSLContext.setVerify((long)ctx, (int)0, (int)10);
                if (!OpenSsl.useKeyManagerFactory()) {
                    if (keyManagerFactory != null) {
                        throw new IllegalArgumentException("KeyManagerFactory not supported");
                    }
                    ObjectUtil.checkNotNull(keyCertChain, "keyCertChain");
                    ReferenceCountedOpenSslServerContext.setKeyMaterial(ctx, keyCertChain, key, keyPassword);
                } else {
                    if (keyManagerFactory == null) {
                        char[] keyPasswordChars = ReferenceCountedOpenSslServerContext.keyStorePassword((String)keyPassword);
                        KeyStore ks = ReferenceCountedOpenSslServerContext.buildKeyStore((X509Certificate[])keyCertChain, (PrivateKey)key, (char[])keyPasswordChars, (String)keyStore);
                        keyManagerFactory = ks.aliases().hasMoreElements() ? new OpenSslX509KeyManagerFactory() : new OpenSslCachingX509KeyManagerFactory(KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()));
                        keyManagerFactory.init(ks, keyPasswordChars);
                    }
                    keyMaterialProvider = ReferenceCountedOpenSslServerContext.providerFor(keyManagerFactory, keyPassword);
                    SSLContext.setCertificateCallback((long)ctx, (CertificateCallback)new OpenSslServerCertificateCallback(engineMap, new OpenSslKeyMaterialManager(keyMaterialProvider)));
                }
            } catch (Exception e) {
                throw new SSLException("failed to set certificate and key", e);
            }
            try {
                if (trustCertCollection != null) {
                    trustManagerFactory = ReferenceCountedOpenSslServerContext.buildTrustManagerFactory((X509Certificate[])trustCertCollection, (TrustManagerFactory)trustManagerFactory, (String)keyStore);
                } else if (trustManagerFactory == null) {
                    trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init((KeyStore)null);
                }
                X509TrustManager manager = ReferenceCountedOpenSslServerContext.chooseTrustManager(trustManagerFactory.getTrustManagers());
                ReferenceCountedOpenSslServerContext.setVerifyCallback(ctx, engineMap, manager);
                X509Certificate[] issuers = manager.getAcceptedIssuers();
                if (issuers != null && issuers.length > 0) {
                    long bio = 0L;
                    try {
                        bio = ReferenceCountedOpenSslServerContext.toBIO(ByteBufAllocator.DEFAULT, issuers);
                        if (!SSLContext.setCACertificateBio((long)ctx, (long)bio)) {
                            throw new SSLException("unable to setup accepted issuers for trustmanager " + manager);
                        }
                    } finally {
                        ReferenceCountedOpenSslServerContext.freeBio(bio);
                    }
                }
                if (PlatformDependent.javaVersion() >= 8) {
                    SSLContext.setSniHostnameMatcher((long)ctx, (SniHostNameMatcher)new OpenSslSniHostnameMatcher(engineMap));
                }
            } catch (SSLException e) {
                throw e;
            } catch (Exception e) {
                throw new SSLException("unable to setup trustmanager", e);
            }
            OpenSslServerSessionContext sessionContext = new OpenSslServerSessionContext(thiz, keyMaterialProvider);
            sessionContext.setSessionIdContext(ID);
            sessionContext.setSessionCacheEnabled(SERVER_ENABLE_SESSION_CACHE);
            if (sessionCacheSize > 0L) {
                sessionContext.setSessionCacheSize((int)Math.min(sessionCacheSize, Integer.MAX_VALUE));
            }
            if (sessionTimeout > 0L) {
                sessionContext.setSessionTimeout((int)Math.min(sessionTimeout, Integer.MAX_VALUE));
            }
            keyMaterialProvider = null;
            OpenSslServerSessionContext openSslServerSessionContext = sessionContext;
            return openSslServerSessionContext;
        } finally {
            if (keyMaterialProvider != null) {
                keyMaterialProvider.destroy();
            }
        }
    }

    @SuppressJava6Requirement(reason="Guarded by java version check")
    private static void setVerifyCallback(long ctx, OpenSslEngineMap engineMap, X509TrustManager manager) {
        if (ReferenceCountedOpenSslServerContext.useExtendedTrustManager(manager)) {
            SSLContext.setCertVerifyCallback((long)ctx, (CertificateVerifier)new ExtendedTrustManagerVerifyCallback(engineMap, (X509ExtendedTrustManager)manager));
        } else {
            SSLContext.setCertVerifyCallback((long)ctx, (CertificateVerifier)new TrustManagerVerifyCallback(engineMap, manager));
        }
    }

    private static final class OpenSslSniHostnameMatcher
    implements SniHostNameMatcher {
        private final OpenSslEngineMap engineMap;

        OpenSslSniHostnameMatcher(OpenSslEngineMap engineMap) {
            this.engineMap = engineMap;
        }

        public boolean match(long ssl, String hostname) {
            ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
            if (engine != null) {
                return engine.checkSniHostnameMatch(hostname.getBytes(CharsetUtil.UTF_8));
            }
            logger.warn("No ReferenceCountedOpenSslEngine found for SSL pointer: {}", (Object)ssl);
            return false;
        }
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    private static final class ExtendedTrustManagerVerifyCallback
    extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier {
        private final X509ExtendedTrustManager manager;

        ExtendedTrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509ExtendedTrustManager manager) {
            super(engineMap);
            this.manager = OpenSslTlsv13X509ExtendedTrustManager.wrap(manager);
        }

        @Override
        void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
            this.manager.checkClientTrusted(peerCerts, auth, engine);
        }
    }

    private static final class TrustManagerVerifyCallback
    extends ReferenceCountedOpenSslContext.AbstractCertificateVerifier {
        private final X509TrustManager manager;

        TrustManagerVerifyCallback(OpenSslEngineMap engineMap, X509TrustManager manager) {
            super(engineMap);
            this.manager = manager;
        }

        @Override
        void verify(ReferenceCountedOpenSslEngine engine, X509Certificate[] peerCerts, String auth) throws Exception {
            this.manager.checkClientTrusted(peerCerts, auth);
        }
    }

    private static final class OpenSslServerCertificateCallback
    implements CertificateCallback {
        private final OpenSslEngineMap engineMap;
        private final OpenSslKeyMaterialManager keyManagerHolder;

        OpenSslServerCertificateCallback(OpenSslEngineMap engineMap, OpenSslKeyMaterialManager keyManagerHolder) {
            this.engineMap = engineMap;
            this.keyManagerHolder = keyManagerHolder;
        }

        public void handle(long ssl, byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws Exception {
            ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
            if (engine == null) {
                return;
            }
            try {
                this.keyManagerHolder.setKeyMaterialServerSide(engine);
            } catch (Throwable cause) {
                engine.initHandshakeException(cause);
                if (cause instanceof Exception) {
                    throw (Exception)cause;
                }
                throw new SSLException(cause);
            }
        }
    }
}

