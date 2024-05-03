/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.nio;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.nio.ManagedAsyncClientConnection;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public interface AsyncClientConnectionOperator {
    public Future<ManagedAsyncClientConnection> connect(ConnectionInitiator var1, HttpHost var2, SocketAddress var3, Timeout var4, Object var5, FutureCallback<ManagedAsyncClientConnection> var6);

    public void upgrade(ManagedAsyncClientConnection var1, HttpHost var2, Object var3);
}

