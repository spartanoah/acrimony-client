/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  io.netty.internal.tcnative.SSLPrivateKeyMethod
 */
package io.netty.handler.ssl;

import io.netty.internal.tcnative.SSLPrivateKeyMethod;
import io.netty.util.concurrent.Future;
import javax.net.ssl.SSLEngine;

public interface OpenSslAsyncPrivateKeyMethod {
    public static final int SSL_SIGN_RSA_PKCS1_SHA1 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA1;
    public static final int SSL_SIGN_RSA_PKCS1_SHA256 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA256;
    public static final int SSL_SIGN_RSA_PKCS1_SHA384 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA384;
    public static final int SSL_SIGN_RSA_PKCS1_SHA512 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_SHA512;
    public static final int SSL_SIGN_ECDSA_SHA1 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SHA1;
    public static final int SSL_SIGN_ECDSA_SECP256R1_SHA256 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP256R1_SHA256;
    public static final int SSL_SIGN_ECDSA_SECP384R1_SHA384 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP384R1_SHA384;
    public static final int SSL_SIGN_ECDSA_SECP521R1_SHA512 = SSLPrivateKeyMethod.SSL_SIGN_ECDSA_SECP521R1_SHA512;
    public static final int SSL_SIGN_RSA_PSS_RSAE_SHA256 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA256;
    public static final int SSL_SIGN_RSA_PSS_RSAE_SHA384 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA384;
    public static final int SSL_SIGN_RSA_PSS_RSAE_SHA512 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PSS_RSAE_SHA512;
    public static final int SSL_SIGN_ED25519 = SSLPrivateKeyMethod.SSL_SIGN_ED25519;
    public static final int SSL_SIGN_RSA_PKCS1_MD5_SHA1 = SSLPrivateKeyMethod.SSL_SIGN_RSA_PKCS1_MD5_SHA1;

    public Future<byte[]> sign(SSLEngine var1, int var2, byte[] var3);

    public Future<byte[]> decrypt(SSLEngine var1, byte[] var2);
}

