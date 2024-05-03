/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.concurrent;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import java.util.concurrent.ThreadFactory;

final class DefaultEventExecutor
extends SingleThreadEventExecutor {
    DefaultEventExecutor(DefaultEventExecutorGroup parent, ThreadFactory threadFactory) {
        super(parent, threadFactory, true);
    }

    @Override
    protected void run() {
        do {
            Runnable task;
            if ((task = this.takeTask()) == null) continue;
            task.run();
            this.updateLastExecutionTime();
        } while (!this.confirmShutdown());
    }
}

