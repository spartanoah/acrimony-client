/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.reactor.ListenerEndpoint;

public interface ConnectionAcceptor {
    public Future<ListenerEndpoint> listen(SocketAddress var1, FutureCallback<ListenerEndpoint> var2);

    public void pause() throws IOException;

    public void resume() throws IOException;

    public Set<ListenerEndpoint> getEndpoints();
}

