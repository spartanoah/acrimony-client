/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.concurrent;

import org.apache.hc.core5.concurrent.Cancellable;

public interface CancellableDependency
extends Cancellable {
    public void setDependency(Cancellable var1);

    public boolean isCancelled();
}

