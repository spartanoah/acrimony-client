/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.hc.client5.http.impl.cache.CacheRevalidatorBase;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultCacheRevalidator
extends CacheRevalidatorBase {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCacheRevalidator.class);

    public DefaultCacheRevalidator(CacheRevalidatorBase.ScheduledExecutor scheduledExecutor, SchedulingStrategy schedulingStrategy) {
        super(scheduledExecutor, schedulingStrategy);
    }

    public DefaultCacheRevalidator(ScheduledExecutorService scheduledThreadPoolExecutor, SchedulingStrategy schedulingStrategy) {
        this(DefaultCacheRevalidator.wrap(scheduledThreadPoolExecutor), schedulingStrategy);
    }

    public void revalidateCacheEntry(final String cacheKey, final RevalidationCall call) {
        this.scheduleRevalidation(cacheKey, new Runnable(){

            @Override
            public void run() {
                try (ClassicHttpResponse httpResponse = call.execute();){
                    if (httpResponse.getCode() < 500 && !DefaultCacheRevalidator.this.isStale(httpResponse)) {
                        DefaultCacheRevalidator.this.jobSuccessful(cacheKey);
                    } else {
                        DefaultCacheRevalidator.this.jobFailed(cacheKey);
                    }
                } catch (IOException ex) {
                    DefaultCacheRevalidator.this.jobFailed(cacheKey);
                    LOG.debug("Asynchronous revalidation failed due to I/O error", ex);
                } catch (HttpException ex) {
                    DefaultCacheRevalidator.this.jobFailed(cacheKey);
                    LOG.error("HTTP protocol exception during asynchronous revalidation", ex);
                } catch (RuntimeException ex) {
                    DefaultCacheRevalidator.this.jobFailed(cacheKey);
                    LOG.error("Unexpected runtime exception thrown during asynchronous revalidation", ex);
                }
            }
        });
    }

    static interface RevalidationCall {
        public ClassicHttpResponse execute() throws IOException, HttpException;
    }
}

