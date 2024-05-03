/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.AbstractIOReactorBase;
import org.apache.hc.core5.reactor.ConnectionAcceptor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactor;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorShutdownException;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.IOReactorWorker;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.reactor.IOWorkers;
import org.apache.hc.core5.reactor.ListenerEndpoint;
import org.apache.hc.core5.reactor.MultiCoreIOReactor;
import org.apache.hc.core5.reactor.SingleCoreIOReactor;
import org.apache.hc.core5.reactor.SingleCoreListeningIOReactor;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

public class DefaultListeningIOReactor
extends AbstractIOReactorBase
implements ConnectionAcceptor {
    private static final ThreadFactory DISPATCH_THREAD_FACTORY = new DefaultThreadFactory("I/O server dispatch", true);
    private static final ThreadFactory LISTENER_THREAD_FACTORY = new DefaultThreadFactory("I/O listener", true);
    private final int workerCount;
    private final SingleCoreIOReactor[] workers;
    private final SingleCoreListeningIOReactor listener;
    private final MultiCoreIOReactor ioReactor;
    private final IOWorkers.Selector workerSelector;

    public DefaultListeningIOReactor(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig ioReactorConfig, ThreadFactory dispatchThreadFactory, ThreadFactory listenerThreadFactory, Decorator<IOSession> ioSessionDecorator, Callback<Exception> exceptionCallback, IOSessionListener sessionListener, Callback<IOSession> sessionShutdownCallback) {
        Args.notNull(eventHandlerFactory, "Event handler factory");
        this.workerCount = ioReactorConfig != null ? ioReactorConfig.getIoThreadCount() : IOReactorConfig.DEFAULT.getIoThreadCount();
        this.workers = new SingleCoreIOReactor[this.workerCount];
        Thread[] threads = new Thread[this.workerCount + 1];
        for (int i = 0; i < this.workers.length; ++i) {
            SingleCoreIOReactor dispatcher;
            this.workers[i] = dispatcher = new SingleCoreIOReactor(exceptionCallback, eventHandlerFactory, ioReactorConfig != null ? ioReactorConfig : IOReactorConfig.DEFAULT, ioSessionDecorator, sessionListener, sessionShutdownCallback);
            threads[i + 1] = (dispatchThreadFactory != null ? dispatchThreadFactory : DISPATCH_THREAD_FACTORY).newThread(new IOReactorWorker(dispatcher));
        }
        IOReactor[] ioReactors = new IOReactor[this.workerCount + 1];
        System.arraycopy(this.workers, 0, ioReactors, 1, this.workerCount);
        this.listener = new SingleCoreListeningIOReactor(exceptionCallback, ioReactorConfig, new Callback<SocketChannel>(){

            @Override
            public void execute(SocketChannel channel) {
                DefaultListeningIOReactor.this.enqueueChannel(channel);
            }
        });
        ioReactors[0] = this.listener;
        threads[0] = (listenerThreadFactory != null ? listenerThreadFactory : LISTENER_THREAD_FACTORY).newThread(new IOReactorWorker(this.listener));
        this.ioReactor = new MultiCoreIOReactor(ioReactors, threads);
        this.workerSelector = IOWorkers.newSelector(this.workers);
    }

    public DefaultListeningIOReactor(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig config, Callback<IOSession> sessionShutdownCallback) {
        this(eventHandlerFactory, config, null, null, null, null, null, sessionShutdownCallback);
    }

    public DefaultListeningIOReactor(IOEventHandlerFactory eventHandlerFactory) {
        this(eventHandlerFactory, null, null);
    }

    @Override
    public void start() {
        this.ioReactor.start();
    }

    @Override
    public Future<ListenerEndpoint> listen(SocketAddress address, FutureCallback<ListenerEndpoint> callback) {
        return this.listener.listen(address, callback);
    }

    public Future<ListenerEndpoint> listen(SocketAddress address) {
        return this.listen(address, null);
    }

    @Override
    public Set<ListenerEndpoint> getEndpoints() {
        return this.listener.getEndpoints();
    }

    @Override
    public void pause() throws IOException {
        this.listener.pause();
    }

    @Override
    public void resume() throws IOException {
        this.listener.resume();
    }

    @Override
    public IOReactorStatus getStatus() {
        return this.ioReactor.getStatus();
    }

    @Override
    IOWorkers.Selector getWorkerSelector() {
        return this.workerSelector;
    }

    private void enqueueChannel(SocketChannel socketChannel) {
        try {
            this.workerSelector.next().enqueueChannel(socketChannel);
        } catch (IOReactorShutdownException ex) {
            this.initiateShutdown();
        }
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

