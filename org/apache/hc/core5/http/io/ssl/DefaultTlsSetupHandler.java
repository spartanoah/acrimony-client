/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.ssl;

import javax.net.ssl.SSLParameters;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;

public final class DefaultTlsSetupHandler
implements Callback<SSLParameters> {
    @Override
    public void execute(SSLParameters sslParameters) {
        sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
        sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
    }
}

