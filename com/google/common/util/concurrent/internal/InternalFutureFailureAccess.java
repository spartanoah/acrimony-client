/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.util.concurrent.internal;

public abstract class InternalFutureFailureAccess {
    protected InternalFutureFailureAccess() {
    }

    protected abstract Throwable tryInternalFastPathGetFailure();
}

