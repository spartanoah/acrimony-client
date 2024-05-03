/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedChannelPool
extends SimpleChannelPool {
    private final EventExecutor executor;
    private final long acquireTimeoutNanos;
    private final Runnable timeoutTask;
    private final Queue<AcquireTask> pendingAcquireQueue = new ArrayDeque<AcquireTask>();
    private final int maxConnections;
    private final int maxPendingAcquires;
    private final AtomicInteger acquiredChannelCount = new AtomicInteger();
    private int pendingAcquireCount;
    private boolean closed;

    public FixedChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, int maxConnections) {
        this(bootstrap, handler, maxConnections, Integer.MAX_VALUE);
    }

    public FixedChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, int maxConnections, int maxPendingAcquires) {
        this(bootstrap, handler, ChannelHealthChecker.ACTIVE, null, -1L, maxConnections, maxPendingAcquires);
    }

    public FixedChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, AcquireTimeoutAction action, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires) {
        this(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, true);
    }

    public FixedChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, AcquireTimeoutAction action, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires, boolean releaseHealthCheck) {
        this(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, releaseHealthCheck, true);
    }

    public FixedChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, AcquireTimeoutAction action, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires, boolean releaseHealthCheck, boolean lastRecentUsed) {
        super(bootstrap, handler, healthCheck, releaseHealthCheck, lastRecentUsed);
        ObjectUtil.checkPositive(maxConnections, "maxConnections");
        ObjectUtil.checkPositive(maxPendingAcquires, "maxPendingAcquires");
        if (action == null && acquireTimeoutMillis == -1L) {
            this.timeoutTask = null;
            this.acquireTimeoutNanos = -1L;
        } else {
            if (action == null && acquireTimeoutMillis != -1L) {
                throw new NullPointerException("action");
            }
            if (action != null && acquireTimeoutMillis < 0L) {
                throw new IllegalArgumentException("acquireTimeoutMillis: " + acquireTimeoutMillis + " (expected: >= 0)");
            }
            this.acquireTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(acquireTimeoutMillis);
            switch (action) {
                case FAIL: {
                    this.timeoutTask = new TimeoutTask(){

                        @Override
                        public void onTimeout(AcquireTask task) {
                            task.promise.setFailure(new AcquireTimeoutException());
                        }
                    };
                    break;
                }
                case NEW: {
                    this.timeoutTask = new TimeoutTask(){

                        @Override
                        public void onTimeout(AcquireTask task) {
                            task.acquired();
                            FixedChannelPool.super.acquire(task.promise);
                        }
                    };
                    break;
                }
                default: {
                    throw new Error();
                }
            }
        }
        this.executor = bootstrap.config().group().next();
        this.maxConnections = maxConnections;
        this.maxPendingAcquires = maxPendingAcquires;
    }

    public int acquiredChannelCount() {
        return this.acquiredChannelCount.get();
    }

    @Override
    public Future<Channel> acquire(final Promise<Channel> promise) {
        try {
            if (this.executor.inEventLoop()) {
                this.acquire0(promise);
            } else {
                this.executor.execute(new Runnable(){

                    @Override
                    public void run() {
                        FixedChannelPool.this.acquire0(promise);
                    }
                });
            }
        } catch (Throwable cause) {
            promise.setFailure(cause);
        }
        return promise;
    }

    private void acquire0(Promise<Channel> promise) {
        assert (this.executor.inEventLoop());
        if (this.closed) {
            promise.setFailure(new IllegalStateException("FixedChannelPool was closed"));
            return;
        }
        if (this.acquiredChannelCount.get() < this.maxConnections) {
            assert (this.acquiredChannelCount.get() >= 0);
            Promise<Channel> p = this.executor.newPromise();
            AcquireListener l = new AcquireListener(promise);
            l.acquired();
            p.addListener(l);
            super.acquire(p);
        } else {
            if (this.pendingAcquireCount >= this.maxPendingAcquires) {
                this.tooManyOutstanding(promise);
            } else {
                AcquireTask task = new AcquireTask(promise);
                if (this.pendingAcquireQueue.offer(task)) {
                    ++this.pendingAcquireCount;
                    if (this.timeoutTask != null) {
                        task.timeoutFuture = this.executor.schedule(this.timeoutTask, this.acquireTimeoutNanos, TimeUnit.NANOSECONDS);
                    }
                } else {
                    this.tooManyOutstanding(promise);
                }
            }
            assert (this.pendingAcquireCount > 0);
        }
    }

    private void tooManyOutstanding(Promise<?> promise) {
        promise.setFailure(new IllegalStateException("Too many outstanding acquire operations"));
    }

    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        Promise<Void> p = this.executor.newPromise();
        super.release(channel, p.addListener(new FutureListener<Void>(){

            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                assert (FixedChannelPool.this.executor.inEventLoop());
                if (FixedChannelPool.this.closed) {
                    channel.close();
                    promise.setFailure(new IllegalStateException("FixedChannelPool was closed"));
                    return;
                }
                if (future.isSuccess()) {
                    FixedChannelPool.this.decrementAndRunTaskQueue();
                    promise.setSuccess(null);
                } else {
                    Throwable cause = future.cause();
                    if (!(cause instanceof IllegalArgumentException)) {
                        FixedChannelPool.this.decrementAndRunTaskQueue();
                    }
                    promise.setFailure(future.cause());
                }
            }
        }));
        return promise;
    }

    private void decrementAndRunTaskQueue() {
        int currentCount = this.acquiredChannelCount.decrementAndGet();
        assert (currentCount >= 0);
        this.runTaskQueue();
    }

    private void runTaskQueue() {
        AcquireTask task;
        while (this.acquiredChannelCount.get() < this.maxConnections && (task = this.pendingAcquireQueue.poll()) != null) {
            ScheduledFuture<?> timeoutFuture = task.timeoutFuture;
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }
            --this.pendingAcquireCount;
            task.acquired();
            super.acquire(task.promise);
        }
        assert (this.pendingAcquireCount >= 0);
        assert (this.acquiredChannelCount.get() >= 0);
    }

    @Override
    public void close() {
        try {
            this.closeAsync().await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<Void> closeAsync() {
        if (this.executor.inEventLoop()) {
            return this.close0();
        }
        final Promise<Void> closeComplete = this.executor.newPromise();
        this.executor.execute(new Runnable(){

            @Override
            public void run() {
                FixedChannelPool.this.close0().addListener(new FutureListener<Void>(){

                    @Override
                    public void operationComplete(Future<Void> f) throws Exception {
                        if (f.isSuccess()) {
                            closeComplete.setSuccess(null);
                        } else {
                            closeComplete.setFailure(f.cause());
                        }
                    }
                });
            }
        });
        return closeComplete;
    }

    private Future<Void> close0() {
        assert (this.executor.inEventLoop());
        if (!this.closed) {
            AcquireTask task;
            this.closed = true;
            while ((task = this.pendingAcquireQueue.poll()) != null) {
                ScheduledFuture<?> f = task.timeoutFuture;
                if (f != null) {
                    f.cancel(false);
                }
                task.promise.setFailure(new ClosedChannelException());
            }
            this.acquiredChannelCount.set(0);
            this.pendingAcquireCount = 0;
            return GlobalEventExecutor.INSTANCE.submit(new Callable<Void>(){

                @Override
                public Void call() throws Exception {
                    FixedChannelPool.super.close();
                    return null;
                }
            });
        }
        return GlobalEventExecutor.INSTANCE.newSucceededFuture(null);
    }

    private static final class AcquireTimeoutException
    extends TimeoutException {
        private AcquireTimeoutException() {
            super("Acquire operation took longer then configured maximum time");
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    private class AcquireListener
    implements FutureListener<Channel> {
        private final Promise<Channel> originalPromise;
        protected boolean acquired;

        AcquireListener(Promise<Channel> originalPromise) {
            this.originalPromise = originalPromise;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {
            assert (FixedChannelPool.this.executor.inEventLoop());
            if (FixedChannelPool.this.closed) {
                if (future.isSuccess()) {
                    future.getNow().close();
                }
                this.originalPromise.setFailure(new IllegalStateException("FixedChannelPool was closed"));
                return;
            }
            if (future.isSuccess()) {
                this.originalPromise.setSuccess(future.getNow());
            } else {
                if (this.acquired) {
                    FixedChannelPool.this.decrementAndRunTaskQueue();
                } else {
                    FixedChannelPool.this.runTaskQueue();
                }
                this.originalPromise.setFailure(future.cause());
            }
        }

        public void acquired() {
            if (this.acquired) {
                return;
            }
            FixedChannelPool.this.acquiredChannelCount.incrementAndGet();
            this.acquired = true;
        }
    }

    private abstract class TimeoutTask
    implements Runnable {
        private TimeoutTask() {
        }

        @Override
        public final void run() {
            AcquireTask task;
            assert (FixedChannelPool.this.executor.inEventLoop());
            long nanoTime = System.nanoTime();
            while ((task = (AcquireTask)FixedChannelPool.this.pendingAcquireQueue.peek()) != null && nanoTime - task.expireNanoTime >= 0L) {
                FixedChannelPool.this.pendingAcquireQueue.remove();
                --FixedChannelPool.this.pendingAcquireCount;
                this.onTimeout(task);
            }
        }

        public abstract void onTimeout(AcquireTask var1);
    }

    private final class AcquireTask
    extends AcquireListener {
        final Promise<Channel> promise;
        final long expireNanoTime;
        ScheduledFuture<?> timeoutFuture;

        AcquireTask(Promise<Channel> promise) {
            super(promise);
            this.expireNanoTime = System.nanoTime() + FixedChannelPool.this.acquireTimeoutNanos;
            this.promise = FixedChannelPool.this.executor.newPromise().addListener(this);
        }
    }

    public static enum AcquireTimeoutAction {
        NEW,
        FAIL;

    }
}

