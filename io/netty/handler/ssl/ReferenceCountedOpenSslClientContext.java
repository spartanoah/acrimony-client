/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.CertificateCallback
 *  io.netty.internal.tcnative.CertificateVerifier
 *  io.netty.internal.tcnative.SSL
 *  io.netty.internal.tcnative.SSLContext
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslCachingX509KeyManagerFactory;
import io.netty.handler.ssl.OpenSslClientSessionCache;
import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslKeyMaterialManager;
import io.netty.handler.ssl.OpenSslKeyMaterialProvider;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslSessionTicketKey;
import io.netty.handler.ssl.OpenSslTlsv13X509ExtendedTrustManager;
import io.netty.handler.ssl.OpenSslX509KeyManagerFactory;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslContextOption;
import io.netty.internal.tcnative.CertificateCallback;
import io.netty.internal.tcnative.CertificateVerifier;
import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import io.netty.util.internal.SuppressJava6Requirement;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public final class ReferenceCountedOpenSslClientContext
extends ReferenceCountedOpenSslContext {
    private static final Set<String> SUPPORTED_KEY_TYPES = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList("RSA", "DH_RSA", "EC", "EC_RSA", "EC_EC")));
    private final OpenSslSessionContext sessionContext;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ReferenceCountedOpenSslClientContext(X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, String[] protocols, long sessionCacheSize, long sessionTimeout, boolean enableOcsp, String keyStore, Map.Entry<SslContextOption<?>, Object> ... options) throws SSLException {
        super(ciphers, cipherFilter, ReferenceCountedOpenSslClientContext.toNegotiator(apn), 0, keyCertChain, ClientAuth.NONE, protocols, false, enableOcsp, true, options);
        boolean success = false;
        try {
            this.sessionContext = ReferenceCountedOpenSslClientContext.newSessionContext(this, this.ctx, this.engineMap, trustCertCollection, trustManagerFactory, keyCertChain, key, keyPassword, keyManagerFactory, keyStore, sessionCacheSize, sessionTimeout);
            success = true;
        } finally {
            if (!success) {
                this.release();
            }
        }
    }

    @Override
    public OpenSslSessionContext sessionContext() {
        return this.sessionContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OpenSslSessionContext newSessionContext(ReferenceCountedOpenSslContext thiz, long ctx, OpenSslEngineMap engineMap, X509Certificate[] trustCertCollection, TrustManagerFactory trustManagerFactory, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword, KeyManagerFactory keyManagerFactory, String keyStore, long sessionCacheSize, long sessionTimeout) throws SSLException {
        if (key == null && keyCertChain != null || key != null && keyCertChain == null) {
            throw new IllegalArgumentException("Either both keyCertChain and key needs to be null or none of them");
        }
        OpenSslKeyMaterialProvider keyMaterialProvider = null;
        try {
            try {
                if (!OpenSsl.useKeyManagerFactory()) {
                    if (keyManagerFactory != null) {
                        throw new IllegalArgumentException("KeyManagerFactory not supported");
                    }
                    if (keyCertChain != null) {
                        ReferenceCountedOpenSslClientContext.setKeyMaterial(ctx, keyCertChain, key, keyPassword);
                    }
                } else {
                    if (keyManagerFactory == null && keyCertChain != null) {
                        char[] keyPasswordChars = ReferenceCountedOpenSslClientContext.keyStorePassword((String)keyPassword);
                        KeyStore ks = ReferenceCountedOpenSslClientContext.buildKeyStore((X509Certificate[])keyCertChain, (PrivateKey)key, (char[])keyPasswordChars, (String)keyStore);
                        keyManagerFactory = ks.aliases().hasMoreElements() ? new OpenSslX509KeyManagerFactory() : new OpenSslCachingX509KeyManagerFactory(KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()));
                        keyManagerFactory.init(ks, keyPasswordChars);
                        keyMaterialProvider = ReferenceCountedOpenSslClientContext.providerFor(keyManagerFactory, keyPassword);
                    } else if (keyManagerFactory != null) {
                        keyMaterialProvider = ReferenceCountedOpenSslClientContext.providerFor(keyManagerFactory, keyPassword);
                    }
                    if (keyMaterialProvider != null) {
                        OpenSslKeyMaterialManager materialManager = new OpenSslKeyMaterialManager(keyMaterialProvider);
                        SSLContext.setCertificateCallback((long)ctx, (CertificateCallback)new OpenSslClientCertificateCallback(engineMap, materialManager));
                    }
                }
            } catch (Exception e) {
                throw new SSLException("failed to set certificate and key", e);
            }
            SSLContext.setVerify((long)ctx, (int)1, (int)10);
            try {
                if (trustCertCollection != null) {
                    trustManagerFactory = ReferenceCountedOpenSslClientContext.buildTrustManagerFactory((X509Certificate[])trustCertCollection, (TrustManagerFactory)trustManagerFactory, (String)keyStore);
                } else if (trustManagerFactory == null) {
                    trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init((KeyStore)null);
                }
                X509TrustManager manager = ReferenceCountedOpenSslClientContext.chooseTrustManager(trustManagerFactory.getTrustManagers());
                ReferenceCountedOpenSslClientContext.setVerifyCallback(ctx, engineMap, manager);
            } catch (Exception e) {
                if (keyMaterialProvider != null) {
                    keyMaterialProvider.destroy();
                }
                throw new SSLException("unable to setup trustmanager", e);
            }
            OpenSslClientSessionContext context = new OpenSslClientSessionContext(thiz, keyMaterialProvider);
            context.setSessionCacheEnabled(CLIENT_ENABLE_SESSION_CACHE);
            if (sessionCacheSize > 0L) {
                context.setSessionCacheSize((int)Math.min(sessionCacheSize, Integer.MAX_VALUE));
            }
            if (sessionTimeout > 0L) {
                context.setSessionTimeout((int)Math.min(sessionTimeout, Integer.MAX_VALUE));
            }
            if (CLIENT_ENABLE_SESSION_TICKET) {
                context.setTicketKeys(new OpenSslSessionTicketKey[0]);
            }
            keyMaterialProvider = null;
            OpenSslClientSessionContext openSslClientSessionContext = context;
            return openSslClientSessionContext;
        } finally {
            if (keyMaterialProvider != null) {
                keyMaterialProvider.destroy();
            }
        }
    }

    @SuppressJava6Requirement(reason="Guarded by java version check")
    private static void setVerifyCallback(long ctx, OpenSslEngineMap engineMap, X509TrustManager manager) {
        if (ReferenceCountedOpenSslClientContext.useExtendedTrustManager(manager)) {
            SSLContext.setCertVerifyCallback((long)ctx, (CertificateVerifier)new ExtendedTrustManagerVerifyCallback(engineMap, (X509ExtendedTrustManager)manager));
        } else {
            SSLContext.setCertVerifyCallback((long)ctx, (CertificateVerifier)new TrustManagerVerifyCallback(engineMap, manager));
        }
    }

    private static final class OpenSslClientCertificateCallback
    implements CertificateCallback {
        private final OpenSslEngineMap engineMap;
        private final OpenSslKeyMaterialManager keyManagerHolder;

        OpenSslClientCertificateCallback(OpenSslEngineMap engineMap, OpenSslKeyMaterialManager keyManagerHolder) {
            this.engineMap = engineMap;
            this.keyManagerHolder = keyManagerHolder;
        }

        public void handle(long ssl, byte[] keyTypeBytes, byte[][] asn1DerEncodedPrincipals) throws Exception {
            ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
            if (engine == null) {
                return;
            }
            try {
                X500Principal[] issuers;
                Set<String> keyTypesSet = OpenSslClientCertificateCallback.supportedClientKeyTypes(keyTypeBytes);
                String[] keyTypes = keyTypesSet.toArray(new String[0]);
                if (asn1DerEncodedPrincipals == null) {
                    issuers = null;
                } else {
                    issuers = new X500Principal[asn1DerEncodedPrincipals.length];
                    for (int i = 0; i < asn1DerEncodedPrincipals.length; ++i) {
                        issuers[i] = new X500Principal(asn1DerEncodedPrincipals[i]);
                    }
                }
                this.keyManagerHolder.setKeyMaterialClientSide(engine, keyTypes, issuers);
            } catch (Throwable cause) {
                engine.initHandshakeException(cause);
                if (cause instanceof Exception) {
                    throw (Exception)cause;
                }
                throw new SSLException(cause);
            }
        }

        private static Set<String> supportedClientKeyTypes(byte[] clientCertificateTypes) {
            if (clientCertificateTypes == null) {
                return SUPPORTED_KEY_TYPES;
            }
            HashSet<String> result = new HashSet<String>(clientCertificateTypes.length);
            for (byte keyTypeCode : clientCertificateTypes) {
                String keyType = OpenSslClientCertificateCallback.clientKeyType(keyTypeCode);
                if (keyType == null) continue;
                result.add(keyType);
            }
            return result;
        }

        private static String clientKeyType(byte clientCertificateType) {
            switch (clientCertificateType) {
                case 1: {
                    return "RSA";
                }
                case 3: {
                    return "DH_RSA";
                }
                case 64: {
                    return "EC";
                }
                case 65: {
                    return "EC_RSA";
                }
                case 66: {
                    return "EC_EC";
                }
            }
            return null;
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
            this.manager.checkServerTrusted(peerCerts, auth, engine);
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
            this.manager.checkServerTrusted(peerCerts, auth);
        }
    }

    static final class OpenSslClientSessionContext
    extends OpenSslSessionContext {
        OpenSslClientSessionContext(ReferenceCountedOpenSslContext context, OpenSslKeyMaterialProvider provider) {
            super(context, provider, SSL.SSL_SESS_CACHE_CLIENT, new OpenSslClientSessionCache(context.engineMap));
        }
    }
}

