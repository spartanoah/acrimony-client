/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;

public interface ConnectionInitiator {
    public Future<IOSession> connect(NamedEndpoint var1, SocketAddress var2, SocketAddress var3, Timeout var4, Object var5, FutureCallback<IOSession> var6);
}

