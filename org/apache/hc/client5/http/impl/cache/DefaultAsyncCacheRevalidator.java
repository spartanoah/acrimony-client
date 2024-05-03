/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.impl.Operations;
import org.apache.hc.client5.http.impl.cache.CacheKeyGenerator;
import org.apache.hc.client5.http.impl.cache.CacheRevalidatorBase;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultAsyncCacheRevalidator
extends CacheRevalidatorBase {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAsyncCacheRevalidator.class);
    private final CacheKeyGenerator cacheKeyGenerator = CacheKeyGenerator.INSTANCE;

    public DefaultAsyncCacheRevalidator(CacheRevalidatorBase.ScheduledExecutor scheduledExecutor, SchedulingStrategy schedulingStrategy) {
        super(new InternalScheduledExecutor(scheduledExecutor), schedulingStrategy);
    }

    public DefaultAsyncCacheRevalidator(ScheduledExecutorService executorService, SchedulingStrategy schedulingStrategy) {
        this(DefaultAsyncCacheRevalidator.wrap(executorService), schedulingStrategy);
    }

    public void revalidateCacheEntry(final String cacheKey, final AsyncExecCallback asyncExecCallback, final RevalidationCall call) {
        this.scheduleRevalidation(cacheKey, new Runnable(){

            @Override
            public void run() {
                call.execute(new AsyncExecCallback(){
                    private final AtomicReference<HttpResponse> responseRef = new AtomicReference<Object>(null);

                    @Override
                    public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
                        this.responseRef.set(response);
                        return asyncExecCallback.handleResponse(response, entityDetails);
                    }

                    @Override
                    public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                        asyncExecCallback.handleInformationResponse(response);
                    }

                    @Override
                    public void completed() {
                        HttpResponse httpResponse = this.responseRef.getAndSet(null);
                        if (httpResponse != null && httpResponse.getCode() < 500 && !DefaultAsyncCacheRevalidator.this.isStale(httpResponse)) {
                            DefaultAsyncCacheRevalidator.this.jobSuccessful(cacheKey);
                        } else {
                            DefaultAsyncCacheRevalidator.this.jobFailed(cacheKey);
                        }
                        asyncExecCallback.completed();
                    }

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void failed(Exception cause) {
                        if (cause instanceof IOException) {
                            LOG.debug("Asynchronous revalidation failed due to I/O error", cause);
                        } else if (cause instanceof HttpException) {
                            LOG.error("HTTP protocol exception during asynchronous revalidation", cause);
                        } else {
                            LOG.error("Unexpected runtime exception thrown during asynchronous revalidation", cause);
                        }
                        try {
                            DefaultAsyncCacheRevalidator.this.jobFailed(cacheKey);
                        } finally {
                            asyncExecCallback.failed(cause);
                        }
                    }
                });
            }
        });
    }

    static class InternalScheduledExecutor
    implements CacheRevalidatorBase.ScheduledExecutor {
        private final CacheRevalidatorBase.ScheduledExecutor executor;

        InternalScheduledExecutor(CacheRevalidatorBase.ScheduledExecutor executor) {
            this.executor = executor;
        }

        @Override
        public Future<?> schedule(Runnable command, TimeValue timeValue) throws RejectedExecutionException {
            if (timeValue.toMilliseconds() <= 0L) {
                command.run();
                return new Operations.CompletedFuture<Object>(null);
            }
            return this.executor.schedule(command, timeValue);
        }

        @Override
        public void shutdown() {
            this.executor.shutdown();
        }

        @Override
        public void awaitTermination(Timeout timeout) throws InterruptedException {
            this.executor.awaitTermination(timeout);
        }
    }

    static interface RevalidationCall {
        public void execute(AsyncExecCallback var1);
    }
}

