/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.Future;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.http.impl.bootstrap.AbstractConnectionInitiatorBase;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ConnectionAcceptor;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.DefaultListeningIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorService;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.reactor.ListenerEndpoint;
import org.apache.hc.core5.util.TimeValue;

public class AsyncServer
extends AbstractConnectionInitiatorBase
implements IOReactorService,
ConnectionAcceptor {
    private final DefaultListeningIOReactor ioReactor;

    @Internal
    public AsyncServer(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig ioReactorConfig, Decorator<IOSession> ioSessionDecorator, Callback<Exception> exceptionCallback, IOSessionListener sessionListener, Callback<IOSession> sessionShutdownCallback) {
        this.ioReactor = new DefaultListeningIOReactor(eventHandlerFactory, ioReactorConfig, new DefaultThreadFactory("server-dispatch", true), new DefaultThreadFactory("server-listener", true), ioSessionDecorator, exceptionCallback, sessionListener, sessionShutdownCallback);
    }

    @Override
    public void start() {
        this.ioReactor.start();
    }

    @Override
    ConnectionInitiator getIOReactor() {
        return this.ioReactor;
    }

    @Override
    public Future<ListenerEndpoint> listen(SocketAddress address, FutureCallback<ListenerEndpoint> callback) {
        return this.ioReactor.listen(address, callback);
    }

    public Future<ListenerEndpoint> listen(SocketAddress address) {
        return this.ioReactor.listen(address, null);
    }

    @Override
    public void pause() throws IOException {
        this.ioReactor.pause();
    }

    @Override
    public void resume() throws IOException {
        this.ioReactor.resume();
    }

    @Override
    public Set<ListenerEndpoint> getEndpoints() {
        return this.ioReactor.getEndpoints();
    }

    @Override
    public IOReactorStatus getStatus() {
        return this.ioReactor.getStatus();
    }

    @Override
    public void initiateShutdown() {
        this.ioReactor.initiateShutdown();
    }

    @Override
    public void awaitShutdown(TimeValue waitTime) throws InterruptedException {
        this.ioReactor.awaitShutdown(waitTime);
    }

    @Override
    public void close(CloseMode closeMode) {
        this.ioReactor.close(closeMode);
    }

    @Override
    public void close() throws IOException {
        this.ioReactor.close();
    }
}

