/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor.ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ssl.TlsDetails;

public interface SSLSessionVerifier {
    public TlsDetails verify(NamedEndpoint var1, SSLEngine var2) throws SSLException;
}

