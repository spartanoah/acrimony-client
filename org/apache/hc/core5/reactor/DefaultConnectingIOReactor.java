/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.AbstractIOReactorBase;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.reactor.IOReactorWorker;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.apache.hc.core5.reactor.IOWorkers;
import org.apache.hc.core5.reactor.MultiCoreIOReactor;
import org.apache.hc.core5.reactor.SingleCoreIOReactor;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

public class DefaultConnectingIOReactor
extends AbstractIOReactorBase {
    private final int workerCount;
    private final SingleCoreIOReactor[] workers;
    private final MultiCoreIOReactor ioReactor;
    private final IOWorkers.Selector workerSelector;
    private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory("I/O client dispatch", true);

    public DefaultConnectingIOReactor(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig ioReactorConfig, ThreadFactory threadFactory, Decorator<IOSession> ioSessionDecorator, Callback<Exception> exceptionCallback, IOSessionListener sessionListener, Callback<IOSession> sessionShutdownCallback) {
        Args.notNull(eventHandlerFactory, "Event handler factory");
        this.workerCount = ioReactorConfig != null ? ioReactorConfig.getIoThreadCount() : IOReactorConfig.DEFAULT.getIoThreadCount();
        this.workers = new SingleCoreIOReactor[this.workerCount];
        Thread[] threads = new Thread[this.workerCount];
        for (int i = 0; i < this.workers.length; ++i) {
            SingleCoreIOReactor dispatcher;
            this.workers[i] = dispatcher = new SingleCoreIOReactor(exceptionCallback, eventHandlerFactory, ioReactorConfig != null ? ioReactorConfig : IOReactorConfig.DEFAULT, ioSessionDecorator, sessionListener, sessionShutdownCallback);
            threads[i] = (threadFactory != null ? threadFactory : THREAD_FACTORY).newThread(new IOReactorWorker(dispatcher));
        }
        this.ioReactor = new MultiCoreIOReactor(this.workers, threads);
        this.workerSelector = IOWorkers.newSelector(this.workers);
    }

    public DefaultConnectingIOReactor(IOEventHandlerFactory eventHandlerFactory, IOReactorConfig config, Callback<IOSession> sessionShutdownCallback) {
        this(eventHandlerFactory, config, null, null, null, null, sessionShutdownCallback);
    }

    public DefaultConnectingIOReactor(IOEventHandlerFactory eventHandlerFactory) {
        this(eventHandlerFactory, null, null);
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
    IOWorkers.Selector getWorkerSelector() {
        return this.workerSelector;
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

