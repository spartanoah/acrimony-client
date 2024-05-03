/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.net.SocketAddress;
import java.util.concurrent.Future;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.IOReactorService;
import org.apache.hc.core5.reactor.IOReactorShutdownException;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOWorkers;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.Timeout;

abstract class AbstractIOReactorBase
implements ConnectionInitiator,
IOReactorService {
    AbstractIOReactorBase() {
    }

    @Override
    public final Future<IOSession> connect(NamedEndpoint remoteEndpoint, SocketAddress remoteAddress, SocketAddress localAddress, Timeout timeout, Object attachment, FutureCallback<IOSession> callback) throws IOReactorShutdownException {
        Args.notNull(remoteEndpoint, "Remote endpoint");
        if (this.getStatus().compareTo(IOReactorStatus.ACTIVE) > 0) {
            throw new IOReactorShutdownException("I/O reactor has been shut down");
        }
        try {
            return this.getWorkerSelector().next().connect(remoteEndpoint, remoteAddress, localAddress, timeout, attachment, callback);
        } catch (IOReactorShutdownException ex) {
            this.initiateShutdown();
            throw ex;
        }
    }

    abstract IOWorkers.Selector getWorkerSelector();
}

