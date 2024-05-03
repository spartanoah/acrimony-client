/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.ssl;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.apache.hc.core5.util.Args;

public final class PrivateKeyDetails {
    private final String type;
    private final X509Certificate[] certChain;

    public PrivateKeyDetails(String type, X509Certificate[] certChain) {
        this.type = Args.notNull(type, "Private key type");
        this.certChain = certChain;
    }

    public String getType() {
        return this.type;
    }

    public X509Certificate[] getCertChain() {
        return this.certChain;
    }

    public String toString() {
        return this.type + ':' + Arrays.toString(this.certChain);
    }
}

