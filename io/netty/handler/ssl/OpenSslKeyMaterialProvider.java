/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSL
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.ssl.DefaultOpenSslKeyMaterial;
import io.netty.handler.ssl.OpenSslKeyMaterial;
import io.netty.handler.ssl.OpenSslPrivateKey;
import io.netty.handler.ssl.PemEncoded;
import io.netty.handler.ssl.PemX509Certificate;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.internal.tcnative.SSL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509KeyManager;

class OpenSslKeyMaterialProvider {
    private final X509KeyManager keyManager;
    private final String password;

    OpenSslKeyMaterialProvider(X509KeyManager keyManager, String password) {
        this.keyManager = keyManager;
        this.password = password;
    }

    static void validateKeyMaterialSupported(X509Certificate[] keyCertChain, PrivateKey key, String keyPassword) throws SSLException {
        OpenSslKeyMaterialProvider.validateSupported(keyCertChain);
        OpenSslKeyMaterialProvider.validateSupported(key, keyPassword);
    }

    private static void validateSupported(PrivateKey key, String password) throws SSLException {
        if (key == null) {
            return;
        }
        long pkeyBio = 0L;
        long pkey = 0L;
        try {
            pkeyBio = ReferenceCountedOpenSslContext.toBIO((ByteBufAllocator)UnpooledByteBufAllocator.DEFAULT, key);
            pkey = SSL.parsePrivateKey((long)pkeyBio, (String)password);
        } catch (Exception e) {
            throw new SSLException("PrivateKey type not supported " + key.getFormat(), e);
        } finally {
            SSL.freeBIO((long)pkeyBio);
            if (pkey != 0L) {
                SSL.freePrivateKey((long)pkey);
            }
        }
    }

    private static void validateSupported(X509Certificate[] certificates) throws SSLException {
        if (certificates == null || certificates.length == 0) {
            return;
        }
        long chainBio = 0L;
        long chain = 0L;
        PemEncoded encoded = null;
        try {
            encoded = PemX509Certificate.toPEM(UnpooledByteBufAllocator.DEFAULT, true, certificates);
            chainBio = ReferenceCountedOpenSslContext.toBIO((ByteBufAllocator)UnpooledByteBufAllocator.DEFAULT, encoded.retain());
            chain = SSL.parseX509Chain((long)chainBio);
        } catch (Exception e) {
            throw new SSLException("Certificate type not supported", e);
        } finally {
            SSL.freeBIO((long)chainBio);
            if (chain != 0L) {
                SSL.freeX509Chain((long)chain);
            }
            if (encoded != null) {
                encoded.release();
            }
        }
    }

    X509KeyManager keyManager() {
        return this.keyManager;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    OpenSslKeyMaterial chooseKeyMaterial(ByteBufAllocator allocator, String alias) throws Exception {
        X509Certificate[] certificates = this.keyManager.getCertificateChain(alias);
        if (certificates == null || certificates.length == 0) {
            return null;
        }
        PrivateKey key = this.keyManager.getPrivateKey(alias);
        PemEncoded encoded = PemX509Certificate.toPEM(allocator, true, certificates);
        long chainBio = 0L;
        long pkeyBio = 0L;
        long chain = 0L;
        long pkey = 0L;
        try {
            OpenSslKeyMaterial keyMaterial;
            chainBio = ReferenceCountedOpenSslContext.toBIO(allocator, encoded.retain());
            chain = SSL.parseX509Chain((long)chainBio);
            if (key instanceof OpenSslPrivateKey) {
                keyMaterial = ((OpenSslPrivateKey)key).newKeyMaterial(chain, certificates);
            } else {
                pkeyBio = ReferenceCountedOpenSslContext.toBIO(allocator, key);
                pkey = key == null ? 0L : SSL.parsePrivateKey((long)pkeyBio, (String)this.password);
                keyMaterial = new DefaultOpenSslKeyMaterial(chain, pkey, certificates);
            }
            chain = 0L;
            pkey = 0L;
            OpenSslKeyMaterial openSslKeyMaterial = keyMaterial;
            return openSslKeyMaterial;
        } finally {
            SSL.freeBIO((long)chainBio);
            SSL.freeBIO((long)pkeyBio);
            if (chain != 0L) {
                SSL.freeX509Chain((long)chain);
            }
            if (pkey != 0L) {
                SSL.freePrivateKey((long)pkey);
            }
            encoded.release();
        }
    }

    void destroy() {
    }
}

