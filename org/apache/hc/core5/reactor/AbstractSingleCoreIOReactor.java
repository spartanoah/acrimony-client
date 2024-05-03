/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.reactor.IOReactor;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

abstract class AbstractSingleCoreIOReactor
implements IOReactor {
    private final Callback<Exception> exceptionCallback;
    private final AtomicReference<IOReactorStatus> status;
    private final AtomicBoolean terminated;
    private final Object shutdownMutex;
    final Selector selector;

    AbstractSingleCoreIOReactor(Callback<Exception> exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
        this.shutdownMutex = new Object();
        this.status = new AtomicReference<IOReactorStatus>(IOReactorStatus.INACTIVE);
        this.terminated = new AtomicBoolean();
        try {
            this.selector = Selector.open();
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected failure opening I/O selector", ex);
        }
    }

    @Override
    public final IOReactorStatus getStatus() {
        return this.status.get();
    }

    void logException(Exception ex) {
        if (this.exceptionCallback != null) {
            this.exceptionCallback.execute(ex);
        }
    }

    abstract void doExecute() throws IOException;

    abstract void doTerminate() throws IOException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void execute() {
        if (!this.status.compareAndSet(IOReactorStatus.INACTIVE, IOReactorStatus.ACTIVE)) return;
        try {
            this.doExecute();
            return;
        } catch (ClosedSelectorException ignore) {
            try {
                this.doTerminate();
                return;
            } catch (Exception ex) {
                this.logException(ex);
                return;
            } finally {
                this.close(CloseMode.IMMEDIATE);
            }
        } catch (Exception ex) {
            this.logException(ex);
            return;
        } finally {
            try {
                this.doTerminate();
            } catch (Exception ex) {
                this.logException(ex);
            } finally {
                this.close(CloseMode.IMMEDIATE);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void awaitShutdown(TimeValue waitTime) throws InterruptedException {
        Args.notNull(waitTime, "Wait time");
        long deadline = System.currentTimeMillis() + waitTime.toMilliseconds();
        long remaining = waitTime.toMilliseconds();
        Object object = this.shutdownMutex;
        synchronized (object) {
            while (this.status.get().compareTo(IOReactorStatus.SHUT_DOWN) < 0) {
                this.shutdownMutex.wait(remaining);
                remaining = deadline - System.currentTimeMillis();
                if (remaining > 0L) continue;
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void initiateShutdown() {
        if (this.status.compareAndSet(IOReactorStatus.INACTIVE, IOReactorStatus.SHUT_DOWN)) {
            Object object = this.shutdownMutex;
            synchronized (object) {
                this.shutdownMutex.notifyAll();
            }
        } else if (this.status.compareAndSet(IOReactorStatus.ACTIVE, IOReactorStatus.SHUTTING_DOWN)) {
            this.selector.wakeup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void close(CloseMode closeMode) {
        if (closeMode == CloseMode.GRACEFUL) {
            this.initiateShutdown();
            try {
                this.awaitShutdown(TimeValue.ofSeconds(5L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.status.set(IOReactorStatus.SHUT_DOWN);
        if (this.terminated.compareAndSet(false, true)) {
            try {
                Set<SelectionKey> keys = this.selector.keys();
                for (SelectionKey key : keys) {
                    try {
                        Closer.close((Closeable)key.attachment());
                    } catch (IOException ex) {
                        this.logException(ex);
                    }
                    key.channel().close();
                }
                this.selector.close();
            } catch (Exception ex) {
                this.logException(ex);
            }
        }
        Object object = this.shutdownMutex;
        synchronized (object) {
            this.shutdownMutex.notifyAll();
        }
    }

    @Override
    public final void close() {
        this.close(CloseMode.GRACEFUL);
    }

    public String toString() {
        return super.toString() + " [status=" + this.status + "]";
    }
}

