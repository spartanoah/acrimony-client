/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.DefaultAddressResolver;
import org.apache.hc.core5.http.impl.bootstrap.AbstractConnectionInitiatorBase;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorService;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public class AsyncRequester
extends AbstractConnectionInitiatorBase
implements IOReactorService {
    private final DefaultConnectingIOReactor ioReactor;
    private final Resolver<HttpHost, InetSocketAddress> addressResolver;

    @Internal
    public AsyncRequester(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig ioReactorConfig, Decorator<IOSession> ioSessionDecorator, Callback<Exception> exceptionCallback, IOSessionListener sessionListener, Callback<IOSession> sessionShutdownCallback, Resolver<HttpHost, InetSocketAddress> addressResolver) {
        this.ioReactor = new DefaultConnectingIOReactor(eventHandlerFactory, ioReactorConfig, new DefaultThreadFactory("requester-dispatch", true), ioSessionDecorator, exceptionCallback, sessionListener, sessionShutdownCallback);
        this.addressResolver = addressResolver != null ? addressResolver : DefaultAddressResolver.INSTANCE;
    }

    @Override
    ConnectionInitiator getIOReactor() {
        return this.ioReactor;
    }

    public Future<IOSession> requestSession(HttpHost host, Timeout timeout, Object attachment, FutureCallback<IOSession> callback) {
        Args.notNull(host, "Host");
        Args.notNull(timeout, "Timeout");
        return this.connect(host, this.addressResolver.resolve(host), null, timeout, attachment, callback);
    }

    @Override
    public void start() {
        this.ioReactor.start();
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

