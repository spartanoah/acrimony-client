/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod
 *  io.netty.internal.tcnative.CertificateVerifier
 *  io.netty.internal.tcnative.ResultCallback
 *  io.netty.internal.tcnative.SSL
 *  io.netty.internal.tcnative.SSLContext
 *  io.netty.internal.tcnative.SSLPrivateKeyMethod
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslAsyncPrivateKeyMethod;
import io.netty.handler.ssl.OpenSslCachingX509KeyManagerFactory;
import io.netty.handler.ssl.OpenSslCertificateException;
import io.netty.handler.ssl.OpenSslContextOption;
import io.netty.handler.ssl.OpenSslDefaultApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslKeyMaterialProvider;
import io.netty.handler.ssl.OpenSslPrivateKeyMethod;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslSessionStats;
import io.netty.handler.ssl.OpenSslX509KeyManagerFactory;
import io.netty.handler.ssl.OpenSslX509TrustManagerWrapper;
import io.netty.handler.ssl.PemEncoded;
import io.netty.handler.ssl.PemPrivateKey;
import io.netty.handler.ssl.PemX509Certificate;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextOption;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslUtils;
import io.netty.handler.ssl.util.LazyX509Certificate;
import io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod;
import io.netty.internal.tcnative.CertificateVerifier;
import io.netty.internal.tcnative.ResultCallback;
import io.netty.internal.tcnative.SSL;
import io.netty.internal.tcnative.SSLContext;
import io.netty.internal.tcnative.SSLPrivateKeyMethod;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public abstract class ReferenceCountedOpenSslContext
extends SslContext
implements ReferenceCounted {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslContext.class);
    private static final int DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE = Math.max(1, SystemPropertyUtil.getInt("io.netty.handler.ssl.openssl.bioNonApplicationBufferSize", 2048));
    static final boolean USE_TASKS = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.useTasks", true);
    private static final Integer DH_KEY_LENGTH;
    private static final ResourceLeakDetector<ReferenceCountedOpenSslContext> leakDetector;
    protected static final int VERIFY_DEPTH = 10;
    static final boolean CLIENT_ENABLE_SESSION_TICKET;
    static final boolean CLIENT_ENABLE_SESSION_TICKET_TLSV13;
    static final boolean SERVER_ENABLE_SESSION_TICKET;
    static final boolean SERVER_ENABLE_SESSION_TICKET_TLSV13;
    static final boolean SERVER_ENABLE_SESSION_CACHE;
    static final boolean CLIENT_ENABLE_SESSION_CACHE;
    protected long ctx;
    private final List<String> unmodifiableCiphers;
    private final OpenSslApplicationProtocolNegotiator apn;
    private final int mode;
    private final ResourceLeakTracker<ReferenceCountedOpenSslContext> leak;
    private final AbstractReferenceCounted refCnt = new AbstractReferenceCounted(){

        public ReferenceCounted touch(Object hint) {
            if (ReferenceCountedOpenSslContext.this.leak != null) {
                ReferenceCountedOpenSslContext.this.leak.record(hint);
            }
            return ReferenceCountedOpenSslContext.this;
        }

        @Override
        protected void deallocate() {
            ReferenceCountedOpenSslContext.this.destroy();
            if (ReferenceCountedOpenSslContext.this.leak != null) {
                boolean closed = ReferenceCountedOpenSslContext.this.leak.close(ReferenceCountedOpenSslContext.this);
                assert (closed);
            }
        }
    };
    final Certificate[] keyCertChain;
    final ClientAuth clientAuth;
    final String[] protocols;
    final boolean enableOcsp;
    final OpenSslEngineMap engineMap = new DefaultOpenSslEngineMap();
    final ReadWriteLock ctxLock = new ReentrantReadWriteLock();
    private volatile int bioNonApplicationBufferSize = DEFAULT_BIO_NON_APPLICATION_BUFFER_SIZE;
    static final OpenSslApplicationProtocolNegotiator NONE_PROTOCOL_NEGOTIATOR;
    final boolean tlsFalseStart;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ReferenceCountedOpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn, int mode, Certificate[] keyCertChain, ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp, boolean leakDetection, Map.Entry<SslContextOption<?>, Object> ... ctxOptions) throws SSLException {
        super(startTls);
        OpenSsl.ensureAvailability();
        if (enableOcsp && !OpenSsl.isOcspSupported()) {
            throw new IllegalStateException("OCSP is not supported.");
        }
        if (mode != 1 && mode != 0) {
            throw new IllegalArgumentException("mode most be either SSL.SSL_MODE_SERVER or SSL.SSL_MODE_CLIENT");
        }
        boolean tlsFalseStart = false;
        boolean useTasks = USE_TASKS;
        OpenSslPrivateKeyMethod privateKeyMethod = null;
        OpenSslAsyncPrivateKeyMethod asyncPrivateKeyMethod = null;
        if (ctxOptions != null) {
            for (Map.Entry<SslContextOption<?>, Object> ctxOpt : ctxOptions) {
                SslContextOption<?> option = ctxOpt.getKey();
                if (option == OpenSslContextOption.TLS_FALSE_START) {
                    tlsFalseStart = (Boolean)ctxOpt.getValue();
                    continue;
                }
                if (option == OpenSslContextOption.USE_TASKS) {
                    useTasks = (Boolean)ctxOpt.getValue();
                    continue;
                }
                if (option == OpenSslContextOption.PRIVATE_KEY_METHOD) {
                    privateKeyMethod = (OpenSslPrivateKeyMethod)ctxOpt.getValue();
                    continue;
                }
                if (option == OpenSslContextOption.ASYNC_PRIVATE_KEY_METHOD) {
                    asyncPrivateKeyMethod = (OpenSslAsyncPrivateKeyMethod)ctxOpt.getValue();
                    continue;
                }
                logger.debug("Skipping unsupported " + SslContextOption.class.getSimpleName() + ": " + ctxOpt.getKey());
            }
        }
        if (privateKeyMethod != null && asyncPrivateKeyMethod != null) {
            throw new IllegalArgumentException("You can either only use " + OpenSslAsyncPrivateKeyMethod.class.getSimpleName() + " or " + OpenSslPrivateKeyMethod.class.getSimpleName());
        }
        this.tlsFalseStart = tlsFalseStart;
        this.leak = leakDetection ? leakDetector.track(this) : null;
        this.mode = mode;
        this.clientAuth = this.isServer() ? ObjectUtil.checkNotNull(clientAuth, "clientAuth") : ClientAuth.NONE;
        this.protocols = protocols;
        this.enableOcsp = enableOcsp;
        this.keyCertChain = keyCertChain == null ? null : (Certificate[])keyCertChain.clone();
        this.unmodifiableCiphers = Arrays.asList(ObjectUtil.checkNotNull(cipherFilter, "cipherFilter").filterCipherSuites(ciphers, OpenSsl.DEFAULT_CIPHERS, OpenSsl.availableJavaCipherSuites()));
        this.apn = ObjectUtil.checkNotNull(apn, "apn");
        boolean success = false;
        try {
            List<String> nextProtoList;
            boolean tlsv13Supported = OpenSsl.isTlsv13Supported();
            try {
                int protocolOpts = 30;
                if (tlsv13Supported) {
                    protocolOpts |= 0x20;
                }
                this.ctx = SSLContext.make((int)protocolOpts, (int)mode);
            } catch (Exception e) {
                throw new SSLException("failed to create an SSL_CTX", e);
            }
            StringBuilder cipherBuilder = new StringBuilder();
            StringBuilder cipherTLSv13Builder = new StringBuilder();
            try {
                if (this.unmodifiableCiphers.isEmpty()) {
                    SSLContext.setCipherSuite((long)this.ctx, (String)"", (boolean)false);
                    if (tlsv13Supported) {
                        SSLContext.setCipherSuite((long)this.ctx, (String)"", (boolean)true);
                    }
                } else {
                    CipherSuiteConverter.convertToCipherStrings(this.unmodifiableCiphers, cipherBuilder, cipherTLSv13Builder, OpenSsl.isBoringSSL());
                    SSLContext.setCipherSuite((long)this.ctx, (String)cipherBuilder.toString(), (boolean)false);
                    if (tlsv13Supported) {
                        SSLContext.setCipherSuite((long)this.ctx, (String)OpenSsl.checkTls13Ciphers((InternalLogger)logger, (String)cipherTLSv13Builder.toString()), (boolean)true);
                    }
                }
            } catch (SSLException e) {
                throw e;
            } catch (Exception e) {
                throw new SSLException("failed to set cipher suite: " + this.unmodifiableCiphers, e);
            }
            int options = SSLContext.getOptions((long)this.ctx) | SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_NO_TLSv1 | SSL.SSL_OP_NO_TLSv1_1 | SSL.SSL_OP_CIPHER_SERVER_PREFERENCE | SSL.SSL_OP_NO_COMPRESSION | SSL.SSL_OP_NO_TICKET;
            if (cipherBuilder.length() == 0) {
                options |= SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_NO_TLSv1 | SSL.SSL_OP_NO_TLSv1_1 | SSL.SSL_OP_NO_TLSv1_2;
            }
            SSLContext.setOptions((long)this.ctx, (int)options);
            SSLContext.setMode((long)this.ctx, (int)(SSLContext.getMode((long)this.ctx) | SSL.SSL_MODE_ACCEPT_MOVING_WRITE_BUFFER));
            if (DH_KEY_LENGTH != null) {
                SSLContext.setTmpDHLength((long)this.ctx, (int)DH_KEY_LENGTH);
            }
            if (!(nextProtoList = apn.protocols()).isEmpty()) {
                String[] appProtocols = nextProtoList.toArray(new String[0]);
                int selectorBehavior = ReferenceCountedOpenSslContext.opensslSelectorFailureBehavior(apn.selectorFailureBehavior());
                switch (apn.protocol()) {
                    case NPN: {
                        SSLContext.setNpnProtos((long)this.ctx, (String[])appProtocols, (int)selectorBehavior);
                        break;
                    }
                    case ALPN: {
                        SSLContext.setAlpnProtos((long)this.ctx, (String[])appProtocols, (int)selectorBehavior);
                        break;
                    }
                    case NPN_AND_ALPN: {
                        SSLContext.setNpnProtos((long)this.ctx, (String[])appProtocols, (int)selectorBehavior);
                        SSLContext.setAlpnProtos((long)this.ctx, (String[])appProtocols, (int)selectorBehavior);
                        break;
                    }
                    default: {
                        throw new Error();
                    }
                }
            }
            if (enableOcsp) {
                SSLContext.enableOcsp((long)this.ctx, (boolean)this.isClient());
            }
            SSLContext.setUseTasks((long)this.ctx, (boolean)useTasks);
            if (privateKeyMethod != null) {
                SSLContext.setPrivateKeyMethod((long)this.ctx, (SSLPrivateKeyMethod)new PrivateKeyMethod(this.engineMap, privateKeyMethod));
            }
            if (asyncPrivateKeyMethod != null) {
                SSLContext.setPrivateKeyMethod((long)this.ctx, (AsyncSSLPrivateKeyMethod)new AsyncPrivateKeyMethod(this.engineMap, asyncPrivateKeyMethod));
            }
            success = true;
        } finally {
            if (!success) {
                this.release();
            }
        }
    }

    private static int opensslSelectorFailureBehavior(ApplicationProtocolConfig.SelectorFailureBehavior behavior) {
        switch (behavior) {
            case NO_ADVERTISE: {
                return 0;
            }
            case CHOOSE_MY_LAST_PROTOCOL: {
                return 1;
            }
        }
        throw new Error();
    }

    @Override
    public final List<String> cipherSuites() {
        return this.unmodifiableCiphers;
    }

    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return this.apn;
    }

    @Override
    public final boolean isClient() {
        return this.mode == 0;
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        return this.newEngine0(alloc, peerHost, peerPort, true);
    }

    protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls) {
        return new SslHandler(this.newEngine0(alloc, null, -1, false), startTls);
    }

    protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls) {
        return new SslHandler(this.newEngine0(alloc, peerHost, peerPort, false), startTls);
    }

    protected SslHandler newHandler(ByteBufAllocator alloc, boolean startTls, Executor executor) {
        return new SslHandler(this.newEngine0(alloc, null, -1, false), startTls, executor);
    }

    protected SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls, Executor executor) {
        return new SslHandler(this.newEngine0(alloc, peerHost, peerPort, false), executor);
    }

    SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode) {
        return new ReferenceCountedOpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode, true);
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc) {
        return this.newEngine(alloc, null, -1);
    }

    @Deprecated
    public final long context() {
        return this.sslCtxPointer();
    }

    @Deprecated
    public final OpenSslSessionStats stats() {
        return this.sessionContext().stats();
    }

    @Deprecated
    public void setRejectRemoteInitiatedRenegotiation(boolean rejectRemoteInitiatedRenegotiation) {
        if (!rejectRemoteInitiatedRenegotiation) {
            throw new UnsupportedOperationException("Renegotiation is not supported");
        }
    }

    @Deprecated
    public boolean getRejectRemoteInitiatedRenegotiation() {
        return true;
    }

    public void setBioNonApplicationBufferSize(int bioNonApplicationBufferSize) {
        this.bioNonApplicationBufferSize = ObjectUtil.checkPositiveOrZero(bioNonApplicationBufferSize, "bioNonApplicationBufferSize");
    }

    public int getBioNonApplicationBufferSize() {
        return this.bioNonApplicationBufferSize;
    }

    @Deprecated
    public final void setTicketKeys(byte[] keys) {
        this.sessionContext().setTicketKeys(keys);
    }

    public abstract OpenSslSessionContext sessionContext();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public final long sslCtxPointer() {
        Lock readerLock = this.ctxLock.readLock();
        readerLock.lock();
        try {
            long l = SSLContext.getSslCtx((long)this.ctx);
            return l;
        } finally {
            readerLock.unlock();
        }
    }

    @Deprecated
    public final void setPrivateKeyMethod(OpenSslPrivateKeyMethod method) {
        ObjectUtil.checkNotNull(method, "method");
        Lock writerLock = this.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.setPrivateKeyMethod((long)this.ctx, (SSLPrivateKeyMethod)new PrivateKeyMethod(this.engineMap, method));
        } finally {
            writerLock.unlock();
        }
    }

    @Deprecated
    public final void setUseTasks(boolean useTasks) {
        Lock writerLock = this.ctxLock.writeLock();
        writerLock.lock();
        try {
            SSLContext.setUseTasks((long)this.ctx, (boolean)useTasks);
        } finally {
            writerLock.unlock();
        }
    }

    private void destroy() {
        Lock writerLock = this.ctxLock.writeLock();
        writerLock.lock();
        try {
            if (this.ctx != 0L) {
                if (this.enableOcsp) {
                    SSLContext.disableOcsp((long)this.ctx);
                }
                SSLContext.free((long)this.ctx);
                this.ctx = 0L;
                OpenSslSessionContext context = this.sessionContext();
                if (context != null) {
                    context.destroy();
                }
            }
        } finally {
            writerLock.unlock();
        }
    }

    protected static X509Certificate[] certificates(byte[][] chain) {
        X509Certificate[] peerCerts = new X509Certificate[chain.length];
        for (int i = 0; i < peerCerts.length; ++i) {
            peerCerts[i] = new LazyX509Certificate(chain[i]);
        }
        return peerCerts;
    }

    protected static X509TrustManager chooseTrustManager(TrustManager[] managers) {
        for (TrustManager m : managers) {
            if (!(m instanceof X509TrustManager)) continue;
            if (PlatformDependent.javaVersion() >= 7) {
                return OpenSslX509TrustManagerWrapper.wrapIfNeeded((X509TrustManager)m);
            }
            return (X509TrustManager)m;
        }
        throw new IllegalStateException("no X509TrustManager found");
    }

    protected static X509KeyManager chooseX509KeyManager(KeyManager[] kms) {
        for (KeyManager km : kms) {
            if (!(km instanceof X509KeyManager)) continue;
            return (X509KeyManager)km;
        }
        throw new IllegalStateException("no X509KeyManager found");
    }

    static OpenSslApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config) {
        if (config == null) {
            return NONE_PROTOCOL_NEGOTIATOR;
        }
        switch (config.protocol()) {
            case NONE: {
                return NONE_PROTOCOL_NEGOTIATOR;
            }
            case NPN: 
            case ALPN: 
            case NPN_AND_ALPN: {
                switch (config.selectedListenerFailureBehavior()) {
                    case CHOOSE_MY_LAST_PROTOCOL: 
                    case ACCEPT: {
                        switch (config.selectorFailureBehavior()) {
                            case NO_ADVERTISE: 
                            case CHOOSE_MY_LAST_PROTOCOL: {
                                return new OpenSslDefaultApplicationProtocolNegotiator(config);
                            }
                        }
                        throw new UnsupportedOperationException("OpenSSL provider does not support " + (Object)((Object)config.selectorFailureBehavior()) + " behavior");
                    }
                }
                throw new UnsupportedOperationException("OpenSSL provider does not support " + (Object)((Object)config.selectedListenerFailureBehavior()) + " behavior");
            }
        }
        throw new Error();
    }

    @SuppressJava6Requirement(reason="Guarded by java version check")
    static boolean useExtendedTrustManager(X509TrustManager trustManager) {
        return PlatformDependent.javaVersion() >= 7 && trustManager instanceof X509ExtendedTrustManager;
    }

    @Override
    public final int refCnt() {
        return this.refCnt.refCnt();
    }

    @Override
    public final ReferenceCounted retain() {
        this.refCnt.retain();
        return this;
    }

    @Override
    public final ReferenceCounted retain(int increment) {
        this.refCnt.retain(increment);
        return this;
    }

    public final ReferenceCounted touch() {
        this.refCnt.touch();
        return this;
    }

    public final ReferenceCounted touch(Object hint) {
        this.refCnt.touch(hint);
        return this;
    }

    @Override
    public final boolean release() {
        return this.refCnt.release();
    }

    @Override
    public final boolean release(int decrement) {
        return this.refCnt.release(decrement);
    }

    static void setKeyMaterial(long ctx, X509Certificate[] keyCertChain, PrivateKey key, String keyPassword) throws SSLException {
        long keyBio = 0L;
        long keyCertChainBio = 0L;
        long keyCertChainBio2 = 0L;
        PemEncoded encoded = null;
        try {
            encoded = PemX509Certificate.toPEM(ByteBufAllocator.DEFAULT, true, keyCertChain);
            keyCertChainBio = ReferenceCountedOpenSslContext.toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
            keyCertChainBio2 = ReferenceCountedOpenSslContext.toBIO(ByteBufAllocator.DEFAULT, encoded.retain());
            if (key != null) {
                keyBio = ReferenceCountedOpenSslContext.toBIO(ByteBufAllocator.DEFAULT, key);
            }
            SSLContext.setCertificateBio((long)ctx, (long)keyCertChainBio, (long)keyBio, (String)(keyPassword == null ? "" : keyPassword));
            SSLContext.setCertificateChainBio((long)ctx, (long)keyCertChainBio2, (boolean)true);
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException("failed to set certificate and key", e);
        } finally {
            ReferenceCountedOpenSslContext.freeBio(keyBio);
            ReferenceCountedOpenSslContext.freeBio(keyCertChainBio);
            ReferenceCountedOpenSslContext.freeBio(keyCertChainBio2);
            if (encoded != null) {
                encoded.release();
            }
        }
    }

    static void freeBio(long bio) {
        if (bio != 0L) {
            SSL.freeBIO((long)bio);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long toBIO(ByteBufAllocator allocator, PrivateKey key) throws Exception {
        if (key == null) {
            return 0L;
        }
        PemEncoded pem = PemPrivateKey.toPEM(allocator, true, key);
        try {
            long l = ReferenceCountedOpenSslContext.toBIO(allocator, pem.retain());
            return l;
        } finally {
            pem.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long toBIO(ByteBufAllocator allocator, X509Certificate ... certChain) throws Exception {
        if (certChain == null) {
            return 0L;
        }
        ObjectUtil.checkNonEmpty(certChain, "certChain");
        PemEncoded pem = PemX509Certificate.toPEM(allocator, true, certChain);
        try {
            long l = ReferenceCountedOpenSslContext.toBIO(allocator, pem.retain());
            return l;
        } finally {
            pem.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long toBIO(ByteBufAllocator allocator, PemEncoded pem) throws Exception {
        try {
            long l;
            ByteBuf content = pem.content();
            if (content.isDirect()) {
                long l2 = ReferenceCountedOpenSslContext.newBIO(content.retainedSlice());
                return l2;
            }
            ByteBuf buffer = allocator.directBuffer(content.readableBytes());
            try {
                buffer.writeBytes(content, content.readerIndex(), content.readableBytes());
                l = ReferenceCountedOpenSslContext.newBIO(buffer.retainedSlice());
            } catch (Throwable throwable) {
                try {
                    if (pem.isSensitive()) {
                        SslUtils.zeroout(buffer);
                    }
                } finally {
                    buffer.release();
                }
                throw throwable;
            }
            try {
                if (pem.isSensitive()) {
                    SslUtils.zeroout(buffer);
                }
            } finally {
                buffer.release();
            }
            return l;
        } finally {
            pem.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static long newBIO(ByteBuf buffer) throws Exception {
        try {
            long bio = SSL.newMemBIO();
            int readable = buffer.readableBytes();
            if (SSL.bioWrite((long)bio, (long)(OpenSsl.memoryAddress((ByteBuf)buffer) + (long)buffer.readerIndex()), (int)readable) != readable) {
                SSL.freeBIO((long)bio);
                throw new IllegalStateException("Could not write data to memory BIO");
            }
            long l = bio;
            return l;
        } finally {
            buffer.release();
        }
    }

    static OpenSslKeyMaterialProvider providerFor(KeyManagerFactory factory, String password) {
        if (factory instanceof OpenSslX509KeyManagerFactory) {
            return ((OpenSslX509KeyManagerFactory)factory).newProvider();
        }
        if (factory instanceof OpenSslCachingX509KeyManagerFactory) {
            return ((OpenSslCachingX509KeyManagerFactory)factory).newProvider(password);
        }
        return new OpenSslKeyMaterialProvider(ReferenceCountedOpenSslContext.chooseX509KeyManager(factory.getKeyManagers()), password);
    }

    private static byte[] verifyResult(byte[] result) throws SignatureException {
        if (result == null) {
            throw new SignatureException();
        }
        return result;
    }

    static {
        leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ReferenceCountedOpenSslContext.class);
        CLIENT_ENABLE_SESSION_TICKET = SystemPropertyUtil.getBoolean("jdk.tls.client.enableSessionTicketExtension", false);
        CLIENT_ENABLE_SESSION_TICKET_TLSV13 = SystemPropertyUtil.getBoolean("jdk.tls.client.enableSessionTicketExtension", true);
        SERVER_ENABLE_SESSION_TICKET = SystemPropertyUtil.getBoolean("jdk.tls.server.enableSessionTicketExtension", false);
        SERVER_ENABLE_SESSION_TICKET_TLSV13 = SystemPropertyUtil.getBoolean("jdk.tls.server.enableSessionTicketExtension", true);
        SERVER_ENABLE_SESSION_CACHE = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.sessionCacheServer", true);
        CLIENT_ENABLE_SESSION_CACHE = SystemPropertyUtil.getBoolean("io.netty.handler.ssl.openssl.sessionCacheClient", false);
        NONE_PROTOCOL_NEGOTIATOR = new OpenSslApplicationProtocolNegotiator(){

            @Override
            public ApplicationProtocolConfig.Protocol protocol() {
                return ApplicationProtocolConfig.Protocol.NONE;
            }

            @Override
            public List<String> protocols() {
                return Collections.emptyList();
            }

            @Override
            public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior() {
                return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
            }

            @Override
            public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
                return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
            }
        };
        Integer dhLen = null;
        try {
            String dhKeySize = SystemPropertyUtil.get("jdk.tls.ephemeralDHKeySize");
            if (dhKeySize != null) {
                try {
                    dhLen = Integer.valueOf(dhKeySize);
                } catch (NumberFormatException e) {
                    logger.debug("ReferenceCountedOpenSslContext supports -Djdk.tls.ephemeralDHKeySize={int}, but got: " + dhKeySize);
                }
            }
        } catch (Throwable throwable) {
            // empty catch block
        }
        DH_KEY_LENGTH = dhLen;
    }

    private static final class AsyncPrivateKeyMethod
    implements AsyncSSLPrivateKeyMethod {
        private final OpenSslEngineMap engineMap;
        private final OpenSslAsyncPrivateKeyMethod keyMethod;

        AsyncPrivateKeyMethod(OpenSslEngineMap engineMap, OpenSslAsyncPrivateKeyMethod keyMethod) {
            this.engineMap = engineMap;
            this.keyMethod = keyMethod;
        }

        private ReferenceCountedOpenSslEngine retrieveEngine(long ssl) throws SSLException {
            ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
            if (engine == null) {
                throw new SSLException("Could not find a " + StringUtil.simpleClassName(ReferenceCountedOpenSslEngine.class) + " for sslPointer " + ssl);
            }
            return engine;
        }

        public void sign(long ssl, int signatureAlgorithm, byte[] bytes, ResultCallback<byte[]> resultCallback) {
            try {
                ReferenceCountedOpenSslEngine engine = this.retrieveEngine(ssl);
                this.keyMethod.sign(engine, signatureAlgorithm, bytes).addListener(new ResultCallbackListener(engine, ssl, resultCallback));
            } catch (SSLException e) {
                resultCallback.onError(ssl, (Throwable)e);
            }
        }

        public void decrypt(long ssl, byte[] bytes, ResultCallback<byte[]> resultCallback) {
            try {
                ReferenceCountedOpenSslEngine engine = this.retrieveEngine(ssl);
                this.keyMethod.decrypt(engine, bytes).addListener(new ResultCallbackListener(engine, ssl, resultCallback));
            } catch (SSLException e) {
                resultCallback.onError(ssl, (Throwable)e);
            }
        }

        private static final class ResultCallbackListener
        implements FutureListener<byte[]> {
            private final ReferenceCountedOpenSslEngine engine;
            private final long ssl;
            private final ResultCallback<byte[]> resultCallback;

            ResultCallbackListener(ReferenceCountedOpenSslEngine engine, long ssl, ResultCallback<byte[]> resultCallback) {
                this.engine = engine;
                this.ssl = ssl;
                this.resultCallback = resultCallback;
            }

            @Override
            public void operationComplete(Future<byte[]> future) {
                Throwable cause = future.cause();
                if (cause == null) {
                    try {
                        byte[] result = ReferenceCountedOpenSslContext.verifyResult(future.getNow());
                        this.resultCallback.onSuccess(this.ssl, (Object)result);
                        return;
                    } catch (SignatureException e) {
                        cause = e;
                        this.engine.initHandshakeException(e);
                    }
                }
                this.resultCallback.onError(this.ssl, cause);
            }
        }
    }

    private static final class PrivateKeyMethod
    implements SSLPrivateKeyMethod {
        private final OpenSslEngineMap engineMap;
        private final OpenSslPrivateKeyMethod keyMethod;

        PrivateKeyMethod(OpenSslEngineMap engineMap, OpenSslPrivateKeyMethod keyMethod) {
            this.engineMap = engineMap;
            this.keyMethod = keyMethod;
        }

        private ReferenceCountedOpenSslEngine retrieveEngine(long ssl) throws SSLException {
            ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
            if (engine == null) {
                throw new SSLException("Could not find a " + StringUtil.simpleClassName(ReferenceCountedOpenSslEngine.class) + " for sslPointer " + ssl);
            }
            return engine;
        }

        public byte[] sign(long ssl, int signatureAlgorithm, byte[] digest) throws Exception {
            ReferenceCountedOpenSslEngine engine = this.retrieveEngine(ssl);
            try {
                return ReferenceCountedOpenSslContext.verifyResult(this.keyMethod.sign(engine, signatureAlgorithm, digest));
            } catch (Exception e) {
                engine.initHandshakeException(e);
                throw e;
            }
        }

        public byte[] decrypt(long ssl, byte[] input) throws Exception {
            ReferenceCountedOpenSslEngine engine = this.retrieveEngine(ssl);
            try {
                return ReferenceCountedOpenSslContext.verifyResult(this.keyMethod.decrypt(engine, input));
            } catch (Exception e) {
                engine.initHandshakeException(e);
                throw e;
            }
        }
    }

    private static final class DefaultOpenSslEngineMap
    implements OpenSslEngineMap {
        private final Map<Long, ReferenceCountedOpenSslEngine> engines = PlatformDependent.newConcurrentHashMap();

        private DefaultOpenSslEngineMap() {
        }

        @Override
        public ReferenceCountedOpenSslEngine remove(long ssl) {
            return this.engines.remove(ssl);
        }

        @Override
        public void add(ReferenceCountedOpenSslEngine engine) {
            this.engines.put(engine.sslPointer(), engine);
        }

        @Override
        public ReferenceCountedOpenSslEngine get(long ssl) {
            return this.engines.get(ssl);
        }
    }

    static abstract class AbstractCertificateVerifier
    extends CertificateVerifier {
        private final OpenSslEngineMap engineMap;

        AbstractCertificateVerifier(OpenSslEngineMap engineMap) {
            this.engineMap = engineMap;
        }

        public final int verify(long ssl, byte[][] chain, String auth) {
            ReferenceCountedOpenSslEngine engine = this.engineMap.get(ssl);
            if (engine == null) {
                return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
            }
            X509Certificate[] peerCerts = ReferenceCountedOpenSslContext.certificates(chain);
            try {
                this.verify(engine, peerCerts, auth);
                return CertificateVerifier.X509_V_OK;
            } catch (Throwable cause) {
                logger.debug("verification of certificate failed", cause);
                engine.initHandshakeException(cause);
                if (cause instanceof OpenSslCertificateException) {
                    return ((OpenSslCertificateException)cause).errorCode();
                }
                if (cause instanceof CertificateExpiredException) {
                    return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED;
                }
                if (cause instanceof CertificateNotYetValidException) {
                    return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID;
                }
                if (PlatformDependent.javaVersion() >= 7) {
                    return AbstractCertificateVerifier.translateToError(cause);
                }
                return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
            }
        }

        @SuppressJava6Requirement(reason="Usage guarded by java version check")
        private static int translateToError(Throwable cause) {
            if (cause instanceof CertificateRevokedException) {
                return CertificateVerifier.X509_V_ERR_CERT_REVOKED;
            }
            for (Throwable wrapped = cause.getCause(); wrapped != null; wrapped = wrapped.getCause()) {
                if (!(wrapped instanceof CertPathValidatorException)) continue;
                CertPathValidatorException ex = (CertPathValidatorException)wrapped;
                CertPathValidatorException.Reason reason = ex.getReason();
                if (reason == CertPathValidatorException.BasicReason.EXPIRED) {
                    return CertificateVerifier.X509_V_ERR_CERT_HAS_EXPIRED;
                }
                if (reason == CertPathValidatorException.BasicReason.NOT_YET_VALID) {
                    return CertificateVerifier.X509_V_ERR_CERT_NOT_YET_VALID;
                }
                if (reason != CertPathValidatorException.BasicReason.REVOKED) continue;
                return CertificateVerifier.X509_V_ERR_CERT_REVOKED;
            }
            return CertificateVerifier.X509_V_ERR_UNSPECIFIED;
        }

        abstract void verify(ReferenceCountedOpenSslEngine var1, X509Certificate[] var2, String var3) throws Exception;
    }
}

