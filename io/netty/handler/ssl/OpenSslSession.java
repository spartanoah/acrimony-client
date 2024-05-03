/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.OpenSslSessionContext;
import io.netty.handler.ssl.OpenSslSessionId;
import java.security.cert.Certificate;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

interface OpenSslSession
extends SSLSession {
    public OpenSslSessionId sessionId();

    public void setLocalCertificate(Certificate[] var1);

    public void setSessionId(OpenSslSessionId var1);

    @Override
    public OpenSslSessionContext getSessionContext();

    public void tryExpandApplicationBufferSize(int var1);

    public void handshakeFinished(byte[] var1, String var2, String var3, byte[] var4, byte[][] var5, long var6, long var8) throws SSLException;
}

