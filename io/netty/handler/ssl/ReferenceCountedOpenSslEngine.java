/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.AsyncTask
 *  io.netty.internal.tcnative.Buffer
 *  io.netty.internal.tcnative.SSL
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolAccessor;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.AsyncRunnable;
import io.netty.handler.ssl.CipherSuiteConverter;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.ExtendedOpenSslSession;
import io.netty.handler.ssl.Java7SslParametersUtils;
import io.netty.handler.ssl.Java8SslUtils;
import io.netty.handler.ssl.NotSslRecordException;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslEngineMap;
import io.netty.handler.ssl.OpenSslKeyMaterial;
import io.netty.handler.ssl.OpenSslSession;
import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslSessionId;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SignatureAlgorithmConverter;
import io.netty.handler.ssl.SslUtils;
import io.netty.handler.ssl.util.LazyJavaxX509Certificate;
import io.netty.handler.ssl.util.LazyX509Certificate;
import io.netty.internal.tcnative.AsyncTask;
import io.netty.internal.tcnative.Buffer;
import io.netty.internal.tcnative.SSL;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.security.cert.X509Certificate;

public class ReferenceCountedOpenSslEngine
extends SSLEngine
implements ReferenceCounted,
ApplicationProtocolAccessor {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountedOpenSslEngine.class);
    private static final ResourceLeakDetector<ReferenceCountedOpenSslEngine> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ReferenceCountedOpenSslEngine.class);
    private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_SSLV2 = 0;
    private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_SSLV3 = 1;
    private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1 = 2;
    private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1_1 = 3;
    private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1_2 = 4;
    private static final int OPENSSL_OP_NO_PROTOCOL_INDEX_TLSv1_3 = 5;
    private static final int[] OPENSSL_OP_NO_PROTOCOLS = new int[]{SSL.SSL_OP_NO_SSLv2, SSL.SSL_OP_NO_SSLv3, SSL.SSL_OP_NO_TLSv1, SSL.SSL_OP_NO_TLSv1_1, SSL.SSL_OP_NO_TLSv1_2, SSL.SSL_OP_NO_TLSv1_3};
    static final int MAX_PLAINTEXT_LENGTH = SSL.SSL_MAX_PLAINTEXT_LENGTH;
    static final int MAX_RECORD_SIZE = SSL.SSL_MAX_RECORD_LENGTH;
    private static final SSLEngineResult NEED_UNWRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
    private static final SSLEngineResult NEED_UNWRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
    private static final SSLEngineResult NEED_WRAP_OK = new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
    private static final SSLEngineResult NEED_WRAP_CLOSED = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
    private static final SSLEngineResult CLOSED_NOT_HANDSHAKING = new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
    private long ssl;
    private long networkBIO;
    private HandshakeState handshakeState = HandshakeState.NOT_STARTED;
    private boolean receivedShutdown;
    private volatile boolean destroyed;
    private volatile String applicationProtocol;
    private volatile boolean needTask;
    private String[] explicitlyEnabledProtocols;
    private boolean sessionSet;
    private final ResourceLeakTracker<ReferenceCountedOpenSslEngine> leak;
    private final AbstractReferenceCounted refCnt = new AbstractReferenceCounted(){

        public ReferenceCounted touch(Object hint) {
            if (ReferenceCountedOpenSslEngine.this.leak != null) {
                ReferenceCountedOpenSslEngine.this.leak.record(hint);
            }
            return ReferenceCountedOpenSslEngine.this;
        }

        @Override
        protected void deallocate() {
            ReferenceCountedOpenSslEngine.this.shutdown();
            if (ReferenceCountedOpenSslEngine.this.leak != null) {
                boolean closed = ReferenceCountedOpenSslEngine.this.leak.close(ReferenceCountedOpenSslEngine.this);
                assert (closed);
            }
            ReferenceCountedOpenSslEngine.this.parentContext.release();
        }
    };
    private volatile ClientAuth clientAuth = ClientAuth.NONE;
    private volatile long lastAccessed = -1L;
    private String endPointIdentificationAlgorithm;
    private Object algorithmConstraints;
    private List<String> sniHostNames;
    private volatile Collection<?> matchers;
    private boolean isInboundDone;
    private boolean outboundClosed;
    final boolean jdkCompatibilityMode;
    private final boolean clientMode;
    final ByteBufAllocator alloc;
    private final OpenSslEngineMap engineMap;
    private final OpenSslApplicationProtocolNegotiator apn;
    private final ReferenceCountedOpenSslContext parentContext;
    private final OpenSslSession session;
    private final ByteBuffer[] singleSrcBuffer = new ByteBuffer[1];
    private final ByteBuffer[] singleDstBuffer = new ByteBuffer[1];
    private final boolean enableOcsp;
    private int maxWrapOverhead;
    private int maxWrapBufferSize;
    private Throwable pendingException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ReferenceCountedOpenSslEngine(ReferenceCountedOpenSslContext context, ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode, boolean leakDetection) {
        super(peerHost, peerPort);
        long finalSsl;
        OpenSsl.ensureAvailability();
        this.alloc = ObjectUtil.checkNotNull(alloc, "alloc");
        this.apn = (OpenSslApplicationProtocolNegotiator)context.applicationProtocolNegotiator();
        this.clientMode = context.isClient();
        this.session = PlatformDependent.javaVersion() >= 7 ? new ExtendedOpenSslSession(new DefaultOpenSslSession(context.sessionContext())){
            private String[] peerSupportedSignatureAlgorithms;
            private List requestedServerNames;

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public List getRequestedServerNames() {
                if (ReferenceCountedOpenSslEngine.this.clientMode) {
                    return Java8SslUtils.getSniHostNames(ReferenceCountedOpenSslEngine.this.sniHostNames);
                }
                ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
                synchronized (referenceCountedOpenSslEngine) {
                    if (this.requestedServerNames == null) {
                        String name;
                        this.requestedServerNames = ReferenceCountedOpenSslEngine.this.isDestroyed() ? Collections.emptyList() : ((name = SSL.getSniHostname((long)ReferenceCountedOpenSslEngine.this.ssl)) == null ? Collections.emptyList() : Java8SslUtils.getSniHostName(SSL.getSniHostname((long)ReferenceCountedOpenSslEngine.this.ssl).getBytes(CharsetUtil.UTF_8)));
                    }
                    return this.requestedServerNames;
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public String[] getPeerSupportedSignatureAlgorithms() {
                ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
                synchronized (referenceCountedOpenSslEngine) {
                    if (this.peerSupportedSignatureAlgorithms == null) {
                        if (ReferenceCountedOpenSslEngine.this.isDestroyed()) {
                            this.peerSupportedSignatureAlgorithms = EmptyArrays.EMPTY_STRINGS;
                        } else {
                            String[] algs = SSL.getSigAlgs((long)ReferenceCountedOpenSslEngine.this.ssl);
                            if (algs == null) {
                                this.peerSupportedSignatureAlgorithms = EmptyArrays.EMPTY_STRINGS;
                            } else {
                                LinkedHashSet<String> algorithmList = new LinkedHashSet<String>(algs.length);
                                for (String alg : algs) {
                                    String converted = SignatureAlgorithmConverter.toJavaName(alg);
                                    if (converted == null) continue;
                                    algorithmList.add(converted);
                                }
                                this.peerSupportedSignatureAlgorithms = algorithmList.toArray(new String[0]);
                            }
                        }
                    }
                    return (String[])this.peerSupportedSignatureAlgorithms.clone();
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public List<byte[]> getStatusResponses() {
                byte[] ocspResponse = null;
                if (ReferenceCountedOpenSslEngine.this.enableOcsp && ReferenceCountedOpenSslEngine.this.clientMode) {
                    ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
                    synchronized (referenceCountedOpenSslEngine) {
                        if (!ReferenceCountedOpenSslEngine.this.isDestroyed()) {
                            ocspResponse = SSL.getOcspResponse((long)ReferenceCountedOpenSslEngine.this.ssl);
                        }
                    }
                }
                return ocspResponse == null ? Collections.emptyList() : Collections.singletonList(ocspResponse);
            }
        } : new DefaultOpenSslSession(context.sessionContext());
        this.engineMap = context.engineMap;
        this.enableOcsp = context.enableOcsp;
        if (!context.sessionContext().useKeyManager()) {
            this.session.setLocalCertificate(context.keyCertChain);
        }
        this.jdkCompatibilityMode = jdkCompatibilityMode;
        Lock readerLock = context.ctxLock.readLock();
        readerLock.lock();
        try {
            finalSsl = SSL.newSSL((long)context.ctx, (!context.isClient() ? (char)'\u0001' : '\u0000') != '\u0000');
        } finally {
            readerLock.unlock();
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            this.ssl = finalSsl;
            try {
                this.networkBIO = SSL.bioNewByteBuffer((long)this.ssl, (int)context.getBioNonApplicationBufferSize());
                this.setClientAuth(this.clientMode ? ClientAuth.NONE : context.clientAuth);
                if (context.protocols != null) {
                    this.setEnabledProtocols0(context.protocols, true);
                } else {
                    this.explicitlyEnabledProtocols = this.getEnabledProtocols();
                }
                if (this.clientMode && SslUtils.isValidHostNameForSNI(peerHost)) {
                    SSL.setTlsExtHostName((long)this.ssl, (String)peerHost);
                    this.sniHostNames = Collections.singletonList(peerHost);
                }
                if (this.enableOcsp) {
                    SSL.enableOcsp((long)this.ssl);
                }
                if (!jdkCompatibilityMode) {
                    SSL.setMode((long)this.ssl, (int)(SSL.getMode((long)this.ssl) | SSL.SSL_MODE_ENABLE_PARTIAL_WRITE));
                }
                if (ReferenceCountedOpenSslEngine.isProtocolEnabled(SSL.getOptions((long)this.ssl), SSL.SSL_OP_NO_TLSv1_3, "TLSv1.3")) {
                    boolean enableTickets;
                    boolean bl = enableTickets = this.clientMode ? ReferenceCountedOpenSslContext.CLIENT_ENABLE_SESSION_TICKET_TLSV13 : ReferenceCountedOpenSslContext.SERVER_ENABLE_SESSION_TICKET_TLSV13;
                    if (enableTickets) {
                        SSL.clearOptions((long)this.ssl, (int)SSL.SSL_OP_NO_TICKET);
                    }
                }
                this.calculateMaxWrapOverhead();
            } catch (Throwable cause) {
                this.shutdown();
                PlatformDependent.throwException(cause);
            }
        }
        this.parentContext = context;
        this.parentContext.retain();
        this.leak = leakDetection ? leakDetector.track(this) : null;
    }

    final synchronized String[] authMethods() {
        if (this.isDestroyed()) {
            return EmptyArrays.EMPTY_STRINGS;
        }
        return SSL.authenticationMethods((long)this.ssl);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final boolean setKeyMaterial(OpenSslKeyMaterial keyMaterial) throws Exception {
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (this.isDestroyed()) {
                return false;
            }
            SSL.setKeyMaterial((long)this.ssl, (long)keyMaterial.certificateChainAddress(), (long)keyMaterial.privateKeyAddress());
        }
        this.session.setLocalCertificate(keyMaterial.certificateChain());
        return true;
    }

    final synchronized SecretKeySpec masterKey() {
        if (this.isDestroyed()) {
            return null;
        }
        return new SecretKeySpec(SSL.getMasterKey((long)this.ssl), "AES");
    }

    synchronized boolean isSessionReused() {
        if (this.isDestroyed()) {
            return false;
        }
        return SSL.isSessionReused((long)this.ssl);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setOcspResponse(byte[] response) {
        if (!this.enableOcsp) {
            throw new IllegalStateException("OCSP stapling is not enabled");
        }
        if (this.clientMode) {
            throw new IllegalStateException("Not a server SSLEngine");
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (!this.isDestroyed()) {
                SSL.setOcspResponse((long)this.ssl, (byte[])response);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] getOcspResponse() {
        if (!this.enableOcsp) {
            throw new IllegalStateException("OCSP stapling is not enabled");
        }
        if (!this.clientMode) {
            throw new IllegalStateException("Not a client SSLEngine");
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (this.isDestroyed()) {
                return EmptyArrays.EMPTY_BYTES;
            }
            return SSL.getOcspResponse((long)this.ssl);
        }
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

    @Override
    public String getApplicationProtocol() {
        return this.applicationProtocol;
    }

    @Override
    public String getHandshakeApplicationProtocol() {
        return this.applicationProtocol;
    }

    @Override
    public final synchronized SSLSession getHandshakeSession() {
        switch (this.handshakeState) {
            case NOT_STARTED: 
            case FINISHED: {
                return null;
            }
        }
        return this.session;
    }

    public final synchronized long sslPointer() {
        return this.ssl;
    }

    public final synchronized void shutdown() {
        if (!this.destroyed) {
            this.destroyed = true;
            this.engineMap.remove(this.ssl);
            SSL.freeSSL((long)this.ssl);
            this.networkBIO = 0L;
            this.ssl = 0L;
            this.outboundClosed = true;
            this.isInboundDone = true;
        }
        SSL.clearError();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int writePlaintextData(ByteBuffer src, int len) {
        int sslWrote;
        int pos = src.position();
        int limit = src.limit();
        if (src.isDirect()) {
            sslWrote = SSL.writeToSSL((long)this.ssl, (long)(ReferenceCountedOpenSslEngine.bufferAddress(src) + (long)pos), (int)len);
            if (sslWrote > 0) {
                src.position(pos + sslWrote);
            }
        } else {
            ByteBuf buf = this.alloc.directBuffer(len);
            try {
                src.limit(pos + len);
                buf.setBytes(0, src);
                src.limit(limit);
                sslWrote = SSL.writeToSSL((long)this.ssl, (long)OpenSsl.memoryAddress((ByteBuf)buf), (int)len);
                if (sslWrote > 0) {
                    src.position(pos + sslWrote);
                } else {
                    src.position(pos);
                }
            } finally {
                buf.release();
            }
        }
        return sslWrote;
    }

    private ByteBuf writeEncryptedData(ByteBuffer src, int len) throws SSLException {
        int pos = src.position();
        if (src.isDirect()) {
            SSL.bioSetByteBuffer((long)this.networkBIO, (long)(ReferenceCountedOpenSslEngine.bufferAddress(src) + (long)pos), (int)len, (boolean)false);
        } else {
            ByteBuf buf = this.alloc.directBuffer(len);
            try {
                int limit = src.limit();
                src.limit(pos + len);
                buf.writeBytes(src);
                src.position(pos);
                src.limit(limit);
                SSL.bioSetByteBuffer((long)this.networkBIO, (long)OpenSsl.memoryAddress((ByteBuf)buf), (int)len, (boolean)false);
                return buf;
            } catch (Throwable cause) {
                buf.release();
                PlatformDependent.throwException(cause);
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int readPlaintextData(ByteBuffer dst) throws SSLException {
        int sslRead;
        int pos = dst.position();
        if (dst.isDirect()) {
            sslRead = SSL.readFromSSL((long)this.ssl, (long)(ReferenceCountedOpenSslEngine.bufferAddress(dst) + (long)pos), (int)(dst.limit() - pos));
            if (sslRead > 0) {
                dst.position(pos + sslRead);
            }
        } else {
            int limit = dst.limit();
            int len = Math.min(this.maxEncryptedPacketLength0(), limit - pos);
            ByteBuf buf = this.alloc.directBuffer(len);
            try {
                sslRead = SSL.readFromSSL((long)this.ssl, (long)OpenSsl.memoryAddress((ByteBuf)buf), (int)len);
                if (sslRead > 0) {
                    dst.limit(pos + sslRead);
                    buf.getBytes(buf.readerIndex(), dst);
                    dst.limit(limit);
                }
            } finally {
                buf.release();
            }
        }
        return sslRead;
    }

    final synchronized int maxWrapOverhead() {
        return this.maxWrapOverhead;
    }

    final synchronized int maxEncryptedPacketLength() {
        return this.maxEncryptedPacketLength0();
    }

    final int maxEncryptedPacketLength0() {
        return this.maxWrapOverhead + MAX_PLAINTEXT_LENGTH;
    }

    final int calculateMaxLengthForWrap(int plaintextLength, int numComponents) {
        return (int)Math.min((long)this.maxWrapBufferSize, (long)plaintextLength + (long)this.maxWrapOverhead * (long)numComponents);
    }

    final synchronized int sslPending() {
        return this.sslPending0();
    }

    private void calculateMaxWrapOverhead() {
        this.maxWrapOverhead = SSL.getMaxWrapOverhead((long)this.ssl);
        this.maxWrapBufferSize = this.jdkCompatibilityMode ? this.maxEncryptedPacketLength0() : this.maxEncryptedPacketLength0() << 4;
    }

    private int sslPending0() {
        return this.handshakeState != HandshakeState.FINISHED ? 0 : SSL.sslPending((long)this.ssl);
    }

    private boolean isBytesAvailableEnoughForWrap(int bytesAvailable, int plaintextLength, int numComponents) {
        return (long)bytesAvailable - (long)this.maxWrapOverhead * (long)numComponents >= (long)plaintextLength;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public final SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst) throws SSLException {
        ObjectUtil.checkNotNullWithIAE(srcs, "srcs");
        ObjectUtil.checkNotNullWithIAE(dst, "dst");
        if (offset >= srcs.length || offset + length > srcs.length) {
            throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
        }
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (this.isOutboundDone()) {
                return this.isInboundDone() || this.isDestroyed() ? CLOSED_NOT_HANDSHAKING : NEED_UNWRAP_CLOSED;
            }
            int bytesProduced = 0;
            ByteBuf bioReadCopyBuf = null;
            try {
                if (dst.isDirect()) {
                    SSL.bioSetByteBuffer((long)this.networkBIO, (long)(ReferenceCountedOpenSslEngine.bufferAddress(dst) + (long)dst.position()), (int)dst.remaining(), (boolean)true);
                } else {
                    bioReadCopyBuf = this.alloc.directBuffer(dst.remaining());
                    SSL.bioSetByteBuffer((long)this.networkBIO, (long)OpenSsl.memoryAddress((ByteBuf)bioReadCopyBuf), (int)bioReadCopyBuf.writableBytes(), (boolean)true);
                }
                int bioLengthBefore = SSL.bioLengthByteBuffer((long)this.networkBIO);
                if (this.outboundClosed) {
                    if (!this.isBytesAvailableEnoughForWrap(dst.remaining(), 2, 1)) {
                        SSLEngineResult sSLEngineResult = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.getHandshakeStatus(), 0, 0);
                        return sSLEngineResult;
                    }
                    bytesProduced = SSL.bioFlushByteBuffer((long)this.networkBIO);
                    if (bytesProduced <= 0) {
                        SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
                        return sSLEngineResult;
                    }
                    if (!this.doSSLShutdown()) {
                        SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, bytesProduced);
                        return sSLEngineResult;
                    }
                    bytesProduced = bioLengthBefore - SSL.bioLengthByteBuffer((long)this.networkBIO);
                    SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, bytesProduced);
                    return sSLEngineResult;
                }
                SSLEngineResult.HandshakeStatus status = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
                if (this.handshakeState != HandshakeState.FINISHED) {
                    if (this.handshakeState != HandshakeState.STARTED_EXPLICITLY) {
                        this.handshakeState = HandshakeState.STARTED_IMPLICITLY;
                    }
                    bytesProduced = SSL.bioFlushByteBuffer((long)this.networkBIO);
                    if (this.pendingException != null) {
                        if (bytesProduced > 0) {
                            SSLEngineResult sSLEngineResult = this.newResult(SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, bytesProduced);
                            return sSLEngineResult;
                        }
                        SSLEngineResult sSLEngineResult = this.newResult(this.handshakeException(), 0, 0);
                        return sSLEngineResult;
                    }
                    status = this.handshake();
                    bytesProduced = bioLengthBefore - SSL.bioLengthByteBuffer((long)this.networkBIO);
                    if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                        SSLEngineResult sSLEngineResult = this.newResult(status, 0, bytesProduced);
                        return sSLEngineResult;
                    }
                    if (bytesProduced > 0) {
                        SSLEngineResult sSLEngineResult = this.newResult(this.mayFinishHandshake(status != SSLEngineResult.HandshakeStatus.FINISHED ? (bytesProduced == bioLengthBefore ? SSLEngineResult.HandshakeStatus.NEED_WRAP : this.getHandshakeStatus(SSL.bioLengthNonApplication((long)this.networkBIO))) : SSLEngineResult.HandshakeStatus.FINISHED), 0, bytesProduced);
                        return sSLEngineResult;
                    }
                    if (status == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                        SSLEngineResult sSLEngineResult = this.isOutboundDone() ? NEED_UNWRAP_CLOSED : NEED_UNWRAP_OK;
                        return sSLEngineResult;
                    }
                    if (this.outboundClosed) {
                        bytesProduced = SSL.bioFlushByteBuffer((long)this.networkBIO);
                        SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(status, 0, bytesProduced);
                        return sSLEngineResult;
                    }
                }
                int endOffset = offset + length;
                if (this.jdkCompatibilityMode) {
                    int srcsLen = 0;
                    for (int i = offset; i < endOffset; ++i) {
                        ByteBuffer src = srcs[i];
                        if (src == null) {
                            throw new IllegalArgumentException("srcs[" + i + "] is null");
                        }
                        if (srcsLen == MAX_PLAINTEXT_LENGTH || (srcsLen += src.remaining()) <= MAX_PLAINTEXT_LENGTH && srcsLen >= 0) continue;
                        srcsLen = MAX_PLAINTEXT_LENGTH;
                    }
                    if (!this.isBytesAvailableEnoughForWrap(dst.remaining(), srcsLen, 1)) {
                        SSLEngineResult i = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.getHandshakeStatus(), 0, 0);
                        return i;
                    }
                }
                int bytesConsumed = 0;
                assert (bytesProduced == 0);
                bytesProduced = SSL.bioFlushByteBuffer((long)this.networkBIO);
                if (bytesProduced > 0) {
                    SSLEngineResult i = this.newResultMayFinishHandshake(status, bytesConsumed, bytesProduced);
                    return i;
                }
                if (this.pendingException != null) {
                    Throwable error = this.pendingException;
                    this.pendingException = null;
                    this.shutdown();
                    throw new SSLException(error);
                }
                while (offset < endOffset) {
                    ByteBuffer src = srcs[offset];
                    int remaining = src.remaining();
                    if (remaining != 0) {
                        int bytesWritten;
                        if (this.jdkCompatibilityMode) {
                            bytesWritten = this.writePlaintextData(src, Math.min(remaining, MAX_PLAINTEXT_LENGTH - bytesConsumed));
                        } else {
                            int availableCapacityForWrap = dst.remaining() - bytesProduced - this.maxWrapOverhead;
                            if (availableCapacityForWrap <= 0) {
                                SSLEngineResult sSLEngineResult = new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.getHandshakeStatus(), bytesConsumed, bytesProduced);
                                return sSLEngineResult;
                            }
                            bytesWritten = this.writePlaintextData(src, Math.min(remaining, availableCapacityForWrap));
                        }
                        int pendingNow = SSL.bioLengthByteBuffer((long)this.networkBIO);
                        bytesProduced += bioLengthBefore - pendingNow;
                        bioLengthBefore = pendingNow;
                        if (bytesWritten > 0) {
                            bytesConsumed += bytesWritten;
                            if (this.jdkCompatibilityMode || bytesProduced == dst.remaining()) {
                                SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(status, bytesConsumed, bytesProduced);
                                return sSLEngineResult;
                            }
                        } else {
                            int sslError = SSL.getError((long)this.ssl, (int)bytesWritten);
                            if (sslError == SSL.SSL_ERROR_ZERO_RETURN) {
                                if (!this.receivedShutdown) {
                                    this.closeAll();
                                    SSLEngineResult.HandshakeStatus hs = this.mayFinishHandshake(status != SSLEngineResult.HandshakeStatus.FINISHED ? ((bytesProduced += bioLengthBefore - SSL.bioLengthByteBuffer((long)this.networkBIO)) == dst.remaining() ? SSLEngineResult.HandshakeStatus.NEED_WRAP : this.getHandshakeStatus(SSL.bioLengthNonApplication((long)this.networkBIO))) : SSLEngineResult.HandshakeStatus.FINISHED);
                                    SSLEngineResult sSLEngineResult = this.newResult(hs, bytesConsumed, bytesProduced);
                                    return sSLEngineResult;
                                }
                                SSLEngineResult sSLEngineResult = this.newResult(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, bytesConsumed, bytesProduced);
                                return sSLEngineResult;
                            }
                            if (sslError == SSL.SSL_ERROR_WANT_READ) {
                                SSLEngineResult sSLEngineResult = this.newResult(SSLEngineResult.HandshakeStatus.NEED_UNWRAP, bytesConsumed, bytesProduced);
                                return sSLEngineResult;
                            }
                            if (sslError == SSL.SSL_ERROR_WANT_WRITE) {
                                if (bytesProduced > 0) {
                                    SSLEngineResult sSLEngineResult = this.newResult(SSLEngineResult.HandshakeStatus.NEED_WRAP, bytesConsumed, bytesProduced);
                                    return sSLEngineResult;
                                }
                                SSLEngineResult sSLEngineResult = this.newResult(SSLEngineResult.Status.BUFFER_OVERFLOW, status, bytesConsumed, bytesProduced);
                                return sSLEngineResult;
                            }
                            if (sslError != SSL.SSL_ERROR_WANT_X509_LOOKUP && sslError != SSL.SSL_ERROR_WANT_CERTIFICATE_VERIFY && sslError != SSL.SSL_ERROR_WANT_PRIVATE_KEY_OPERATION) {
                                throw this.shutdownWithError("SSL_write", sslError);
                            }
                            SSLEngineResult sSLEngineResult = this.newResult(SSLEngineResult.HandshakeStatus.NEED_TASK, bytesConsumed, bytesProduced);
                            return sSLEngineResult;
                        }
                    }
                    ++offset;
                }
                SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(status, bytesConsumed, bytesProduced);
                return sSLEngineResult;
            } finally {
                SSL.bioClearByteBuffer((long)this.networkBIO);
                if (bioReadCopyBuf == null) {
                    dst.position(dst.position() + bytesProduced);
                } else {
                    assert (bioReadCopyBuf.readableBytes() <= dst.remaining()) : "The destination buffer " + dst + " didn't have enough remaining space to hold the encrypted content in " + bioReadCopyBuf;
                    dst.put(bioReadCopyBuf.internalNioBuffer(bioReadCopyBuf.readerIndex(), bytesProduced));
                    bioReadCopyBuf.release();
                }
            }
        }
    }

    private SSLEngineResult newResult(SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced) {
        return this.newResult(SSLEngineResult.Status.OK, hs, bytesConsumed, bytesProduced);
    }

    private SSLEngineResult newResult(SSLEngineResult.Status status, SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced) {
        if (this.isOutboundDone()) {
            if (this.isInboundDone()) {
                hs = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
                this.shutdown();
            }
            return new SSLEngineResult(SSLEngineResult.Status.CLOSED, hs, bytesConsumed, bytesProduced);
        }
        if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            this.needTask = true;
        }
        return new SSLEngineResult(status, hs, bytesConsumed, bytesProduced);
    }

    private SSLEngineResult newResultMayFinishHandshake(SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced) throws SSLException {
        return this.newResult(this.mayFinishHandshake(hs, bytesConsumed, bytesProduced), bytesConsumed, bytesProduced);
    }

    private SSLEngineResult newResultMayFinishHandshake(SSLEngineResult.Status status, SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced) throws SSLException {
        return this.newResult(status, this.mayFinishHandshake(hs, bytesConsumed, bytesProduced), bytesConsumed, bytesProduced);
    }

    private SSLException shutdownWithError(String operations, int sslError) {
        return this.shutdownWithError(operations, sslError, SSL.getLastErrorNumber());
    }

    private SSLException shutdownWithError(String operation, int sslError, int error) {
        String errorString = SSL.getErrorString((long)error);
        if (logger.isDebugEnabled()) {
            logger.debug("{} failed with {}: OpenSSL error: {} {}", operation, sslError, error, errorString);
        }
        this.shutdown();
        if (this.handshakeState == HandshakeState.FINISHED) {
            return new SSLException(errorString);
        }
        SSLHandshakeException exception = new SSLHandshakeException(errorString);
        if (this.pendingException != null) {
            exception.initCause(this.pendingException);
            this.pendingException = null;
        }
        return exception;
    }

    private SSLEngineResult handleUnwrapException(int bytesConsumed, int bytesProduced, SSLException e) throws SSLException {
        int lastError = SSL.getLastErrorNumber();
        if (lastError != 0) {
            return this.sslReadErrorResult(SSL.SSL_ERROR_SSL, lastError, bytesConsumed, bytesProduced);
        }
        throw e;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public final SSLEngineResult unwrap(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws SSLException {
        ByteBuffer src;
        ByteBuffer dst;
        ObjectUtil.checkNotNullWithIAE(srcs, "srcs");
        if (srcsOffset >= srcs.length || srcsOffset + srcsLength > srcs.length) {
            throw new IndexOutOfBoundsException("offset: " + srcsOffset + ", length: " + srcsLength + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
        }
        ObjectUtil.checkNotNullWithIAE(dsts, "dsts");
        if (dstsOffset >= dsts.length || dstsOffset + dstsLength > dsts.length) {
            throw new IndexOutOfBoundsException("offset: " + dstsOffset + ", length: " + dstsLength + " (expected: offset <= offset + length <= dsts.length (" + dsts.length + "))");
        }
        long capacity = 0L;
        int dstsEndOffset = dstsOffset + dstsLength;
        for (int i = dstsOffset; i < dstsEndOffset; capacity += (long)dst.remaining(), ++i) {
            dst = ObjectUtil.checkNotNullArrayParam(dsts[i], i, "dsts");
            if (!dst.isReadOnly()) continue;
            throw new ReadOnlyBufferException();
        }
        int srcsEndOffset = srcsOffset + srcsLength;
        long len = 0L;
        for (int i = srcsOffset; i < srcsEndOffset; len += (long)src.remaining(), ++i) {
            src = ObjectUtil.checkNotNullArrayParam(srcs[i], i, "srcs");
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            int bytesConsumed;
            int bytesProduced;
            SSLEngineResult.HandshakeStatus status;
            block59: {
                int packetLength;
                if (this.isInboundDone()) {
                    return this.isOutboundDone() || this.isDestroyed() ? CLOSED_NOT_HANDSHAKING : NEED_WRAP_CLOSED;
                }
                status = SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
                if (this.handshakeState != HandshakeState.FINISHED) {
                    if (this.handshakeState != HandshakeState.STARTED_EXPLICITLY) {
                        this.handshakeState = HandshakeState.STARTED_IMPLICITLY;
                    }
                    if ((status = this.handshake()) == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                        return this.newResult(status, 0, 0);
                    }
                    if (status == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                        return NEED_WRAP_OK;
                    }
                    if (this.isInboundDone) {
                        return NEED_WRAP_CLOSED;
                    }
                }
                int sslPending = this.sslPending0();
                if (this.jdkCompatibilityMode) {
                    if (len < 5L) {
                        return this.newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_UNDERFLOW, status, 0, 0);
                    }
                    packetLength = SslUtils.getEncryptedPacketLength(srcs, srcsOffset);
                    if (packetLength == -2) {
                        throw new NotSslRecordException("not an SSL/TLS record");
                    }
                    int packetLengthDataOnly = packetLength - 5;
                    if ((long)packetLengthDataOnly > capacity) {
                        if (packetLengthDataOnly > MAX_RECORD_SIZE) {
                            throw new SSLException("Illegal packet length: " + packetLengthDataOnly + " > " + this.session.getApplicationBufferSize());
                        }
                        this.session.tryExpandApplicationBufferSize(packetLengthDataOnly);
                        return this.newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_OVERFLOW, status, 0, 0);
                    }
                    if (len < (long)packetLength) {
                        return this.newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_UNDERFLOW, status, 0, 0);
                    }
                } else {
                    if (len == 0L && sslPending <= 0) {
                        return this.newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_UNDERFLOW, status, 0, 0);
                    }
                    if (capacity == 0L) {
                        return this.newResultMayFinishHandshake(SSLEngineResult.Status.BUFFER_OVERFLOW, status, 0, 0);
                    }
                    packetLength = (int)Math.min(Integer.MAX_VALUE, len);
                }
                assert (srcsOffset < srcsEndOffset);
                assert (capacity > 0L);
                bytesProduced = 0;
                bytesConsumed = 0;
                try {
                    while (true) {
                        int pendingEncryptedBytes;
                        ByteBuf bioWriteCopyBuf;
                        ByteBuffer src2;
                        int remaining;
                        if ((remaining = (src2 = srcs[srcsOffset]).remaining()) == 0) {
                            if (sslPending <= 0) {
                                if (++srcsOffset < srcsEndOffset) continue;
                                break;
                            }
                            bioWriteCopyBuf = null;
                            pendingEncryptedBytes = SSL.bioLengthByteBuffer((long)this.networkBIO);
                        } else {
                            pendingEncryptedBytes = Math.min(packetLength, remaining);
                            try {
                                bioWriteCopyBuf = this.writeEncryptedData(src2, pendingEncryptedBytes);
                            } catch (SSLException e) {
                                SSLEngineResult sSLEngineResult = this.handleUnwrapException(bytesConsumed, bytesProduced, e);
                                SSL.bioClearByteBuffer((long)this.networkBIO);
                                this.rejectRemoteInitiatedRenegotiation();
                                return sSLEngineResult;
                            }
                        }
                        try {
                            int bytesRead;
                            block60: {
                                while (true) {
                                    ByteBuffer dst2;
                                    if (!(dst2 = dsts[dstsOffset]).hasRemaining()) {
                                        if (++dstsOffset < dstsEndOffset) continue;
                                        break block59;
                                    }
                                    try {
                                        bytesRead = this.readPlaintextData(dst2);
                                    } catch (SSLException e) {
                                        SSLEngineResult sSLEngineResult = this.handleUnwrapException(bytesConsumed, bytesProduced, e);
                                        if (bioWriteCopyBuf != null) {
                                            bioWriteCopyBuf.release();
                                        }
                                        SSL.bioClearByteBuffer((long)this.networkBIO);
                                        this.rejectRemoteInitiatedRenegotiation();
                                        return sSLEngineResult;
                                    }
                                    int localBytesConsumed = pendingEncryptedBytes - SSL.bioLengthByteBuffer((long)this.networkBIO);
                                    bytesConsumed += localBytesConsumed;
                                    packetLength -= localBytesConsumed;
                                    pendingEncryptedBytes -= localBytesConsumed;
                                    src2.position(src2.position() + localBytesConsumed);
                                    if (bytesRead <= 0) break block60;
                                    bytesProduced += bytesRead;
                                    if (!dst2.hasRemaining()) {
                                        sslPending = this.sslPending0();
                                        if (++dstsOffset < dstsEndOffset) continue;
                                        SSLEngineResult sSLEngineResult = sslPending > 0 ? this.newResult(SSLEngineResult.Status.BUFFER_OVERFLOW, status, bytesConsumed, bytesProduced) : this.newResultMayFinishHandshake(this.isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, status, bytesConsumed, bytesProduced);
                                        return sSLEngineResult;
                                    }
                                    if (packetLength == 0 || this.jdkCompatibilityMode) break;
                                }
                                break;
                            }
                            int sslError = SSL.getError((long)this.ssl, (int)bytesRead);
                            if (sslError != SSL.SSL_ERROR_WANT_READ && sslError != SSL.SSL_ERROR_WANT_WRITE) {
                                if (sslError == SSL.SSL_ERROR_ZERO_RETURN) {
                                    if (!this.receivedShutdown) {
                                        this.closeAll();
                                    }
                                    SSLEngineResult sSLEngineResult = this.newResultMayFinishHandshake(this.isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, status, bytesConsumed, bytesProduced);
                                    return sSLEngineResult;
                                }
                                if (sslError == SSL.SSL_ERROR_WANT_X509_LOOKUP || sslError == SSL.SSL_ERROR_WANT_CERTIFICATE_VERIFY || sslError == SSL.SSL_ERROR_WANT_PRIVATE_KEY_OPERATION) {
                                    SSLEngineResult sSLEngineResult = this.newResult(this.isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_TASK, bytesConsumed, bytesProduced);
                                    return sSLEngineResult;
                                }
                                SSLEngineResult sSLEngineResult = this.sslReadErrorResult(sslError, SSL.getLastErrorNumber(), bytesConsumed, bytesProduced);
                                return sSLEngineResult;
                            }
                            if (++srcsOffset < srcsEndOffset) continue;
                        } finally {
                            if (bioWriteCopyBuf == null) continue;
                            bioWriteCopyBuf.release();
                            continue;
                        }
                        break;
                    }
                } finally {
                    SSL.bioClearByteBuffer((long)this.networkBIO);
                    this.rejectRemoteInitiatedRenegotiation();
                }
            }
            if (!this.receivedShutdown && (SSL.getShutdown((long)this.ssl) & SSL.SSL_RECEIVED_SHUTDOWN) == SSL.SSL_RECEIVED_SHUTDOWN) {
                this.closeAll();
            }
            return this.newResultMayFinishHandshake(this.isInboundDone() ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK, status, bytesConsumed, bytesProduced);
        }
    }

    private boolean needWrapAgain(int stackError) {
        if (SSL.bioLengthNonApplication((long)this.networkBIO) > 0) {
            SSLException exception;
            String message = SSL.getErrorString((long)stackError);
            SSLException sSLException = exception = this.handshakeState == HandshakeState.FINISHED ? new SSLException(message) : new SSLHandshakeException(message);
            if (this.pendingException == null) {
                this.pendingException = exception;
            } else {
                ThrowableUtil.addSuppressed(this.pendingException, exception);
            }
            SSL.clearError();
            return true;
        }
        return false;
    }

    private SSLEngineResult sslReadErrorResult(int error, int stackError, int bytesConsumed, int bytesProduced) throws SSLException {
        if (this.needWrapAgain(stackError)) {
            return new SSLEngineResult(SSLEngineResult.Status.OK, SSLEngineResult.HandshakeStatus.NEED_WRAP, bytesConsumed, bytesProduced);
        }
        throw this.shutdownWithError("SSL_read", error, stackError);
    }

    private void closeAll() throws SSLException {
        this.receivedShutdown = true;
        this.closeOutbound();
        this.closeInbound();
    }

    private void rejectRemoteInitiatedRenegotiation() throws SSLHandshakeException {
        if (!this.isDestroyed() && SSL.getHandshakeCount((long)this.ssl) > 1 && !"TLSv1.3".equals(this.session.getProtocol()) && this.handshakeState == HandshakeState.FINISHED) {
            this.shutdown();
            throw new SSLHandshakeException("remote-initiated renegotiation not allowed");
        }
    }

    public final SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dsts) throws SSLException {
        return this.unwrap(srcs, 0, srcs.length, dsts, 0, dsts.length);
    }

    private ByteBuffer[] singleSrcBuffer(ByteBuffer src) {
        this.singleSrcBuffer[0] = src;
        return this.singleSrcBuffer;
    }

    private void resetSingleSrcBuffer() {
        this.singleSrcBuffer[0] = null;
    }

    private ByteBuffer[] singleDstBuffer(ByteBuffer src) {
        this.singleDstBuffer[0] = src;
        return this.singleDstBuffer;
    }

    private void resetSingleDstBuffer() {
        this.singleDstBuffer[0] = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
        try {
            SSLEngineResult sSLEngineResult = this.unwrap(this.singleSrcBuffer(src), 0, 1, dsts, offset, length);
            return sSLEngineResult;
        } finally {
            this.resetSingleSrcBuffer();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final synchronized SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        try {
            SSLEngineResult sSLEngineResult = this.wrap(this.singleSrcBuffer(src), dst);
            return sSLEngineResult;
        } finally {
            this.resetSingleSrcBuffer();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        try {
            SSLEngineResult sSLEngineResult = this.unwrap(this.singleSrcBuffer(src), this.singleDstBuffer(dst));
            return sSLEngineResult;
        } finally {
            this.resetSingleSrcBuffer();
            this.resetSingleDstBuffer();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final synchronized SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        try {
            SSLEngineResult sSLEngineResult = this.unwrap(this.singleSrcBuffer(src), dsts);
            return sSLEngineResult;
        } finally {
            this.resetSingleSrcBuffer();
        }
    }

    @Override
    public final synchronized Runnable getDelegatedTask() {
        if (this.isDestroyed()) {
            return null;
        }
        Runnable task = SSL.getTask((long)this.ssl);
        if (task == null) {
            return null;
        }
        if (task instanceof AsyncTask) {
            return new AsyncTaskDecorator((AsyncTask)task);
        }
        return new TaskDecorator(this, task);
    }

    @Override
    public final synchronized void closeInbound() throws SSLException {
        if (this.isInboundDone) {
            return;
        }
        this.isInboundDone = true;
        if (this.isOutboundDone()) {
            this.shutdown();
        }
        if (this.handshakeState != HandshakeState.NOT_STARTED && !this.receivedShutdown) {
            throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
        }
    }

    @Override
    public final synchronized boolean isInboundDone() {
        return this.isInboundDone;
    }

    @Override
    public final synchronized void closeOutbound() {
        if (this.outboundClosed) {
            return;
        }
        this.outboundClosed = true;
        if (this.handshakeState != HandshakeState.NOT_STARTED && !this.isDestroyed()) {
            int mode = SSL.getShutdown((long)this.ssl);
            if ((mode & SSL.SSL_SENT_SHUTDOWN) != SSL.SSL_SENT_SHUTDOWN) {
                this.doSSLShutdown();
            }
        } else {
            this.shutdown();
        }
    }

    private boolean doSSLShutdown() {
        if (SSL.isInInit((long)this.ssl) != 0) {
            return false;
        }
        int err = SSL.shutdownSSL((long)this.ssl);
        if (err < 0) {
            int sslErr = SSL.getError((long)this.ssl, (int)err);
            if (sslErr == SSL.SSL_ERROR_SYSCALL || sslErr == SSL.SSL_ERROR_SSL) {
                if (logger.isDebugEnabled()) {
                    int error = SSL.getLastErrorNumber();
                    logger.debug("SSL_shutdown failed: OpenSSL error: {} {}", (Object)error, (Object)SSL.getErrorString((long)error));
                }
                this.shutdown();
                return false;
            }
            SSL.clearError();
        }
        return true;
    }

    @Override
    public final synchronized boolean isOutboundDone() {
        return this.outboundClosed && (this.networkBIO == 0L || SSL.bioLengthNonApplication((long)this.networkBIO) == 0);
    }

    @Override
    public final String[] getSupportedCipherSuites() {
        return OpenSsl.AVAILABLE_CIPHER_SUITES.toArray(new String[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final String[] getEnabledCipherSuites() {
        boolean tls13Enabled;
        String[] extraCiphers;
        String[] enabled;
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (!this.isDestroyed()) {
                enabled = SSL.getCiphers((long)this.ssl);
                int opts = SSL.getOptions((long)this.ssl);
                if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1_3, "TLSv1.3")) {
                    extraCiphers = OpenSsl.EXTRA_SUPPORTED_TLS_1_3_CIPHERS;
                    tls13Enabled = true;
                } else {
                    extraCiphers = EmptyArrays.EMPTY_STRINGS;
                    tls13Enabled = false;
                }
            } else {
                return EmptyArrays.EMPTY_STRINGS;
            }
        }
        if (enabled == null) {
            return EmptyArrays.EMPTY_STRINGS;
        }
        LinkedHashSet<String> enabledSet = new LinkedHashSet<String>(enabled.length + extraCiphers.length);
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine2 = this;
        synchronized (referenceCountedOpenSslEngine2) {
            for (int i = 0; i < enabled.length; ++i) {
                String cipher;
                String mapped = this.toJavaCipherSuite(enabled[i]);
                String string = cipher = mapped == null ? enabled[i] : mapped;
                if ((!tls13Enabled || !OpenSsl.isTlsv13Supported()) && SslUtils.isTLSv13Cipher(cipher)) continue;
                enabledSet.add(cipher);
            }
            Collections.addAll(enabledSet, extraCiphers);
        }
        return enabledSet.toArray(new String[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setEnabledCipherSuites(String[] cipherSuites) {
        ObjectUtil.checkNotNull(cipherSuites, "cipherSuites");
        StringBuilder buf = new StringBuilder();
        StringBuilder bufTLSv13 = new StringBuilder();
        CipherSuiteConverter.convertToCipherStrings(Arrays.asList(cipherSuites), buf, bufTLSv13, OpenSsl.isBoringSSL());
        String cipherSuiteSpec = buf.toString();
        String cipherSuiteSpecTLSv13 = bufTLSv13.toString();
        if (!OpenSsl.isTlsv13Supported() && !cipherSuiteSpecTLSv13.isEmpty()) {
            throw new IllegalArgumentException("TLSv1.3 is not supported by this java version.");
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (!this.isDestroyed()) {
                try {
                    SSL.setCipherSuites((long)this.ssl, (String)cipherSuiteSpec, (boolean)false);
                    if (OpenSsl.isTlsv13Supported()) {
                        SSL.setCipherSuites((long)this.ssl, (String)OpenSsl.checkTls13Ciphers((InternalLogger)logger, (String)cipherSuiteSpecTLSv13), (boolean)true);
                    }
                    HashSet protocols = new HashSet(this.explicitlyEnabledProtocols.length);
                    Collections.addAll(protocols, this.explicitlyEnabledProtocols);
                    if (cipherSuiteSpec.isEmpty()) {
                        protocols.remove("TLSv1");
                        protocols.remove("TLSv1.1");
                        protocols.remove("TLSv1.2");
                        protocols.remove("SSLv3");
                        protocols.remove("SSLv2");
                        protocols.remove("SSLv2Hello");
                    }
                    if (cipherSuiteSpecTLSv13.isEmpty()) {
                        protocols.remove("TLSv1.3");
                    }
                    this.setEnabledProtocols0(protocols.toArray(EmptyArrays.EMPTY_STRINGS), false);
                } catch (Exception e) {
                    throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec, e);
                }
            } else {
                throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec);
            }
        }
    }

    @Override
    public final String[] getSupportedProtocols() {
        return OpenSsl.SUPPORTED_PROTOCOLS_SET.toArray(new String[0]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final String[] getEnabledProtocols() {
        int opts;
        ArrayList<String> enabled = new ArrayList<String>(6);
        enabled.add("SSLv2Hello");
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (this.isDestroyed()) {
                return enabled.toArray(new String[0]);
            }
            opts = SSL.getOptions((long)this.ssl);
        }
        if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1, "TLSv1")) {
            enabled.add("TLSv1");
        }
        if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1_1, "TLSv1.1")) {
            enabled.add("TLSv1.1");
        }
        if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1_2, "TLSv1.2")) {
            enabled.add("TLSv1.2");
        }
        if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_TLSv1_3, "TLSv1.3")) {
            enabled.add("TLSv1.3");
        }
        if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_SSLv2, "SSLv2")) {
            enabled.add("SSLv2");
        }
        if (ReferenceCountedOpenSslEngine.isProtocolEnabled(opts, SSL.SSL_OP_NO_SSLv3, "SSLv3")) {
            enabled.add("SSLv3");
        }
        return enabled.toArray(new String[0]);
    }

    private static boolean isProtocolEnabled(int opts, int disableMask, String protocolString) {
        return (opts & disableMask) == 0 && OpenSsl.SUPPORTED_PROTOCOLS_SET.contains(protocolString);
    }

    @Override
    public final void setEnabledProtocols(String[] protocols) {
        this.setEnabledProtocols0(protocols, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setEnabledProtocols0(String[] protocols, boolean cache) {
        ObjectUtil.checkNotNullWithIAE(protocols, "protocols");
        int minProtocolIndex = OPENSSL_OP_NO_PROTOCOLS.length;
        int maxProtocolIndex = 0;
        for (String p : protocols) {
            if (!OpenSsl.SUPPORTED_PROTOCOLS_SET.contains(p)) {
                throw new IllegalArgumentException("Protocol " + p + " is not supported.");
            }
            if (p.equals("SSLv2")) {
                if (minProtocolIndex > 0) {
                    minProtocolIndex = 0;
                }
                if (maxProtocolIndex >= 0) continue;
                maxProtocolIndex = 0;
                continue;
            }
            if (p.equals("SSLv3")) {
                if (minProtocolIndex > 1) {
                    minProtocolIndex = 1;
                }
                if (maxProtocolIndex >= 1) continue;
                maxProtocolIndex = 1;
                continue;
            }
            if (p.equals("TLSv1")) {
                if (minProtocolIndex > 2) {
                    minProtocolIndex = 2;
                }
                if (maxProtocolIndex >= 2) continue;
                maxProtocolIndex = 2;
                continue;
            }
            if (p.equals("TLSv1.1")) {
                if (minProtocolIndex > 3) {
                    minProtocolIndex = 3;
                }
                if (maxProtocolIndex >= 3) continue;
                maxProtocolIndex = 3;
                continue;
            }
            if (p.equals("TLSv1.2")) {
                if (minProtocolIndex > 4) {
                    minProtocolIndex = 4;
                }
                if (maxProtocolIndex >= 4) continue;
                maxProtocolIndex = 4;
                continue;
            }
            if (!p.equals("TLSv1.3")) continue;
            if (minProtocolIndex > 5) {
                minProtocolIndex = 5;
            }
            if (maxProtocolIndex >= 5) continue;
            maxProtocolIndex = 5;
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            int opts;
            if (cache) {
                this.explicitlyEnabledProtocols = protocols;
            }
            if (!this.isDestroyed()) {
                int i;
                SSL.clearOptions((long)this.ssl, (int)(SSL.SSL_OP_NO_SSLv2 | SSL.SSL_OP_NO_SSLv3 | SSL.SSL_OP_NO_TLSv1 | SSL.SSL_OP_NO_TLSv1_1 | SSL.SSL_OP_NO_TLSv1_2 | SSL.SSL_OP_NO_TLSv1_3));
                opts = 0;
                for (i = 0; i < minProtocolIndex; ++i) {
                    opts |= OPENSSL_OP_NO_PROTOCOLS[i];
                }
                assert (maxProtocolIndex != Integer.MAX_VALUE);
                for (i = maxProtocolIndex + 1; i < OPENSSL_OP_NO_PROTOCOLS.length; ++i) {
                    opts |= OPENSSL_OP_NO_PROTOCOLS[i];
                }
            } else {
                throw new IllegalStateException("failed to enable protocols: " + Arrays.asList(protocols));
            }
            SSL.setOptions((long)this.ssl, (int)opts);
        }
    }

    @Override
    public final SSLSession getSession() {
        return this.session;
    }

    @Override
    public final synchronized void beginHandshake() throws SSLException {
        switch (this.handshakeState) {
            case STARTED_IMPLICITLY: {
                this.checkEngineClosed();
                this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
                this.calculateMaxWrapOverhead();
                break;
            }
            case STARTED_EXPLICITLY: {
                break;
            }
            case FINISHED: {
                throw new SSLException("renegotiation unsupported");
            }
            case NOT_STARTED: {
                this.handshakeState = HandshakeState.STARTED_EXPLICITLY;
                if (this.handshake() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    this.needTask = true;
                }
                this.calculateMaxWrapOverhead();
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    private void checkEngineClosed() throws SSLException {
        if (this.isDestroyed()) {
            throw new SSLException("engine closed");
        }
    }

    private static SSLEngineResult.HandshakeStatus pendingStatus(int pendingStatus) {
        return pendingStatus > 0 ? SSLEngineResult.HandshakeStatus.NEED_WRAP : SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
    }

    private static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    private static boolean isEmpty(byte[] cert) {
        return cert == null || cert.length == 0;
    }

    private SSLEngineResult.HandshakeStatus handshakeException() throws SSLException {
        if (SSL.bioLengthNonApplication((long)this.networkBIO) > 0) {
            return SSLEngineResult.HandshakeStatus.NEED_WRAP;
        }
        Throwable exception = this.pendingException;
        assert (exception != null);
        this.pendingException = null;
        this.shutdown();
        if (exception instanceof SSLHandshakeException) {
            throw (SSLHandshakeException)exception;
        }
        SSLHandshakeException e = new SSLHandshakeException("General OpenSslEngine problem");
        e.initCause(exception);
        throw e;
    }

    final void initHandshakeException(Throwable cause) {
        if (this.pendingException == null) {
            this.pendingException = cause;
        } else {
            ThrowableUtil.addSuppressed(this.pendingException, cause);
        }
    }

    private SSLEngineResult.HandshakeStatus handshake() throws SSLException {
        int code;
        if (this.needTask) {
            return SSLEngineResult.HandshakeStatus.NEED_TASK;
        }
        if (this.handshakeState == HandshakeState.FINISHED) {
            return SSLEngineResult.HandshakeStatus.FINISHED;
        }
        this.checkEngineClosed();
        if (this.pendingException != null) {
            if (SSL.doHandshake((long)this.ssl) <= 0) {
                SSL.clearError();
            }
            return this.handshakeException();
        }
        this.engineMap.add(this);
        if (!this.sessionSet) {
            this.parentContext.sessionContext().setSessionFromCache(this.getPeerHost(), this.getPeerPort(), this.ssl);
            this.sessionSet = true;
        }
        if (this.lastAccessed == -1L) {
            this.lastAccessed = System.currentTimeMillis();
        }
        if ((code = SSL.doHandshake((long)this.ssl)) <= 0) {
            int sslError = SSL.getError((long)this.ssl, (int)code);
            if (sslError == SSL.SSL_ERROR_WANT_READ || sslError == SSL.SSL_ERROR_WANT_WRITE) {
                return ReferenceCountedOpenSslEngine.pendingStatus(SSL.bioLengthNonApplication((long)this.networkBIO));
            }
            if (sslError == SSL.SSL_ERROR_WANT_X509_LOOKUP || sslError == SSL.SSL_ERROR_WANT_CERTIFICATE_VERIFY || sslError == SSL.SSL_ERROR_WANT_PRIVATE_KEY_OPERATION) {
                return SSLEngineResult.HandshakeStatus.NEED_TASK;
            }
            if (this.needWrapAgain(SSL.getLastErrorNumber())) {
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
            if (this.pendingException != null) {
                return this.handshakeException();
            }
            throw this.shutdownWithError("SSL_do_handshake", sslError);
        }
        if (SSL.bioLengthNonApplication((long)this.networkBIO) > 0) {
            return SSLEngineResult.HandshakeStatus.NEED_WRAP;
        }
        this.session.handshakeFinished(SSL.getSessionId((long)this.ssl), SSL.getCipherForSSL((long)this.ssl), SSL.getVersion((long)this.ssl), SSL.getPeerCertificate((long)this.ssl), SSL.getPeerCertChain((long)this.ssl), SSL.getTime((long)this.ssl) * 1000L, this.parentContext.sessionTimeout() * 1000L);
        this.selectApplicationProtocol();
        return SSLEngineResult.HandshakeStatus.FINISHED;
    }

    private SSLEngineResult.HandshakeStatus mayFinishHandshake(SSLEngineResult.HandshakeStatus hs, int bytesConsumed, int bytesProduced) throws SSLException {
        return hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && bytesProduced > 0 || hs == SSLEngineResult.HandshakeStatus.NEED_WRAP && bytesConsumed > 0 ? this.handshake() : this.mayFinishHandshake(hs != SSLEngineResult.HandshakeStatus.FINISHED ? this.getHandshakeStatus() : SSLEngineResult.HandshakeStatus.FINISHED);
    }

    private SSLEngineResult.HandshakeStatus mayFinishHandshake(SSLEngineResult.HandshakeStatus status) throws SSLException {
        if (status == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            if (this.handshakeState != HandshakeState.FINISHED) {
                return this.handshake();
            }
            if (!this.isDestroyed() && SSL.bioLengthNonApplication((long)this.networkBIO) > 0) {
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
        }
        return status;
    }

    @Override
    public final synchronized SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        if (this.needPendingStatus()) {
            if (this.needTask) {
                return SSLEngineResult.HandshakeStatus.NEED_TASK;
            }
            return ReferenceCountedOpenSslEngine.pendingStatus(SSL.bioLengthNonApplication((long)this.networkBIO));
        }
        return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    private SSLEngineResult.HandshakeStatus getHandshakeStatus(int pending) {
        if (this.needPendingStatus()) {
            if (this.needTask) {
                return SSLEngineResult.HandshakeStatus.NEED_TASK;
            }
            return ReferenceCountedOpenSslEngine.pendingStatus(pending);
        }
        return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    private boolean needPendingStatus() {
        return this.handshakeState != HandshakeState.NOT_STARTED && !this.isDestroyed() && (this.handshakeState != HandshakeState.FINISHED || this.isInboundDone() || this.isOutboundDone());
    }

    private String toJavaCipherSuite(String openSslCipherSuite) {
        if (openSslCipherSuite == null) {
            return null;
        }
        String version = SSL.getVersion((long)this.ssl);
        String prefix = ReferenceCountedOpenSslEngine.toJavaCipherSuitePrefix(version);
        return CipherSuiteConverter.toJava(openSslCipherSuite, prefix);
    }

    private static String toJavaCipherSuitePrefix(String protocolVersion) {
        int c = protocolVersion == null || protocolVersion.isEmpty() ? 0 : (int)protocolVersion.charAt(0);
        switch (c) {
            case 84: {
                return "TLS";
            }
            case 83: {
                return "SSL";
            }
        }
        return "UNKNOWN";
    }

    @Override
    public final void setUseClientMode(boolean clientMode) {
        if (clientMode != this.clientMode) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean getUseClientMode() {
        return this.clientMode;
    }

    @Override
    public final void setNeedClientAuth(boolean b) {
        this.setClientAuth(b ? ClientAuth.REQUIRE : ClientAuth.NONE);
    }

    @Override
    public final boolean getNeedClientAuth() {
        return this.clientAuth == ClientAuth.REQUIRE;
    }

    @Override
    public final void setWantClientAuth(boolean b) {
        this.setClientAuth(b ? ClientAuth.OPTIONAL : ClientAuth.NONE);
    }

    @Override
    public final boolean getWantClientAuth() {
        return this.clientAuth == ClientAuth.OPTIONAL;
    }

    public final synchronized void setVerify(int verifyMode, int depth) {
        if (!this.isDestroyed()) {
            SSL.setVerify((long)this.ssl, (int)verifyMode, (int)depth);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setClientAuth(ClientAuth mode) {
        if (this.clientMode) {
            return;
        }
        ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = this;
        synchronized (referenceCountedOpenSslEngine) {
            if (this.clientAuth == mode) {
                return;
            }
            if (!this.isDestroyed()) {
                switch (mode) {
                    case NONE: {
                        SSL.setVerify((long)this.ssl, (int)0, (int)10);
                        break;
                    }
                    case REQUIRE: {
                        SSL.setVerify((long)this.ssl, (int)2, (int)10);
                        break;
                    }
                    case OPTIONAL: {
                        SSL.setVerify((long)this.ssl, (int)1, (int)10);
                        break;
                    }
                    default: {
                        throw new Error(mode.toString());
                    }
                }
            }
            this.clientAuth = mode;
        }
    }

    @Override
    public final void setEnableSessionCreation(boolean b) {
        if (b) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final boolean getEnableSessionCreation() {
        return false;
    }

    @Override
    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    public final synchronized SSLParameters getSSLParameters() {
        SSLParameters sslParameters = super.getSSLParameters();
        int version = PlatformDependent.javaVersion();
        if (version >= 7) {
            sslParameters.setEndpointIdentificationAlgorithm(this.endPointIdentificationAlgorithm);
            Java7SslParametersUtils.setAlgorithmConstraints(sslParameters, this.algorithmConstraints);
            if (version >= 8) {
                if (this.sniHostNames != null) {
                    Java8SslUtils.setSniHostNames(sslParameters, this.sniHostNames);
                }
                if (!this.isDestroyed()) {
                    Java8SslUtils.setUseCipherSuitesOrder(sslParameters, (SSL.getOptions((long)this.ssl) & SSL.SSL_OP_CIPHER_SERVER_PREFERENCE) != 0);
                }
                Java8SslUtils.setSNIMatchers(sslParameters, this.matchers);
            }
        }
        return sslParameters;
    }

    @Override
    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    public final synchronized void setSSLParameters(SSLParameters sslParameters) {
        int version = PlatformDependent.javaVersion();
        if (version >= 7) {
            if (sslParameters.getAlgorithmConstraints() != null) {
                throw new IllegalArgumentException("AlgorithmConstraints are not supported.");
            }
            boolean isDestroyed = this.isDestroyed();
            if (version >= 8) {
                if (!isDestroyed) {
                    if (this.clientMode) {
                        List<String> sniHostNames = Java8SslUtils.getSniHostNames(sslParameters);
                        for (String name : sniHostNames) {
                            SSL.setTlsExtHostName((long)this.ssl, (String)name);
                        }
                        this.sniHostNames = sniHostNames;
                    }
                    if (Java8SslUtils.getUseCipherSuitesOrder(sslParameters)) {
                        SSL.setOptions((long)this.ssl, (int)SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
                    } else {
                        SSL.clearOptions((long)this.ssl, (int)SSL.SSL_OP_CIPHER_SERVER_PREFERENCE);
                    }
                }
                this.matchers = sslParameters.getSNIMatchers();
            }
            String endPointIdentificationAlgorithm = sslParameters.getEndpointIdentificationAlgorithm();
            if (!isDestroyed && this.clientMode && ReferenceCountedOpenSslEngine.isEndPointVerificationEnabled(endPointIdentificationAlgorithm)) {
                SSL.setVerify((long)this.ssl, (int)2, (int)-1);
            }
            this.endPointIdentificationAlgorithm = endPointIdentificationAlgorithm;
            this.algorithmConstraints = sslParameters.getAlgorithmConstraints();
        }
        super.setSSLParameters(sslParameters);
    }

    private static boolean isEndPointVerificationEnabled(String endPointIdentificationAlgorithm) {
        return endPointIdentificationAlgorithm != null && !endPointIdentificationAlgorithm.isEmpty();
    }

    private boolean isDestroyed() {
        return this.destroyed;
    }

    final boolean checkSniHostnameMatch(byte[] hostname) {
        return Java8SslUtils.checkSniHostnameMatch(this.matchers, hostname);
    }

    @Override
    public String getNegotiatedApplicationProtocol() {
        return this.applicationProtocol;
    }

    private static long bufferAddress(ByteBuffer b) {
        assert (b.isDirect());
        if (PlatformDependent.hasUnsafe()) {
            return PlatformDependent.directBufferAddress(b);
        }
        return Buffer.address((ByteBuffer)b);
    }

    private void selectApplicationProtocol() throws SSLException {
        ApplicationProtocolConfig.SelectedListenerFailureBehavior behavior = this.apn.selectedListenerFailureBehavior();
        List<String> protocols = this.apn.protocols();
        switch (this.apn.protocol()) {
            case NONE: {
                break;
            }
            case ALPN: {
                String applicationProtocol = SSL.getAlpnSelected((long)this.ssl);
                if (applicationProtocol == null) break;
                this.applicationProtocol = this.selectApplicationProtocol(protocols, behavior, applicationProtocol);
                break;
            }
            case NPN: {
                String applicationProtocol = SSL.getNextProtoNegotiated((long)this.ssl);
                if (applicationProtocol == null) break;
                this.applicationProtocol = this.selectApplicationProtocol(protocols, behavior, applicationProtocol);
                break;
            }
            case NPN_AND_ALPN: {
                String applicationProtocol = SSL.getAlpnSelected((long)this.ssl);
                if (applicationProtocol == null) {
                    applicationProtocol = SSL.getNextProtoNegotiated((long)this.ssl);
                }
                if (applicationProtocol == null) break;
                this.applicationProtocol = this.selectApplicationProtocol(protocols, behavior, applicationProtocol);
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    private String selectApplicationProtocol(List<String> protocols, ApplicationProtocolConfig.SelectedListenerFailureBehavior behavior, String applicationProtocol) throws SSLException {
        if (behavior == ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT) {
            return applicationProtocol;
        }
        int size = protocols.size();
        assert (size > 0);
        if (protocols.contains(applicationProtocol)) {
            return applicationProtocol;
        }
        if (behavior == ApplicationProtocolConfig.SelectedListenerFailureBehavior.CHOOSE_MY_LAST_PROTOCOL) {
            return protocols.get(size - 1);
        }
        throw new SSLException("unknown protocol " + applicationProtocol);
    }

    final void setSessionId(OpenSslSessionId id) {
        this.session.setSessionId(id);
    }

    private final class DefaultOpenSslSession
    implements OpenSslSession {
        private final OpenSslSessionContext sessionContext;
        private X509Certificate[] x509PeerCerts;
        private Certificate[] peerCerts;
        private boolean valid = true;
        private String protocol;
        private String cipher;
        private OpenSslSessionId id = OpenSslSessionId.NULL_ID;
        private volatile long creationTime;
        private volatile int applicationBufferSize = MAX_PLAINTEXT_LENGTH;
        private volatile Certificate[] localCertificateChain;
        private Map<String, Object> values;

        DefaultOpenSslSession(OpenSslSessionContext sessionContext) {
            this.sessionContext = sessionContext;
        }

        private SSLSessionBindingEvent newSSLSessionBindingEvent(String name) {
            return new SSLSessionBindingEvent(ReferenceCountedOpenSslEngine.this.session, name);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void setSessionId(OpenSslSessionId sessionId) {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                if (this.id == OpenSslSessionId.NULL_ID) {
                    this.id = sessionId;
                    this.creationTime = System.currentTimeMillis();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public OpenSslSessionId sessionId() {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                byte[] sessionId;
                if (this.id == OpenSslSessionId.NULL_ID && !ReferenceCountedOpenSslEngine.this.isDestroyed() && (sessionId = SSL.getSessionId((long)ReferenceCountedOpenSslEngine.this.ssl)) != null) {
                    this.id = new OpenSslSessionId(sessionId);
                }
                return this.id;
            }
        }

        @Override
        public void setLocalCertificate(Certificate[] localCertificate) {
            this.localCertificateChain = localCertificate;
        }

        @Override
        public byte[] getId() {
            return this.sessionId().cloneBytes();
        }

        @Override
        public OpenSslSessionContext getSessionContext() {
            return this.sessionContext;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public long getCreationTime() {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                return this.creationTime;
            }
        }

        @Override
        public long getLastAccessedTime() {
            long lastAccessed = ReferenceCountedOpenSslEngine.this.lastAccessed;
            return lastAccessed == -1L ? this.getCreationTime() : lastAccessed;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void invalidate() {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                this.valid = false;
                this.sessionContext.removeFromCache(this.id);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean isValid() {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                return this.valid || this.sessionContext.isInCache(this.id);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void putValue(String name, Object value) {
            Object old;
            ObjectUtil.checkNotNull(name, "name");
            ObjectUtil.checkNotNull(value, "value");
            DefaultOpenSslSession defaultOpenSslSession = this;
            synchronized (defaultOpenSslSession) {
                Map<String, Object> values = this.values;
                if (values == null) {
                    values = this.values = new HashMap<String, Object>(2);
                }
                old = values.put(name, value);
            }
            if (value instanceof SSLSessionBindingListener) {
                ((SSLSessionBindingListener)value).valueBound(this.newSSLSessionBindingEvent(name));
            }
            this.notifyUnbound(old, name);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Object getValue(String name) {
            ObjectUtil.checkNotNull(name, "name");
            DefaultOpenSslSession defaultOpenSslSession = this;
            synchronized (defaultOpenSslSession) {
                if (this.values == null) {
                    return null;
                }
                return this.values.get(name);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void removeValue(String name) {
            Object old;
            ObjectUtil.checkNotNull(name, "name");
            DefaultOpenSslSession defaultOpenSslSession = this;
            synchronized (defaultOpenSslSession) {
                Map<String, Object> values = this.values;
                if (values == null) {
                    return;
                }
                old = values.remove(name);
            }
            this.notifyUnbound(old, name);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String[] getValueNames() {
            DefaultOpenSslSession defaultOpenSslSession = this;
            synchronized (defaultOpenSslSession) {
                Map<String, Object> values = this.values;
                if (values == null || values.isEmpty()) {
                    return EmptyArrays.EMPTY_STRINGS;
                }
                return values.keySet().toArray(new String[0]);
            }
        }

        private void notifyUnbound(Object value, String name) {
            if (value instanceof SSLSessionBindingListener) {
                ((SSLSessionBindingListener)value).valueUnbound(this.newSSLSessionBindingEvent(name));
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void handshakeFinished(byte[] id, String cipher, String protocol, byte[] peerCertificate, byte[][] peerCertificateChain, long creationTime, long timeout) throws SSLException {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                if (!ReferenceCountedOpenSslEngine.this.isDestroyed()) {
                    this.creationTime = creationTime;
                    if (this.id == OpenSslSessionId.NULL_ID) {
                        this.id = id == null ? OpenSslSessionId.NULL_ID : new OpenSslSessionId(id);
                    }
                    this.cipher = ReferenceCountedOpenSslEngine.this.toJavaCipherSuite(cipher);
                    this.protocol = protocol;
                    if (ReferenceCountedOpenSslEngine.this.clientMode) {
                        if (ReferenceCountedOpenSslEngine.isEmpty((Object[])peerCertificateChain)) {
                            this.peerCerts = EmptyArrays.EMPTY_CERTIFICATES;
                            this.x509PeerCerts = EmptyArrays.EMPTY_JAVAX_X509_CERTIFICATES;
                        } else {
                            this.peerCerts = new Certificate[peerCertificateChain.length];
                            this.x509PeerCerts = new X509Certificate[peerCertificateChain.length];
                            this.initCerts(peerCertificateChain, 0);
                        }
                    } else if (ReferenceCountedOpenSslEngine.isEmpty(peerCertificate)) {
                        this.peerCerts = EmptyArrays.EMPTY_CERTIFICATES;
                        this.x509PeerCerts = EmptyArrays.EMPTY_JAVAX_X509_CERTIFICATES;
                    } else if (ReferenceCountedOpenSslEngine.isEmpty((Object[])peerCertificateChain)) {
                        this.peerCerts = new Certificate[]{new LazyX509Certificate(peerCertificate)};
                        this.x509PeerCerts = new X509Certificate[]{new LazyJavaxX509Certificate(peerCertificate)};
                    } else {
                        this.peerCerts = new Certificate[peerCertificateChain.length + 1];
                        this.x509PeerCerts = new X509Certificate[peerCertificateChain.length + 1];
                        this.peerCerts[0] = new LazyX509Certificate(peerCertificate);
                        this.x509PeerCerts[0] = new LazyJavaxX509Certificate(peerCertificate);
                        this.initCerts(peerCertificateChain, 1);
                    }
                } else {
                    throw new SSLException("Already closed");
                }
                ReferenceCountedOpenSslEngine.this.calculateMaxWrapOverhead();
                ReferenceCountedOpenSslEngine.this.handshakeState = HandshakeState.FINISHED;
            }
        }

        private void initCerts(byte[][] chain, int startPos) {
            for (int i = 0; i < chain.length; ++i) {
                int certPos = startPos + i;
                this.peerCerts[certPos] = new LazyX509Certificate(chain[i]);
                this.x509PeerCerts[certPos] = new LazyJavaxX509Certificate(chain[i]);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                if (ReferenceCountedOpenSslEngine.isEmpty(this.peerCerts)) {
                    throw new SSLPeerUnverifiedException("peer not verified");
                }
                return (Certificate[])this.peerCerts.clone();
            }
        }

        @Override
        public Certificate[] getLocalCertificates() {
            Certificate[] localCerts = this.localCertificateChain;
            if (localCerts == null) {
                return null;
            }
            return (Certificate[])localCerts.clone();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                if (ReferenceCountedOpenSslEngine.isEmpty(this.x509PeerCerts)) {
                    throw new SSLPeerUnverifiedException("peer not verified");
                }
                return (X509Certificate[])this.x509PeerCerts.clone();
            }
        }

        @Override
        public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
            Certificate[] peer = this.getPeerCertificates();
            return ((java.security.cert.X509Certificate)peer[0]).getSubjectX500Principal();
        }

        @Override
        public Principal getLocalPrincipal() {
            Certificate[] local = this.localCertificateChain;
            if (local == null || local.length == 0) {
                return null;
            }
            return ((java.security.cert.X509Certificate)local[0]).getIssuerX500Principal();
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String getCipherSuite() {
            ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
            synchronized (referenceCountedOpenSslEngine) {
                if (this.cipher == null) {
                    return "SSL_NULL_WITH_NULL_NULL";
                }
                return this.cipher;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String getProtocol() {
            String protocol = this.protocol;
            if (protocol == null) {
                ReferenceCountedOpenSslEngine referenceCountedOpenSslEngine = ReferenceCountedOpenSslEngine.this;
                synchronized (referenceCountedOpenSslEngine) {
                    protocol = !ReferenceCountedOpenSslEngine.this.isDestroyed() ? SSL.getVersion((long)ReferenceCountedOpenSslEngine.this.ssl) : "";
                }
            }
            return protocol;
        }

        @Override
        public String getPeerHost() {
            return ReferenceCountedOpenSslEngine.this.getPeerHost();
        }

        @Override
        public int getPeerPort() {
            return ReferenceCountedOpenSslEngine.this.getPeerPort();
        }

        @Override
        public int getPacketBufferSize() {
            return ReferenceCountedOpenSslEngine.this.maxEncryptedPacketLength();
        }

        @Override
        public int getApplicationBufferSize() {
            return this.applicationBufferSize;
        }

        @Override
        public void tryExpandApplicationBufferSize(int packetLengthDataOnly) {
            if (packetLengthDataOnly > MAX_PLAINTEXT_LENGTH && this.applicationBufferSize != MAX_RECORD_SIZE) {
                this.applicationBufferSize = MAX_RECORD_SIZE;
            }
        }

        public String toString() {
            return "DefaultOpenSslSession{sessionContext=" + this.sessionContext + ", id=" + this.id + '}';
        }
    }

    private final class AsyncTaskDecorator
    extends TaskDecorator<AsyncTask>
    implements AsyncRunnable {
        AsyncTaskDecorator(AsyncTask task) {
            super(ReferenceCountedOpenSslEngine.this, (Runnable)task);
        }

        @Override
        public void run(final Runnable runnable) {
            if (ReferenceCountedOpenSslEngine.this.isDestroyed()) {
                runnable.run();
                return;
            }
            ((AsyncTask)this.task).runAsync(new Runnable(){

                @Override
                public void run() {
                    ReferenceCountedOpenSslEngine.this.needTask = false;
                    runnable.run();
                }
            });
        }
    }

    private static class TaskDecorator<R extends Runnable>
    implements Runnable {
        protected final R task;
        final /* synthetic */ ReferenceCountedOpenSslEngine this$0;

        TaskDecorator(R task) {
            this.this$0 = var1_1;
            this.task = task;
        }

        @Override
        public void run() {
            if (this.this$0.isDestroyed()) {
                return;
            }
            try {
                this.task.run();
            } finally {
                this.this$0.needTask = false;
            }
        }
    }

    private static enum HandshakeState {
        NOT_STARTED,
        STARTED_IMPLICITLY,
        STARTED_EXPLICITLY,
        FINISHED;

    }
}

