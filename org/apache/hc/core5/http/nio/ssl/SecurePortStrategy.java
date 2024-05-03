/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.ssl;

import java.net.SocketAddress;

public interface SecurePortStrategy {
    public boolean isSecure(SocketAddress var1);
}

