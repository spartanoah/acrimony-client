/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.impl.async.AsyncPushConsumerRegistry;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractHttpAsyncClientBase
extends CloseableHttpAsyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpAsyncClientBase.class);
    private final AsyncPushConsumerRegistry pushConsumerRegistry;
    private final DefaultConnectingIOReactor ioReactor;
    private final ExecutorService executorService;
    private final AtomicReference<Status> status;

    AbstractHttpAsyncClientBase(DefaultConnectingIOReactor ioReactor, AsyncPushConsumerRegistry pushConsumerRegistry, ThreadFactory threadFactory) {
        this.ioReactor = ioReactor;
        this.pushConsumerRegistry = pushConsumerRegistry;
        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
        this.status = new AtomicReference<Status>(Status.READY);
    }

    @Override
    public final void start() {
        if (this.status.compareAndSet(Status.READY, Status.RUNNING)) {
            this.executorService.execute(new Runnable(){

                @Override
                public void run() {
                    AbstractHttpAsyncClientBase.this.ioReactor.start();
                }
            });
        }
    }

    @Override
    public void register(String hostname, String uriPattern, Supplier<AsyncPushConsumer> supplier) {
        this.pushConsumerRegistry.register(hostname, uriPattern, supplier);
    }

    boolean isRunning() {
        return this.status.get() == Status.RUNNING;
    }

    ConnectionInitiator getConnectionInitiator() {
        return this.ioReactor;
    }

    @Override
    public final IOReactorStatus getStatus() {
        return this.ioReactor.getStatus();
    }

    @Override
    public final void awaitShutdown(TimeValue waitTime) throws InterruptedException {
        this.ioReactor.awaitShutdown(waitTime);
    }

    @Override
    public final void initiateShutdown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Initiating shutdown");
        }
        this.ioReactor.initiateShutdown();
    }

    void internalClose(CloseMode closeMode) {
    }

    @Override
    public final void close(CloseMode closeMode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutdown {}", (Object)closeMode);
        }
        this.ioReactor.initiateShutdown();
        this.ioReactor.close(closeMode);
        this.executorService.shutdownNow();
        this.internalClose(closeMode);
    }

    @Override
    public void close() {
        this.close(CloseMode.GRACEFUL);
    }

    static enum Status {
        READY,
        RUNNING,
        TERMINATED;

    }
}

