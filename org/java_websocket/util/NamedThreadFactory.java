/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory
implements ThreadFactory {
    private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String threadPrefix;

    public NamedThreadFactory(String threadPrefix) {
        this.threadPrefix = threadPrefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = this.defaultThreadFactory.newThread(runnable);
        thread.setName(this.threadPrefix + "-" + this.threadNumber);
        return thread;
    }
}

