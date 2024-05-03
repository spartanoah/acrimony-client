/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor.ssl;

import javax.net.ssl.SSLContext;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.util.Timeout;

public interface TransportSecurityLayer {
    public void startTls(SSLContext var1, NamedEndpoint var2, SSLBufferMode var3, SSLSessionInitializer var4, SSLSessionVerifier var5, Timeout var6) throws UnsupportedOperationException;

    public TlsDetails getTlsDetails();
}

