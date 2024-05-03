/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.util.concurrent.FutureTask;
import org.apache.hc.client5.http.impl.classic.HttpRequestTaskCallable;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.http.ClassicHttpRequest;

final class HttpRequestFutureTask<V>
extends FutureTask<V> {
    private final ClassicHttpRequest request;
    private final HttpRequestTaskCallable<V> callable;

    HttpRequestFutureTask(ClassicHttpRequest request, HttpRequestTaskCallable<V> httpCallable) {
        super(httpCallable);
        this.request = request;
        this.callable = httpCallable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.callable.cancel();
        if (mayInterruptIfRunning && this.request instanceof Cancellable) {
            ((Cancellable)((Object)this.request)).cancel();
        }
        return super.cancel(mayInterruptIfRunning);
    }

    public long scheduledTime() {
        return this.callable.getScheduled();
    }

    public long startedTime() {
        return this.callable.getStarted();
    }

    public long endedTime() {
        if (this.isDone()) {
            return this.callable.getEnded();
        }
        throw new IllegalStateException("Task is not done yet");
    }

    public long requestDuration() {
        if (this.isDone()) {
            return this.endedTime() - this.startedTime();
        }
        throw new IllegalStateException("Task is not done yet");
    }

    public long taskDuration() {
        if (this.isDone()) {
            return this.endedTime() - this.scheduledTime();
        }
        throw new IllegalStateException("Task is not done yet");
    }

    public String toString() {
        return this.request.toString();
    }
}

