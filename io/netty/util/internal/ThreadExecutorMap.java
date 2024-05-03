/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public final class ThreadExecutorMap {
    private static final FastThreadLocal<EventExecutor> mappings = new FastThreadLocal();

    private ThreadExecutorMap() {
    }

    public static EventExecutor currentExecutor() {
        return mappings.get();
    }

    private static void setCurrentEventExecutor(EventExecutor executor) {
        mappings.set(executor);
    }

    public static Executor apply(final Executor executor, final EventExecutor eventExecutor) {
        ObjectUtil.checkNotNull(executor, "executor");
        ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new Executor(){

            @Override
            public void execute(Runnable command) {
                executor.execute(ThreadExecutorMap.apply(command, eventExecutor));
            }
        };
    }

    public static Runnable apply(final Runnable command, final EventExecutor eventExecutor) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new Runnable(){

            @Override
            public void run() {
                ThreadExecutorMap.setCurrentEventExecutor(eventExecutor);
                try {
                    command.run();
                } finally {
                    ThreadExecutorMap.setCurrentEventExecutor(null);
                }
            }
        };
    }

    public static ThreadFactory apply(final ThreadFactory threadFactory, final EventExecutor eventExecutor) {
        ObjectUtil.checkNotNull(threadFactory, "command");
        ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
        return new ThreadFactory(){

            @Override
            public Thread newThread(Runnable r) {
                return threadFactory.newThread(ThreadExecutorMap.apply(r, eventExecutor));
            }
        };
    }
}

