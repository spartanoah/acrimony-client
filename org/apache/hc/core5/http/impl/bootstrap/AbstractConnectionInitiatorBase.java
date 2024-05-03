/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Timeout;

abstract class AbstractConnectionInitiatorBase
implements ConnectionInitiator {
    AbstractConnectionInitiatorBase() {
    }

    @Override
    public final Future<IOSession> connect(NamedEndpoint remoteEndpoint, SocketAddress remoteAddress, SocketAddress localAddress, Timeout timeout, Object attachment, FutureCallback<IOSession> callback) {
        return this.getIOReactor().connect(remoteEndpoint, remoteAddress, localAddress, timeout, attachment, callback);
    }

    abstract ConnectionInitiator getIOReactor();
}

