/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.Cancellable;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class CompletedFuture<T>
implements Future<T>,
Cancellable {
    private final T result;

    public CompletedFuture(T result) {
        this.result = result;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return this.result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return this.result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean cancel() {
        return false;
    }
}

