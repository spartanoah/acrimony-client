/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultThreadFactory
implements ThreadFactory {
    private final String namePrefix;
    private final ThreadGroup group;
    private final AtomicLong count;
    private final boolean daemon;

    public DefaultThreadFactory(String namePrefix, ThreadGroup group, boolean daemon) {
        this.namePrefix = namePrefix;
        this.group = group;
        this.daemon = daemon;
        this.count = new AtomicLong();
    }

    public DefaultThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, null, daemon);
    }

    public DefaultThreadFactory(String namePrefix) {
        this(namePrefix, null, false);
    }

    @Override
    public Thread newThread(Runnable target) {
        Thread thread = new Thread(this.group, target, this.namePrefix + "-" + this.count.incrementAndGet());
        thread.setDaemon(this.daemon);
        return thread;
    }
}

