/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.hc.client5.http.cache.HttpCacheInvalidator;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.cache.BasicHttpCache;
import org.apache.hc.client5.http.impl.cache.BasicHttpCacheStorage;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheKeyGenerator;
import org.apache.hc.client5.http.impl.cache.CachingExec;
import org.apache.hc.client5.http.impl.cache.DefaultCacheInvalidator;
import org.apache.hc.client5.http.impl.cache.DefaultCacheRevalidator;
import org.apache.hc.client5.http.impl.cache.FileResourceFactory;
import org.apache.hc.client5.http.impl.cache.HeapResourceFactory;
import org.apache.hc.client5.http.impl.cache.ManagedHttpCacheStorage;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.schedule.ImmediateSchedulingStrategy;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.http.config.NamedElementChain;

public class CachingHttpClientBuilder
extends HttpClientBuilder {
    private ResourceFactory resourceFactory;
    private HttpCacheStorage storage;
    private File cacheDir;
    private SchedulingStrategy schedulingStrategy;
    private CacheConfig cacheConfig;
    private HttpCacheInvalidator httpCacheInvalidator;
    private boolean deleteCache = true;

    public static CachingHttpClientBuilder create() {
        return new CachingHttpClientBuilder();
    }

    protected CachingHttpClientBuilder() {
    }

    public final CachingHttpClientBuilder setResourceFactory(ResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        return this;
    }

    public final CachingHttpClientBuilder setHttpCacheStorage(HttpCacheStorage storage) {
        this.storage = storage;
        return this;
    }

    public final CachingHttpClientBuilder setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public final CachingHttpClientBuilder setSchedulingStrategy(SchedulingStrategy schedulingStrategy) {
        this.schedulingStrategy = schedulingStrategy;
        return this;
    }

    public final CachingHttpClientBuilder setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        return this;
    }

    public final CachingHttpClientBuilder setHttpCacheInvalidator(HttpCacheInvalidator cacheInvalidator) {
        this.httpCacheInvalidator = cacheInvalidator;
        return this;
    }

    public final CachingHttpClientBuilder setDeleteCache(boolean deleteCache) {
        this.deleteCache = deleteCache;
        return this;
    }

    @Override
    protected void customizeExecChain(NamedElementChain<ExecChainHandler> execChainDefinition) {
        HttpCacheStorage storageCopy;
        CacheConfig config = this.cacheConfig != null ? this.cacheConfig : CacheConfig.DEFAULT;
        ResourceFactory resourceFactoryCopy = this.resourceFactory;
        if (resourceFactoryCopy == null) {
            resourceFactoryCopy = this.cacheDir == null ? new HeapResourceFactory() : new FileResourceFactory(this.cacheDir);
        }
        if ((storageCopy = this.storage) == null) {
            if (this.cacheDir == null) {
                storageCopy = new BasicHttpCacheStorage(config);
            } else {
                final ManagedHttpCacheStorage managedStorage = new ManagedHttpCacheStorage(config);
                if (this.deleteCache) {
                    this.addCloseable(new Closeable(){

                        @Override
                        public void close() throws IOException {
                            managedStorage.shutdown();
                        }
                    });
                } else {
                    this.addCloseable(managedStorage);
                }
                storageCopy = managedStorage;
            }
        }
        BasicHttpCache httpCache = new BasicHttpCache(resourceFactoryCopy, storageCopy, CacheKeyGenerator.INSTANCE, this.httpCacheInvalidator != null ? this.httpCacheInvalidator : new DefaultCacheInvalidator());
        DefaultCacheRevalidator cacheRevalidator = null;
        if (config.getAsynchronousWorkers() > 0) {
            final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(config.getAsynchronousWorkers());
            this.addCloseable(new Closeable(){

                @Override
                public void close() throws IOException {
                    executorService.shutdownNow();
                }
            });
            cacheRevalidator = new DefaultCacheRevalidator((ScheduledExecutorService)executorService, this.schedulingStrategy != null ? this.schedulingStrategy : ImmediateSchedulingStrategy.INSTANCE);
        }
        CachingExec cachingExec = new CachingExec(httpCache, cacheRevalidator, config);
        execChainDefinition.addBefore(ChainElement.PROTOCOL.name(), cachingExec, ChainElement.CACHING.name());
    }
}

