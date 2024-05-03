/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.hc.client5.http.schedule.ConcurrentCountMap;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CacheRevalidatorBase
implements Closeable {
    private final ScheduledExecutor scheduledExecutor;
    private final SchedulingStrategy schedulingStrategy;
    private final Set<String> pendingRequest;
    private final ConcurrentCountMap<String> failureCache;
    private static final Logger LOG = LoggerFactory.getLogger(CacheRevalidatorBase.class);

    public static ScheduledExecutor wrap(final ScheduledExecutorService executorService) {
        return new ScheduledExecutor(){

            public ScheduledFuture<?> schedule(Runnable command, TimeValue timeValue) throws RejectedExecutionException {
                Args.notNull(command, "Runnable");
                Args.notNull(timeValue, "Time value");
                return executorService.schedule(command, timeValue.getDuration(), timeValue.getTimeUnit());
            }

            @Override
            public void shutdown() {
                executorService.shutdown();
            }

            @Override
            public void awaitTermination(Timeout timeout) throws InterruptedException {
                Args.notNull(timeout, "Timeout");
                executorService.awaitTermination(timeout.getDuration(), timeout.getTimeUnit());
            }
        };
    }

    public CacheRevalidatorBase(ScheduledExecutor scheduledExecutor, SchedulingStrategy schedulingStrategy) {
        this.scheduledExecutor = scheduledExecutor;
        this.schedulingStrategy = schedulingStrategy;
        this.pendingRequest = new HashSet<String>();
        this.failureCache = new ConcurrentCountMap();
    }

    public CacheRevalidatorBase(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor, SchedulingStrategy schedulingStrategy) {
        this(CacheRevalidatorBase.wrap(scheduledThreadPoolExecutor), schedulingStrategy);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void scheduleRevalidation(String cacheKey, Runnable command) {
        Set<String> set = this.pendingRequest;
        synchronized (set) {
            if (!this.pendingRequest.contains(cacheKey)) {
                int consecutiveFailedAttempts = this.failureCache.getCount(cacheKey);
                TimeValue executionTime = this.schedulingStrategy.schedule(consecutiveFailedAttempts);
                try {
                    this.scheduledExecutor.schedule(command, executionTime);
                    this.pendingRequest.add(cacheKey);
                } catch (RejectedExecutionException ex) {
                    LOG.debug("Revalidation of cache entry with key {} could not be scheduled", (Object)cacheKey, (Object)ex);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.scheduledExecutor.shutdown();
    }

    public void awaitTermination(Timeout timeout) throws InterruptedException {
        Args.notNull(timeout, "Timeout");
        this.scheduledExecutor.awaitTermination(timeout);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void jobSuccessful(String identifier) {
        this.failureCache.resetCount(identifier);
        Set<String> set = this.pendingRequest;
        synchronized (set) {
            this.pendingRequest.remove(identifier);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void jobFailed(String identifier) {
        this.failureCache.increaseCount(identifier);
        Set<String> set = this.pendingRequest;
        synchronized (set) {
            this.pendingRequest.remove(identifier);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Set<String> getScheduledIdentifiers() {
        Set<String> set = this.pendingRequest;
        synchronized (set) {
            return new HashSet<String>(this.pendingRequest);
        }
    }

    boolean isStale(HttpResponse httpResponse) {
        Iterator<Header> it = httpResponse.headerIterator("Warning");
        while (it.hasNext()) {
            Header warning = it.next();
            String warningValue = warning.getValue();
            if (!warningValue.startsWith("110") && !warningValue.startsWith("111")) continue;
            return true;
        }
        return false;
    }

    static interface ScheduledExecutor {
        public Future<?> schedule(Runnable var1, TimeValue var2) throws RejectedExecutionException;

        public void shutdown();

        public void awaitTermination(Timeout var1) throws InterruptedException;
    }
}

