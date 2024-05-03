/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor.ssl;

import javax.net.ssl.SSLSession;

public final class TlsDetails {
    private final SSLSession sslSession;
    private final String applicationProtocol;

    public TlsDetails(SSLSession sslSession, String applicationProtocol) {
        this.sslSession = sslSession;
        this.applicationProtocol = applicationProtocol;
    }

    public SSLSession getSSLSession() {
        return this.sslSession;
    }

    public String getApplicationProtocol() {
        return this.applicationProtocol;
    }

    public String toString() {
        return "TlsDetails{sslSession=" + this.sslSession + ", applicationProtocol='" + this.applicationProtocol + '\'' + '}';
    }
}

