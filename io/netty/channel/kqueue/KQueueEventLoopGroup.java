/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.kqueue;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopTaskQueueFactory;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SelectStrategyFactory;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoop;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public final class KQueueEventLoopGroup
extends MultithreadEventLoopGroup {
    public KQueueEventLoopGroup() {
        this(0);
    }

    public KQueueEventLoopGroup(int nThreads) {
        this(nThreads, (ThreadFactory)null);
    }

    public KQueueEventLoopGroup(ThreadFactory threadFactory) {
        this(0, threadFactory, 0);
    }

    public KQueueEventLoopGroup(int nThreads, SelectStrategyFactory selectStrategyFactory) {
        this(nThreads, (ThreadFactory)null, selectStrategyFactory);
    }

    public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, 0);
    }

    public KQueueEventLoopGroup(int nThreads, Executor executor) {
        this(nThreads, executor, DefaultSelectStrategyFactory.INSTANCE);
    }

    public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectStrategyFactory selectStrategyFactory) {
        this(nThreads, threadFactory, 0, selectStrategyFactory);
    }

    @Deprecated
    public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce) {
        this(nThreads, threadFactory, maxEventsAtOnce, DefaultSelectStrategyFactory.INSTANCE);
    }

    @Deprecated
    public KQueueEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce, SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, threadFactory, maxEventsAtOnce, selectStrategyFactory, RejectedExecutionHandlers.reject());
        KQueue.ensureAvailability();
    }

    public KQueueEventLoopGroup(int nThreads, Executor executor, SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, executor, new Object[]{0, selectStrategyFactory, RejectedExecutionHandlers.reject()});
        KQueue.ensureAvailability();
    }

    public KQueueEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, executor, chooserFactory, new Object[]{0, selectStrategyFactory, RejectedExecutionHandlers.reject()});
        KQueue.ensureAvailability();
    }

    public KQueueEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        super(nThreads, executor, chooserFactory, new Object[]{0, selectStrategyFactory, rejectedExecutionHandler});
        KQueue.ensureAvailability();
    }

    public KQueueEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler, EventLoopTaskQueueFactory queueFactory) {
        super(nThreads, executor, chooserFactory, new Object[]{0, selectStrategyFactory, rejectedExecutionHandler, queueFactory});
        KQueue.ensureAvailability();
    }

    public KQueueEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler, EventLoopTaskQueueFactory taskQueueFactory, EventLoopTaskQueueFactory tailTaskQueueFactory) {
        super(nThreads, executor, chooserFactory, new Object[]{0, selectStrategyFactory, rejectedExecutionHandler, taskQueueFactory, tailTaskQueueFactory});
        KQueue.ensureAvailability();
    }

    public void setIoRatio(int ioRatio) {
        for (EventExecutor e : this) {
            ((KQueueEventLoop)e).setIoRatio(ioRatio);
        }
    }

    protected EventLoop newChild(Executor executor, Object ... args) throws Exception {
        Integer maxEvents = (Integer)args[0];
        SelectStrategyFactory selectStrategyFactory = (SelectStrategyFactory)args[1];
        RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler)args[2];
        EventLoopTaskQueueFactory taskQueueFactory = null;
        EventLoopTaskQueueFactory tailTaskQueueFactory = null;
        int argsLength = args.length;
        if (argsLength > 3) {
            taskQueueFactory = (EventLoopTaskQueueFactory)args[3];
        }
        if (argsLength > 4) {
            tailTaskQueueFactory = (EventLoopTaskQueueFactory)args[4];
        }
        return new KQueueEventLoop(this, executor, maxEvents, selectStrategyFactory.newSelectStrategy(), rejectedExecutionHandler, taskQueueFactory, tailTaskQueueFactory);
    }
}

