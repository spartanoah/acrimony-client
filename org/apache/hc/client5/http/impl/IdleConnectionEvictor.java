/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.util.concurrent.ThreadFactory;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.DefaultThreadFactory;
import org.apache.hc.core5.pool.ConnPoolControl;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public final class IdleConnectionEvictor {
    private final ThreadFactory threadFactory;
    private final Thread thread;

    public IdleConnectionEvictor(final ConnPoolControl<?> connectionManager, ThreadFactory threadFactory, TimeValue sleepTime, final TimeValue maxIdleTime) {
        Args.notNull(connectionManager, "Connection manager");
        this.threadFactory = threadFactory != null ? threadFactory : new DefaultThreadFactory("idle-connection-evictor", true);
        final TimeValue localSleepTime = sleepTime != null ? sleepTime : TimeValue.ofSeconds(5L);
        this.thread = this.threadFactory.newThread(new Runnable(){

            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        localSleepTime.sleep();
                        connectionManager.closeExpired();
                        if (maxIdleTime == null) continue;
                        connectionManager.closeIdle(maxIdleTime);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception exception) {
                    // empty catch block
                }
            }
        });
    }

    public IdleConnectionEvictor(ConnPoolControl<?> connectionManager, TimeValue sleepTime, TimeValue maxIdleTime) {
        this(connectionManager, null, sleepTime, maxIdleTime);
    }

    public IdleConnectionEvictor(ConnPoolControl<?> connectionManager, TimeValue maxIdleTime) {
        this(connectionManager, null, maxIdleTime, maxIdleTime);
    }

    public void start() {
        this.thread.start();
    }

    public void shutdown() {
        this.thread.interrupt();
    }

    public boolean isRunning() {
        return this.thread.isAlive();
    }

    public void awaitTermination(Timeout timeout) throws InterruptedException {
        this.thread.join(timeout != null ? timeout.toMilliseconds() : Long.MAX_VALUE);
    }
}

