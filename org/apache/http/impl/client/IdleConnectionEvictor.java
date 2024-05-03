/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.client;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.util.Args;

public final class IdleConnectionEvictor {
    private final HttpClientConnectionManager connectionManager;
    private final ThreadFactory threadFactory;
    private final Thread thread;
    private final long sleepTimeMs;
    private final long maxIdleTimeMs;
    private volatile Exception exception;

    public IdleConnectionEvictor(final HttpClientConnectionManager connectionManager, ThreadFactory threadFactory, long sleepTime, TimeUnit sleepTimeUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        this.connectionManager = Args.notNull(connectionManager, "Connection manager");
        this.threadFactory = threadFactory != null ? threadFactory : new DefaultThreadFactory();
        this.sleepTimeMs = sleepTimeUnit != null ? sleepTimeUnit.toMillis(sleepTime) : sleepTime;
        this.maxIdleTimeMs = maxIdleTimeUnit != null ? maxIdleTimeUnit.toMillis(maxIdleTime) : maxIdleTime;
        this.thread = this.threadFactory.newThread(new Runnable(){

            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(IdleConnectionEvictor.this.sleepTimeMs);
                        connectionManager.closeExpiredConnections();
                        if (IdleConnectionEvictor.this.maxIdleTimeMs <= 0L) continue;
                        connectionManager.closeIdleConnections(IdleConnectionEvictor.this.maxIdleTimeMs, TimeUnit.MILLISECONDS);
                    }
                } catch (Exception ex) {
                    IdleConnectionEvictor.this.exception = ex;
                }
            }
        });
    }

    public IdleConnectionEvictor(HttpClientConnectionManager connectionManager, long sleepTime, TimeUnit sleepTimeUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        this(connectionManager, null, sleepTime, sleepTimeUnit, maxIdleTime, maxIdleTimeUnit);
    }

    public IdleConnectionEvictor(HttpClientConnectionManager connectionManager, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
        this(connectionManager, null, maxIdleTime > 0L ? maxIdleTime : 5L, maxIdleTimeUnit != null ? maxIdleTimeUnit : TimeUnit.SECONDS, maxIdleTime, maxIdleTimeUnit);
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

    public void awaitTermination(long time, TimeUnit timeUnit) throws InterruptedException {
        this.thread.join((timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS).toMillis(time));
    }

    static class DefaultThreadFactory
    implements ThreadFactory {
        DefaultThreadFactory() {
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Connection evictor");
            t.setDaemon(true);
            return t;
        }
    }
}

