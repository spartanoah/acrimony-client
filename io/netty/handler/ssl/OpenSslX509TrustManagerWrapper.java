/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressJava6Requirement(reason="Usage guarded by java version check")
final class OpenSslX509TrustManagerWrapper {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(OpenSslX509TrustManagerWrapper.class);
    private static final TrustManagerWrapper WRAPPER;

    private OpenSslX509TrustManagerWrapper() {
    }

    static X509TrustManager wrapIfNeeded(X509TrustManager trustManager) {
        return WRAPPER.wrapIfNeeded(trustManager);
    }

    private static SSLContext newSSLContext() throws NoSuchAlgorithmException, NoSuchProviderException {
        return SSLContext.getInstance("TLS", "SunJSSE");
    }

    static {
        TrustManagerWrapper wrapper = new TrustManagerWrapper(){

            @Override
            public X509TrustManager wrapIfNeeded(X509TrustManager manager) {
                return manager;
            }
        };
        Throwable cause = null;
        Throwable unsafeCause = PlatformDependent.getUnsafeUnavailabilityCause();
        if (unsafeCause == null) {
            SSLContext context;
            try {
                context = OpenSslX509TrustManagerWrapper.newSSLContext();
                context.init(null, new TrustManager[]{new X509TrustManager(){

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        throw new CertificateException();
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        throw new CertificateException();
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return EmptyArrays.EMPTY_X509_CERTIFICATES;
                    }
                }}, null);
            } catch (Throwable error) {
                context = null;
                cause = error;
            }
            if (cause != null) {
                LOGGER.debug("Unable to access wrapped TrustManager", cause);
            } else {
                final SSLContext finalContext = context;
                Object maybeWrapper = AccessController.doPrivileged(new PrivilegedAction<Object>(){

                    @Override
                    public Object run() {
                        try {
                            Field contextSpiField = SSLContext.class.getDeclaredField("contextSpi");
                            long spiOffset = PlatformDependent.objectFieldOffset(contextSpiField);
                            Object spi = PlatformDependent.getObject(finalContext, spiOffset);
                            if (spi != null) {
                                Class<?> clazz = spi.getClass();
                                do {
                                    try {
                                        Field trustManagerField = clazz.getDeclaredField("trustManager");
                                        long tmOffset = PlatformDependent.objectFieldOffset(trustManagerField);
                                        Object trustManager = PlatformDependent.getObject(spi, tmOffset);
                                        if (trustManager instanceof X509ExtendedTrustManager) {
                                            return new UnsafeTrustManagerWrapper(spiOffset, tmOffset);
                                        }
                                    } catch (NoSuchFieldException noSuchFieldException) {
                                        // empty catch block
                                    }
                                } while ((clazz = clazz.getSuperclass()) != null);
                            }
                            throw new NoSuchFieldException();
                        } catch (NoSuchFieldException e) {
                            return e;
                        } catch (SecurityException e) {
                            return e;
                        }
                    }
                });
                if (maybeWrapper instanceof Throwable) {
                    LOGGER.debug("Unable to access wrapped TrustManager", (Throwable)maybeWrapper);
                } else {
                    wrapper = (TrustManagerWrapper)maybeWrapper;
                }
            }
        } else {
            LOGGER.debug("Unable to access wrapped TrustManager", cause);
        }
        WRAPPER = wrapper;
    }

    private static final class UnsafeTrustManagerWrapper
    implements TrustManagerWrapper {
        private final long spiOffset;
        private final long tmOffset;

        UnsafeTrustManagerWrapper(long spiOffset, long tmOffset) {
            this.spiOffset = spiOffset;
            this.tmOffset = tmOffset;
        }

        @Override
        @SuppressJava6Requirement(reason="Usage guarded by java version check")
        public X509TrustManager wrapIfNeeded(X509TrustManager manager) {
            if (!(manager instanceof X509ExtendedTrustManager)) {
                try {
                    Object tm;
                    SSLContext ctx = OpenSslX509TrustManagerWrapper.newSSLContext();
                    ctx.init(null, new TrustManager[]{manager}, null);
                    Object spi = PlatformDependent.getObject(ctx, this.spiOffset);
                    if (spi != null && (tm = PlatformDependent.getObject(spi, this.tmOffset)) instanceof X509ExtendedTrustManager) {
                        return (X509TrustManager)tm;
                    }
                } catch (NoSuchAlgorithmException e) {
                    PlatformDependent.throwException(e);
                } catch (KeyManagementException e) {
                    PlatformDependent.throwException(e);
                } catch (NoSuchProviderException e) {
                    PlatformDependent.throwException(e);
                }
            }
            return manager;
        }
    }

    private static interface TrustManagerWrapper {
        public X509TrustManager wrapIfNeeded(X509TrustManager var1);
    }
}

