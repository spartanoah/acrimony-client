/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.reactor.IOReactor;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

class MultiCoreIOReactor
implements IOReactor {
    private final IOReactor[] ioReactors;
    private final Thread[] threads;
    private final AtomicReference<IOReactorStatus> status;
    private final AtomicBoolean terminated;

    MultiCoreIOReactor(IOReactor[] ioReactors, Thread[] threads) {
        this.ioReactors = (IOReactor[])ioReactors.clone();
        this.threads = (Thread[])threads.clone();
        this.status = new AtomicReference<IOReactorStatus>(IOReactorStatus.INACTIVE);
        this.terminated = new AtomicBoolean();
    }

    @Override
    public IOReactorStatus getStatus() {
        return this.status.get();
    }

    public final void start() {
        if (this.status.compareAndSet(IOReactorStatus.INACTIVE, IOReactorStatus.ACTIVE)) {
            for (int i = 0; i < this.threads.length; ++i) {
                this.threads[i].start();
            }
        }
    }

    @Override
    public final void initiateShutdown() {
        if (this.status.compareAndSet(IOReactorStatus.INACTIVE, IOReactorStatus.SHUT_DOWN) || this.status.compareAndSet(IOReactorStatus.ACTIVE, IOReactorStatus.SHUTTING_DOWN)) {
            for (int i = 0; i < this.ioReactors.length; ++i) {
                IOReactor ioReactor = this.ioReactors[i];
                ioReactor.initiateShutdown();
            }
        }
    }

    @Override
    public final void awaitShutdown(TimeValue waitTime) throws InterruptedException {
        int i;
        Args.notNull(waitTime, "Wait time");
        long deadline = System.currentTimeMillis() + waitTime.toMilliseconds();
        long remaining = waitTime.toMilliseconds();
        for (i = 0; i < this.ioReactors.length; ++i) {
            IOReactor ioReactor = this.ioReactors[i];
            if (ioReactor.getStatus().compareTo(IOReactorStatus.SHUT_DOWN) >= 0) continue;
            ioReactor.awaitShutdown(TimeValue.of(remaining, TimeUnit.MILLISECONDS));
            remaining = deadline - System.currentTimeMillis();
            if (remaining > 0L) continue;
            return;
        }
        for (i = 0; i < this.threads.length; ++i) {
            Thread thread = this.threads[i];
            thread.join(remaining);
            remaining = deadline - System.currentTimeMillis();
            if (remaining > 0L) continue;
            return;
        }
    }

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
            int i;
            for (i = 0; i < this.ioReactors.length; ++i) {
                Closer.close(this.ioReactors[i], CloseMode.IMMEDIATE);
            }
            for (i = 0; i < this.threads.length; ++i) {
                this.threads[i].interrupt();
            }
        }
    }

    @Override
    public final void close() {
        this.close(CloseMode.GRACEFUL);
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [status=" + this.status + "]";
    }
}

