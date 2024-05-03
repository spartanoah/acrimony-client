/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;

public final class TlsSupport {
    public static SSLSessionInitializer enforceStrongSecurity(final SSLSessionInitializer initializer) {
        return new SSLSessionInitializer(){

            @Override
            public void initialize(NamedEndpoint endpoint, SSLEngine sslEngine) {
                SSLParameters sslParameters = sslEngine.getSSLParameters();
                sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
                sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
                sslEngine.setSSLParameters(sslParameters);
                if (initializer != null) {
                    initializer.initialize(endpoint, sslEngine);
                }
            }
        };
    }
}

