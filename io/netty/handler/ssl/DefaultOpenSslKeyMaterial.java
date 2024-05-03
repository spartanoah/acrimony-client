/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSL
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslKeyMaterial;
import io.netty.internal.tcnative.SSL;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;
import java.security.cert.X509Certificate;

final class DefaultOpenSslKeyMaterial
extends AbstractReferenceCounted
implements OpenSslKeyMaterial {
    private static final ResourceLeakDetector<DefaultOpenSslKeyMaterial> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(DefaultOpenSslKeyMaterial.class);
    private final ResourceLeakTracker<DefaultOpenSslKeyMaterial> leak;
    private final X509Certificate[] x509CertificateChain;
    private long chain;
    private long privateKey;

    DefaultOpenSslKeyMaterial(long chain, long privateKey, X509Certificate[] x509CertificateChain) {
        this.chain = chain;
        this.privateKey = privateKey;
        this.x509CertificateChain = x509CertificateChain;
        this.leak = leakDetector.track(this);
    }

    @Override
    public X509Certificate[] certificateChain() {
        return (X509Certificate[])this.x509CertificateChain.clone();
    }

    @Override
    public long certificateChainAddress() {
        if (this.refCnt() <= 0) {
            throw new IllegalReferenceCountException();
        }
        return this.chain;
    }

    @Override
    public long privateKeyAddress() {
        if (this.refCnt() <= 0) {
            throw new IllegalReferenceCountException();
        }
        return this.privateKey;
    }

    @Override
    protected void deallocate() {
        SSL.freeX509Chain((long)this.chain);
        this.chain = 0L;
        SSL.freePrivateKey((long)this.privateKey);
        this.privateKey = 0L;
        if (this.leak != null) {
            boolean closed = this.leak.close(this);
            assert (closed);
        }
    }

    @Override
    public DefaultOpenSslKeyMaterial retain() {
        if (this.leak != null) {
            this.leak.record();
        }
        super.retain();
        return this;
    }

    @Override
    public DefaultOpenSslKeyMaterial retain(int increment) {
        if (this.leak != null) {
            this.leak.record();
        }
        super.retain(increment);
        return this;
    }

    @Override
    public DefaultOpenSslKeyMaterial touch() {
        if (this.leak != null) {
            this.leak.record();
        }
        super.touch();
        return this;
    }

    @Override
    public DefaultOpenSslKeyMaterial touch(Object hint) {
        if (this.leak != null) {
            this.leak.record(hint);
        }
        return this;
    }

    @Override
    public boolean release() {
        if (this.leak != null) {
            this.leak.record();
        }
        return super.release();
    }

    @Override
    public boolean release(int decrement) {
        if (this.leak != null) {
            this.leak.record();
        }
        return super.release(decrement);
    }
}

