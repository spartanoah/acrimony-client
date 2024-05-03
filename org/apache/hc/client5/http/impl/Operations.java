/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.hc.core5.concurrent.Cancellable;

public final class Operations {
    private static final Cancellable NOOP_CANCELLABLE = new Cancellable(){

        @Override
        public boolean cancel() {
            return false;
        }
    };

    public static Cancellable nonCancellable() {
        return NOOP_CANCELLABLE;
    }

    public static Cancellable cancellable(final Future<?> future) {
        if (future == null) {
            return NOOP_CANCELLABLE;
        }
        if (future instanceof Cancellable) {
            return (Cancellable)((Object)future);
        }
        return new Cancellable(){

            @Override
            public boolean cancel() {
                return future.cancel(true);
            }
        };
    }

    public static class CompletedFuture<T>
    implements Future<T> {
        private final T result;

        public CompletedFuture(T result) {
            this.result = result;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return this.result;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }
    }
}

