/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.ssl;

import java.net.SocketAddress;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.ssl.TransportSecurityLayer;
import org.apache.hc.core5.util.Timeout;

public interface TlsStrategy {
    public boolean upgrade(TransportSecurityLayer var1, HttpHost var2, SocketAddress var3, SocketAddress var4, Object var5, Timeout var6);
}

