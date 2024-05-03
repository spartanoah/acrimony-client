/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.ssl.TrustStrategy;

@Contract(threading=ThreadingBehavior.STATELESS)
public class TrustAllStrategy
implements TrustStrategy {
    public static final TrustAllStrategy INSTANCE = new TrustAllStrategy();

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return true;
    }
}

