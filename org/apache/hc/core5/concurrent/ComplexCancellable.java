/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.concurrent;

import java.util.concurrent.atomic.AtomicMarkableReference;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.util.Args;

public final class ComplexCancellable
implements CancellableDependency {
    private final AtomicMarkableReference<Cancellable> dependencyRef = new AtomicMarkableReference<Object>(null, false);

    @Override
    public boolean isCancelled() {
        return this.dependencyRef.isMarked();
    }

    @Override
    public void setDependency(Cancellable dependency) {
        Args.notNull(dependency, "dependency");
        Cancellable actualDependency = this.dependencyRef.getReference();
        if (!this.dependencyRef.compareAndSet(actualDependency, dependency, false, false)) {
            dependency.cancel();
        }
    }

    @Override
    public boolean cancel() {
        while (!this.dependencyRef.isMarked()) {
            Cancellable actualDependency = this.dependencyRef.getReference();
            if (!this.dependencyRef.compareAndSet(actualDependency, actualDependency, false, true)) continue;
            if (actualDependency != null) {
                actualDependency.cancel();
            }
            return true;
        }
        return false;
    }
}

