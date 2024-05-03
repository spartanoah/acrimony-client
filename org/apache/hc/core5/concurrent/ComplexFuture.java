/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.util.Args;

public final class ComplexFuture<T>
extends BasicFuture<T>
implements CancellableDependency {
    private final AtomicReference<Cancellable> dependencyRef = new AtomicReference<Object>(null);

    public ComplexFuture(FutureCallback<T> callback) {
        super(callback);
    }

    @Override
    public void setDependency(Cancellable dependency) {
        Args.notNull(dependency, "dependency");
        if (this.isDone()) {
            dependency.cancel();
        } else {
            this.dependencyRef.set(dependency);
        }
    }

    public void setDependency(final Future<?> dependency) {
        Args.notNull(dependency, "dependency");
        if (dependency instanceof Cancellable) {
            this.setDependency((Cancellable)((Object)dependency));
        } else {
            this.setDependency(new Cancellable(){

                @Override
                public boolean cancel() {
                    return dependency.cancel(true);
                }
            });
        }
    }

    @Override
    public boolean completed(T result) {
        boolean completed = super.completed(result);
        this.dependencyRef.set(null);
        return completed;
    }

    @Override
    public boolean failed(Exception exception) {
        boolean failed = super.failed(exception);
        this.dependencyRef.set(null);
        return failed;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = super.cancel(mayInterruptIfRunning);
        Cancellable dependency = this.dependencyRef.getAndSet(null);
        if (dependency != null) {
            dependency.cancel();
        }
        return cancelled;
    }
}

