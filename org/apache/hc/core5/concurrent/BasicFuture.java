/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeoutValueException;

public class BasicFuture<T>
implements Future<T>,
Cancellable {
    private final FutureCallback<T> callback;
    private volatile boolean completed;
    private volatile boolean cancelled;
    private volatile T result;
    private volatile Exception ex;

    public BasicFuture(FutureCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.completed;
    }

    private T getResult() throws ExecutionException {
        if (this.ex != null) {
            throw new ExecutionException(this.ex);
        }
        if (this.cancelled) {
            throw new CancellationException();
        }
        return this.result;
    }

    @Override
    public synchronized T get() throws InterruptedException, ExecutionException {
        while (!this.completed) {
            this.wait();
        }
        return this.getResult();
    }

    @Override
    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Args.notNull(unit, "Time unit");
        long msecs = unit.toMillis(timeout);
        long startTime = msecs <= 0L ? 0L : System.currentTimeMillis();
        long waitTime = msecs;
        if (this.completed) {
            return this.getResult();
        }
        if (waitTime <= 0L) {
            throw TimeoutValueException.fromMilliseconds(msecs, msecs + Math.abs(waitTime));
        }
        do {
            this.wait(waitTime);
            if (!this.completed) continue;
            return this.getResult();
        } while ((waitTime = msecs - (System.currentTimeMillis() - startTime)) > 0L);
        throw TimeoutValueException.fromMilliseconds(msecs, msecs + Math.abs(waitTime));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean completed(T result) {
        BasicFuture basicFuture = this;
        synchronized (basicFuture) {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.result = result;
            this.notifyAll();
        }
        if (this.callback != null) {
            this.callback.completed(result);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean failed(Exception exception) {
        BasicFuture basicFuture = this;
        synchronized (basicFuture) {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.ex = exception;
            this.notifyAll();
        }
        if (this.callback != null) {
            this.callback.failed(exception);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        BasicFuture basicFuture = this;
        synchronized (basicFuture) {
            if (this.completed) {
                return false;
            }
            this.completed = true;
            this.cancelled = true;
            this.notifyAll();
        }
        if (this.callback != null) {
            this.callback.cancelled();
        }
        return true;
    }

    @Override
    public boolean cancel() {
        return this.cancel(true);
    }
}

