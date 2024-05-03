/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.client5.http.cache.HttpAsyncCacheStorage;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.RequestCopier;
import org.apache.hc.client5.http.impl.cache.BasicHttpAsyncCache;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheValidityPolicy;
import org.apache.hc.client5.http.impl.cache.CacheableRequestPolicy;
import org.apache.hc.client5.http.impl.cache.CachedHttpResponseGenerator;
import org.apache.hc.client5.http.impl.cache.CachedResponseSuitabilityChecker;
import org.apache.hc.client5.http.impl.cache.CachingExecBase;
import org.apache.hc.client5.http.impl.cache.ConditionalRequestBuilder;
import org.apache.hc.client5.http.impl.cache.DefaultAsyncCacheRevalidator;
import org.apache.hc.client5.http.impl.cache.HttpAsyncCache;
import org.apache.hc.client5.http.impl.cache.RequestProtocolCompliance;
import org.apache.hc.client5.http.impl.cache.ResponseCachingPolicy;
import org.apache.hc.client5.http.impl.cache.ResponseProtocolCompliance;
import org.apache.hc.client5.http.impl.cache.Variant;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.concurrent.ComplexFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE)
class AsyncCachingExec
extends CachingExecBase
implements AsyncExecChainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncCachingExec.class);
    private final HttpAsyncCache responseCache;
    private final DefaultAsyncCacheRevalidator cacheRevalidator;
    private final ConditionalRequestBuilder<HttpRequest> conditionalRequestBuilder;

    AsyncCachingExec(HttpAsyncCache cache, DefaultAsyncCacheRevalidator cacheRevalidator, CacheConfig config) {
        super(config);
        this.responseCache = Args.notNull(cache, "Response cache");
        this.cacheRevalidator = cacheRevalidator;
        this.conditionalRequestBuilder = new ConditionalRequestBuilder<HttpRequest>(RequestCopier.INSTANCE);
    }

    AsyncCachingExec(HttpAsyncCache responseCache, CacheValidityPolicy validityPolicy, ResponseCachingPolicy responseCachingPolicy, CachedHttpResponseGenerator responseGenerator, CacheableRequestPolicy cacheableRequestPolicy, CachedResponseSuitabilityChecker suitabilityChecker, ResponseProtocolCompliance responseCompliance, RequestProtocolCompliance requestCompliance, DefaultAsyncCacheRevalidator cacheRevalidator, ConditionalRequestBuilder<HttpRequest> conditionalRequestBuilder, CacheConfig config) {
        super(validityPolicy, responseCachingPolicy, responseGenerator, cacheableRequestPolicy, suitabilityChecker, responseCompliance, requestCompliance, config);
        this.responseCache = responseCache;
        this.cacheRevalidator = cacheRevalidator;
        this.conditionalRequestBuilder = conditionalRequestBuilder;
    }

    AsyncCachingExec(HttpAsyncCache cache, ScheduledExecutorService executorService, SchedulingStrategy schedulingStrategy, CacheConfig config) {
        this(cache, executorService != null ? new DefaultAsyncCacheRevalidator(executorService, schedulingStrategy) : null, config);
    }

    AsyncCachingExec(ResourceFactory resourceFactory, HttpAsyncCacheStorage storage, ScheduledExecutorService executorService, SchedulingStrategy schedulingStrategy, CacheConfig config) {
        this(new BasicHttpAsyncCache(resourceFactory, storage), executorService, schedulingStrategy, config);
    }

    private void triggerResponse(SimpleHttpResponse cacheResponse, AsyncExecChain.Scope scope, AsyncExecCallback asyncExecCallback) {
        scope.clientContext.setAttribute("http.response", cacheResponse);
        scope.execRuntime.releaseEndpoint();
        SimpleBody body = cacheResponse.getBody();
        byte[] content = body != null ? body.getBodyBytes() : null;
        ContentType contentType = body != null ? body.getContentType() : null;
        try {
            AsyncDataConsumer dataConsumer = asyncExecCallback.handleResponse(cacheResponse, content != null ? new BasicEntityDetails(content.length, contentType) : null);
            if (dataConsumer != null) {
                dataConsumer.consume(ByteBuffer.wrap(content));
                dataConsumer.streamEnd(null);
            }
            asyncExecCallback.completed();
        } catch (IOException | HttpException ex) {
            asyncExecCallback.failed(ex);
        }
    }

    @Override
    public void execute(final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        HttpRoute route = scope.route;
        CancellableDependency operation = scope.cancellableDependency;
        HttpClientContext context = scope.clientContext;
        context.setAttribute("http.route", route);
        context.setAttribute("http.request", request);
        URIAuthority authority = request.getAuthority();
        String scheme = request.getScheme();
        final HttpHost target = authority != null ? new HttpHost(scheme, authority) : route.getTargetHost();
        String via = this.generateViaHeader(request);
        this.setResponseStatus(context, CacheResponseStatus.CACHE_MISS);
        if (this.clientRequestsOurOptions(request)) {
            this.setResponseStatus(context, CacheResponseStatus.CACHE_MODULE_RESPONSE);
            this.triggerResponse(SimpleHttpResponse.create(501), scope, asyncExecCallback);
            return;
        }
        SimpleHttpResponse fatalErrorResponse = this.getFatallyNoncompliantResponse(request, context);
        if (fatalErrorResponse != null) {
            this.triggerResponse(fatalErrorResponse, scope, asyncExecCallback);
            return;
        }
        this.requestCompliance.makeRequestCompliant(request);
        request.addHeader("Via", via);
        if (!this.cacheableRequestPolicy.isServableFromCache(request)) {
            LOG.debug("Request is not servable from cache");
            operation.setDependency(this.responseCache.flushCacheEntriesInvalidatedByRequest(target, request, new FutureCallback<Boolean>(){

                @Override
                public void completed(Boolean result) {
                    AsyncCachingExec.this.callBackend(target, request, entityProducer, scope, chain, asyncExecCallback);
                }

                @Override
                public void failed(Exception cause) {
                    asyncExecCallback.failed(cause);
                }

                @Override
                public void cancelled() {
                    asyncExecCallback.failed(new InterruptedIOException());
                }
            }));
        } else {
            operation.setDependency(this.responseCache.getCacheEntry(target, request, new FutureCallback<HttpCacheEntry>(){

                @Override
                public void completed(HttpCacheEntry entry) {
                    if (entry == null) {
                        LOG.debug("Cache miss");
                        AsyncCachingExec.this.handleCacheMiss(target, request, entityProducer, scope, chain, asyncExecCallback);
                    } else {
                        AsyncCachingExec.this.handleCacheHit(target, request, entityProducer, scope, chain, asyncExecCallback, entry);
                    }
                }

                @Override
                public void failed(Exception cause) {
                    asyncExecCallback.failed(cause);
                }

                @Override
                public void cancelled() {
                    asyncExecCallback.failed(new InterruptedIOException());
                }
            }));
        }
    }

    void chainProceed(HttpRequest request, AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, AsyncExecCallback asyncExecCallback) {
        try {
            chain.proceed(request, entityProducer, scope, asyncExecCallback);
        } catch (IOException | HttpException ex) {
            asyncExecCallback.failed(ex);
        }
    }

    void callBackend(final HttpHost target, final HttpRequest request, AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) {
        LOG.debug("Calling the backend");
        final Date requestDate = this.getCurrentDate();
        final AtomicReference callbackRef = new AtomicReference();
        this.chainProceed(request, entityProducer, scope, chain, new AsyncExecCallback(){

            @Override
            public AsyncDataConsumer handleResponse(HttpResponse backendResponse, EntityDetails entityDetails) throws HttpException, IOException {
                Date responseDate = AsyncCachingExec.this.getCurrentDate();
                backendResponse.addHeader("Via", AsyncCachingExec.this.generateViaHeader(backendResponse));
                BackendResponseHandler callback = new BackendResponseHandler(target, request, requestDate, responseDate, scope, asyncExecCallback);
                callbackRef.set(callback);
                return callback.handleResponse(backendResponse, entityDetails);
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                AsyncExecCallback callback = callbackRef.getAndSet(null);
                if (callback != null) {
                    callback.handleInformationResponse(response);
                } else {
                    asyncExecCallback.handleInformationResponse(response);
                }
            }

            @Override
            public void completed() {
                AsyncExecCallback callback = callbackRef.getAndSet(null);
                if (callback != null) {
                    callback.completed();
                } else {
                    asyncExecCallback.completed();
                }
            }

            @Override
            public void failed(Exception cause) {
                AsyncExecCallback callback = callbackRef.getAndSet(null);
                if (callback != null) {
                    callback.failed(cause);
                } else {
                    asyncExecCallback.failed(cause);
                }
            }
        });
    }

    private void handleCacheHit(final HttpHost target, final HttpRequest request, final AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope, final AsyncExecChain chain, AsyncExecCallback asyncExecCallback, final HttpCacheEntry entry) {
        block15: {
            HttpClientContext context = scope.clientContext;
            this.recordCacheHit(target, request);
            Date now = this.getCurrentDate();
            if (this.suitabilityChecker.canCachedResponseBeUsed(target, request, entry, now)) {
                LOG.debug("Cache hit");
                try {
                    SimpleHttpResponse cacheResponse = this.generateCachedResponse(request, context, entry, now);
                    this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                } catch (ResourceIOException ex) {
                    this.recordCacheFailure(target, request);
                    if (!this.mayCallBackend(request)) {
                        SimpleHttpResponse cacheResponse = this.generateGatewayTimeout(context);
                        this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                        break block15;
                    }
                    this.setResponseStatus(scope.clientContext, CacheResponseStatus.FAILURE);
                    try {
                        chain.proceed(request, entityProducer, scope, asyncExecCallback);
                    } catch (IOException | HttpException ex2) {
                        asyncExecCallback.failed(ex2);
                    }
                }
            } else if (!this.mayCallBackend(request)) {
                LOG.debug("Cache entry not suitable but only-if-cached requested");
                SimpleHttpResponse cacheResponse = this.generateGatewayTimeout(context);
                this.triggerResponse(cacheResponse, scope, asyncExecCallback);
            } else if (entry.getStatus() != 304 || this.suitabilityChecker.isConditional(request)) {
                LOG.debug("Revalidating cache entry");
                if (this.cacheRevalidator != null && !this.staleResponseNotAllowed(request, entry, now) && this.validityPolicy.mayReturnStaleWhileRevalidating(entry, now)) {
                    LOG.debug("Serving stale with asynchronous revalidation");
                    try {
                        SimpleHttpResponse cacheResponse = this.generateCachedResponse(request, context, entry, now);
                        String exchangeId = ExecSupport.getNextExchangeId();
                        final AsyncExecChain.Scope fork = new AsyncExecChain.Scope(exchangeId, scope.route, scope.originalRequest, new ComplexFuture(null), HttpClientContext.create(), scope.execRuntime.fork());
                        this.cacheRevalidator.revalidateCacheEntry(this.responseCache.generateKey(target, request, entry), asyncExecCallback, new DefaultAsyncCacheRevalidator.RevalidationCall(){

                            @Override
                            public void execute(AsyncExecCallback asyncExecCallback) {
                                AsyncCachingExec.this.revalidateCacheEntry(target, request, entityProducer, fork, chain, asyncExecCallback, entry);
                            }
                        });
                        this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                    } catch (ResourceIOException ex) {
                        asyncExecCallback.failed(ex);
                    }
                } else {
                    this.revalidateCacheEntry(target, request, entityProducer, scope, chain, asyncExecCallback, entry);
                }
            } else {
                LOG.debug("Cache entry not usable; calling backend");
                this.callBackend(target, request, entityProducer, scope, chain, asyncExecCallback);
            }
        }
    }

    void revalidateCacheEntry(final HttpHost target, final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback, final HttpCacheEntry cacheEntry) {
        final Date requestDate = this.getCurrentDate();
        final HttpRequest conditionalRequest = this.conditionalRequestBuilder.buildConditionalRequest(scope.originalRequest, cacheEntry);
        this.chainProceed(conditionalRequest, entityProducer, scope, chain, new AsyncExecCallback(){
            final AtomicReference<AsyncExecCallback> callbackRef = new AtomicReference();

            void triggerUpdatedCacheEntryResponse(HttpResponse backendResponse, Date responseDate) {
                CancellableDependency operation = scope.cancellableDependency;
                AsyncCachingExec.this.recordCacheUpdate(scope.clientContext);
                operation.setDependency(AsyncCachingExec.this.responseCache.updateCacheEntry(target, request, cacheEntry, backendResponse, requestDate, responseDate, new FutureCallback<HttpCacheEntry>(){

                    @Override
                    public void completed(HttpCacheEntry updatedEntry) {
                        if (AsyncCachingExec.this.suitabilityChecker.isConditional(request) && AsyncCachingExec.this.suitabilityChecker.allConditionalsMatch(request, updatedEntry, new Date())) {
                            SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateNotModifiedResponse(updatedEntry);
                            AsyncCachingExec.this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                        } else {
                            try {
                                SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateResponse(request, updatedEntry);
                                AsyncCachingExec.this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                            } catch (ResourceIOException ex) {
                                asyncExecCallback.failed(ex);
                            }
                        }
                    }

                    @Override
                    public void failed(Exception ex) {
                        asyncExecCallback.failed(ex);
                    }

                    @Override
                    public void cancelled() {
                        asyncExecCallback.failed(new InterruptedIOException());
                    }
                }));
            }

            void triggerResponseStaleCacheEntry() {
                try {
                    SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateResponse(request, cacheEntry);
                    cacheResponse.addHeader("Warning", "110 localhost \"Response is stale\"");
                    AsyncCachingExec.this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                } catch (ResourceIOException ex) {
                    asyncExecCallback.failed(ex);
                }
            }

            AsyncExecCallback evaluateResponse(final HttpResponse backendResponse, final Date responseDate) {
                backendResponse.addHeader("Via", AsyncCachingExec.this.generateViaHeader(backendResponse));
                int statusCode = backendResponse.getCode();
                if (statusCode == 304 || statusCode == 200) {
                    AsyncCachingExec.this.recordCacheUpdate(scope.clientContext);
                }
                if (statusCode == 304) {
                    return new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                        @Override
                        public void run() {
                            this.triggerUpdatedCacheEntryResponse(backendResponse, responseDate);
                        }
                    });
                }
                if (AsyncCachingExec.this.staleIfErrorAppliesTo(statusCode) && !AsyncCachingExec.this.staleResponseNotAllowed(request, cacheEntry, AsyncCachingExec.this.getCurrentDate()) && AsyncCachingExec.this.validityPolicy.mayReturnStaleIfError(request, cacheEntry, responseDate)) {
                    return new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                        @Override
                        public void run() {
                            this.triggerResponseStaleCacheEntry();
                        }
                    });
                }
                return new BackendResponseHandler(target, conditionalRequest, requestDate, responseDate, scope, asyncExecCallback);
            }

            @Override
            public AsyncDataConsumer handleResponse(HttpResponse backendResponse1, EntityDetails entityDetails) throws HttpException, IOException {
                AsyncExecCallback callback1;
                Date responseDate1 = AsyncCachingExec.this.getCurrentDate();
                if (AsyncCachingExec.this.revalidationResponseIsTooOld(backendResponse1, cacheEntry) && (entityProducer == null || entityProducer.isRepeatable())) {
                    final HttpRequest unconditional = AsyncCachingExec.this.conditionalRequestBuilder.buildUnconditionalRequest(scope.originalRequest);
                    callback1 = new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                        @Override
                        public void run() {
                            AsyncCachingExec.this.chainProceed(unconditional, entityProducer, scope, chain, new AsyncExecCallback(){

                                @Override
                                public AsyncDataConsumer handleResponse(HttpResponse backendResponse2, EntityDetails entityDetails) throws HttpException, IOException {
                                    Date responseDate2 = AsyncCachingExec.this.getCurrentDate();
                                    AsyncExecCallback callback2 = this.evaluateResponse(backendResponse2, responseDate2);
                                    callbackRef.set(callback2);
                                    return callback2.handleResponse(backendResponse2, entityDetails);
                                }

                                @Override
                                public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                                    AsyncExecCallback callback2 = callbackRef.getAndSet(null);
                                    if (callback2 != null) {
                                        callback2.handleInformationResponse(response);
                                    } else {
                                        asyncExecCallback.handleInformationResponse(response);
                                    }
                                }

                                @Override
                                public void completed() {
                                    AsyncExecCallback callback2 = callbackRef.getAndSet(null);
                                    if (callback2 != null) {
                                        callback2.completed();
                                    } else {
                                        asyncExecCallback.completed();
                                    }
                                }

                                @Override
                                public void failed(Exception cause) {
                                    AsyncExecCallback callback2 = callbackRef.getAndSet(null);
                                    if (callback2 != null) {
                                        callback2.failed(cause);
                                    } else {
                                        asyncExecCallback.failed(cause);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    callback1 = this.evaluateResponse(backendResponse1, responseDate1);
                }
                this.callbackRef.set(callback1);
                return callback1.handleResponse(backendResponse1, entityDetails);
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                AsyncExecCallback callback1 = this.callbackRef.getAndSet(null);
                if (callback1 != null) {
                    callback1.handleInformationResponse(response);
                } else {
                    asyncExecCallback.handleInformationResponse(response);
                }
            }

            @Override
            public void completed() {
                AsyncExecCallback callback1 = this.callbackRef.getAndSet(null);
                if (callback1 != null) {
                    callback1.completed();
                } else {
                    asyncExecCallback.completed();
                }
            }

            @Override
            public void failed(Exception cause) {
                AsyncExecCallback callback1 = this.callbackRef.getAndSet(null);
                if (callback1 != null) {
                    callback1.failed(cause);
                } else {
                    asyncExecCallback.failed(cause);
                }
            }
        });
    }

    private void handleCacheMiss(final HttpHost target, final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback) {
        this.recordCacheMiss(target, request);
        if (this.mayCallBackend(request)) {
            CancellableDependency operation = scope.cancellableDependency;
            operation.setDependency(this.responseCache.getVariantCacheEntriesWithEtags(target, request, new FutureCallback<Map<String, Variant>>(){

                @Override
                public void completed(Map<String, Variant> variants) {
                    if (variants != null && !variants.isEmpty() && (entityProducer == null || entityProducer.isRepeatable())) {
                        AsyncCachingExec.this.negotiateResponseFromVariants(target, request, entityProducer, scope, chain, asyncExecCallback, variants);
                    } else {
                        AsyncCachingExec.this.callBackend(target, request, entityProducer, scope, chain, asyncExecCallback);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    asyncExecCallback.failed(ex);
                }

                @Override
                public void cancelled() {
                    asyncExecCallback.failed(new InterruptedIOException());
                }
            }));
        } else {
            SimpleHttpResponse cacheResponse = SimpleHttpResponse.create(504, "Gateway Timeout");
            this.triggerResponse(cacheResponse, scope, asyncExecCallback);
        }
    }

    void negotiateResponseFromVariants(final HttpHost target, final HttpRequest request, final AsyncEntityProducer entityProducer, final AsyncExecChain.Scope scope, final AsyncExecChain chain, final AsyncExecCallback asyncExecCallback, final Map<String, Variant> variants) {
        final CancellableDependency operation = scope.cancellableDependency;
        final HttpRequest conditionalRequest = this.conditionalRequestBuilder.buildConditionalRequestFromVariants(request, variants);
        final Date requestDate = this.getCurrentDate();
        this.chainProceed(conditionalRequest, entityProducer, scope, chain, new AsyncExecCallback(){
            final AtomicReference<AsyncExecCallback> callbackRef = new AtomicReference();

            void updateVariantCacheEntry(HttpResponse backendResponse, Date responseDate, final Variant matchingVariant) {
                AsyncCachingExec.this.recordCacheUpdate(scope.clientContext);
                operation.setDependency(AsyncCachingExec.this.responseCache.updateVariantCacheEntry(target, conditionalRequest, backendResponse, matchingVariant, requestDate, responseDate, new FutureCallback<HttpCacheEntry>(){

                    @Override
                    public void completed(HttpCacheEntry responseEntry) {
                        if (AsyncCachingExec.this.shouldSendNotModifiedResponse(request, responseEntry)) {
                            SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateNotModifiedResponse(responseEntry);
                            AsyncCachingExec.this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                        } else {
                            try {
                                final SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateResponse(request, responseEntry);
                                operation.setDependency(AsyncCachingExec.this.responseCache.reuseVariantEntryFor(target, request, matchingVariant, new FutureCallback<Boolean>(){

                                    @Override
                                    public void completed(Boolean result) {
                                        AsyncCachingExec.this.triggerResponse(cacheResponse, scope, asyncExecCallback);
                                    }

                                    @Override
                                    public void failed(Exception ex) {
                                        asyncExecCallback.failed(ex);
                                    }

                                    @Override
                                    public void cancelled() {
                                        asyncExecCallback.failed(new InterruptedIOException());
                                    }
                                }));
                            } catch (ResourceIOException ex) {
                                asyncExecCallback.failed(ex);
                            }
                        }
                    }

                    @Override
                    public void failed(Exception ex) {
                        asyncExecCallback.failed(ex);
                    }

                    @Override
                    public void cancelled() {
                        asyncExecCallback.failed(new InterruptedIOException());
                    }
                }));
            }

            @Override
            public AsyncDataConsumer handleResponse(final HttpResponse backendResponse, EntityDetails entityDetails) throws HttpException, IOException {
                AsyncExecCallback callback;
                final Date responseDate = AsyncCachingExec.this.getCurrentDate();
                backendResponse.addHeader("Via", AsyncCachingExec.this.generateViaHeader(backendResponse));
                if (backendResponse.getCode() != 304) {
                    callback = new BackendResponseHandler(target, request, requestDate, responseDate, scope, asyncExecCallback);
                } else {
                    Header resultEtagHeader = backendResponse.getFirstHeader("ETag");
                    if (resultEtagHeader == null) {
                        LOG.warn("304 response did not contain ETag");
                        callback = new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                            @Override
                            public void run() {
                                AsyncCachingExec.this.callBackend(target, request, entityProducer, scope, chain, asyncExecCallback);
                            }
                        });
                    } else {
                        String resultEtag = resultEtagHeader.getValue();
                        final Variant matchingVariant = (Variant)variants.get(resultEtag);
                        if (matchingVariant == null) {
                            LOG.debug("304 response did not contain ETag matching one sent in If-None-Match");
                            callback = new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                                @Override
                                public void run() {
                                    AsyncCachingExec.this.callBackend(target, request, entityProducer, scope, chain, asyncExecCallback);
                                }
                            });
                        } else if (AsyncCachingExec.this.revalidationResponseIsTooOld(backendResponse, matchingVariant.getEntry())) {
                            HttpRequest unconditional = AsyncCachingExec.this.conditionalRequestBuilder.buildUnconditionalRequest(request);
                            scope.clientContext.setAttribute("http.request", unconditional);
                            callback = new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                                @Override
                                public void run() {
                                    AsyncCachingExec.this.callBackend(target, request, entityProducer, scope, chain, asyncExecCallback);
                                }
                            });
                        } else {
                            callback = new AsyncExecCallbackWrapper(asyncExecCallback, new Runnable(){

                                @Override
                                public void run() {
                                    this.updateVariantCacheEntry(backendResponse, responseDate, matchingVariant);
                                }
                            });
                        }
                    }
                }
                this.callbackRef.set(callback);
                return callback.handleResponse(backendResponse, entityDetails);
            }

            @Override
            public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                AsyncExecCallback callback = this.callbackRef.getAndSet(null);
                if (callback != null) {
                    callback.handleInformationResponse(response);
                } else {
                    asyncExecCallback.handleInformationResponse(response);
                }
            }

            @Override
            public void completed() {
                AsyncExecCallback callback = this.callbackRef.getAndSet(null);
                if (callback != null) {
                    callback.completed();
                } else {
                    asyncExecCallback.completed();
                }
            }

            @Override
            public void failed(Exception cause) {
                AsyncExecCallback callback = this.callbackRef.getAndSet(null);
                if (callback != null) {
                    callback.failed(cause);
                } else {
                    asyncExecCallback.failed(cause);
                }
            }
        });
    }

    class BackendResponseHandler
    implements AsyncExecCallback {
        private final HttpHost target;
        private final HttpRequest request;
        private final Date requestDate;
        private final Date responseDate;
        private final AsyncExecChain.Scope scope;
        private final AsyncExecCallback asyncExecCallback;
        private final AtomicReference<CachingAsyncDataConsumer> cachingConsumerRef;

        BackendResponseHandler(HttpHost target, HttpRequest request, Date requestDate, Date responseDate, AsyncExecChain.Scope scope, AsyncExecCallback asyncExecCallback) {
            this.target = target;
            this.request = request;
            this.requestDate = requestDate;
            this.responseDate = responseDate;
            this.scope = scope;
            this.asyncExecCallback = asyncExecCallback;
            this.cachingConsumerRef = new AtomicReference();
        }

        @Override
        public AsyncDataConsumer handleResponse(HttpResponse backendResponse, EntityDetails entityDetails) throws HttpException, IOException {
            AsyncCachingExec.this.responseCompliance.ensureProtocolCompliance(this.scope.originalRequest, this.request, backendResponse);
            AsyncCachingExec.this.responseCache.flushCacheEntriesInvalidatedByExchange(this.target, this.request, backendResponse, new FutureCallback<Boolean>(){

                @Override
                public void completed(Boolean result) {
                }

                @Override
                public void failed(Exception ex) {
                    LOG.warn("Unable to flush invalidated entries from cache", ex);
                }

                @Override
                public void cancelled() {
                }
            });
            boolean cacheable = AsyncCachingExec.this.responseCachingPolicy.isResponseCacheable(this.request, backendResponse);
            if (cacheable) {
                this.cachingConsumerRef.set(new CachingAsyncDataConsumer(this.asyncExecCallback, backendResponse, entityDetails));
                AsyncCachingExec.this.storeRequestIfModifiedSinceFor304Response(this.request, backendResponse);
            } else {
                LOG.debug("Backend response is not cacheable");
                AsyncCachingExec.this.responseCache.flushCacheEntriesFor(this.target, this.request, new FutureCallback<Boolean>(){

                    @Override
                    public void completed(Boolean result) {
                    }

                    @Override
                    public void failed(Exception ex) {
                        LOG.warn("Unable to flush invalidated entries from cache", ex);
                    }

                    @Override
                    public void cancelled() {
                    }
                });
            }
            CachingAsyncDataConsumer cachingDataConsumer = this.cachingConsumerRef.get();
            if (cachingDataConsumer != null) {
                LOG.debug("Caching backend response");
                return cachingDataConsumer;
            }
            return this.asyncExecCallback.handleResponse(backendResponse, entityDetails);
        }

        @Override
        public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
            this.asyncExecCallback.handleInformationResponse(response);
        }

        void triggerNewCacheEntryResponse(HttpResponse backendResponse, Date responseDate, ByteArrayBuffer buffer) {
            CancellableDependency operation = this.scope.cancellableDependency;
            operation.setDependency(AsyncCachingExec.this.responseCache.createCacheEntry(this.target, this.request, backendResponse, buffer, this.requestDate, responseDate, new FutureCallback<HttpCacheEntry>(){

                @Override
                public void completed(HttpCacheEntry newEntry) {
                    LOG.debug("Backend response successfully cached");
                    try {
                        SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateResponse(BackendResponseHandler.this.request, newEntry);
                        AsyncCachingExec.this.triggerResponse(cacheResponse, BackendResponseHandler.this.scope, BackendResponseHandler.this.asyncExecCallback);
                    } catch (ResourceIOException ex) {
                        BackendResponseHandler.this.asyncExecCallback.failed(ex);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    BackendResponseHandler.this.asyncExecCallback.failed(ex);
                }

                @Override
                public void cancelled() {
                    BackendResponseHandler.this.asyncExecCallback.failed(new InterruptedIOException());
                }
            }));
        }

        @Override
        public void completed() {
            CachingAsyncDataConsumer cachingDataConsumer = this.cachingConsumerRef.getAndSet(null);
            if (cachingDataConsumer != null && !cachingDataConsumer.writtenThrough.get()) {
                final ByteArrayBuffer buffer = cachingDataConsumer.bufferRef.getAndSet(null);
                final HttpResponse backendResponse = cachingDataConsumer.backendResponse;
                if (AsyncCachingExec.this.cacheConfig.isFreshnessCheckEnabled()) {
                    CancellableDependency operation = this.scope.cancellableDependency;
                    operation.setDependency(AsyncCachingExec.this.responseCache.getCacheEntry(this.target, this.request, new FutureCallback<HttpCacheEntry>(){

                        @Override
                        public void completed(HttpCacheEntry existingEntry) {
                            if (DateUtils.isAfter(existingEntry, backendResponse, "Date")) {
                                LOG.debug("Backend already contains fresher cache entry");
                                try {
                                    SimpleHttpResponse cacheResponse = AsyncCachingExec.this.responseGenerator.generateResponse(BackendResponseHandler.this.request, existingEntry);
                                    AsyncCachingExec.this.triggerResponse(cacheResponse, BackendResponseHandler.this.scope, BackendResponseHandler.this.asyncExecCallback);
                                } catch (ResourceIOException ex) {
                                    BackendResponseHandler.this.asyncExecCallback.failed(ex);
                                }
                            } else {
                                BackendResponseHandler.this.triggerNewCacheEntryResponse(backendResponse, BackendResponseHandler.this.responseDate, buffer);
                            }
                        }

                        @Override
                        public void failed(Exception cause) {
                            BackendResponseHandler.this.asyncExecCallback.failed(cause);
                        }

                        @Override
                        public void cancelled() {
                            BackendResponseHandler.this.asyncExecCallback.failed(new InterruptedIOException());
                        }
                    }));
                } else {
                    this.triggerNewCacheEntryResponse(backendResponse, this.responseDate, buffer);
                }
            } else {
                this.asyncExecCallback.completed();
            }
        }

        @Override
        public void failed(Exception cause) {
            this.asyncExecCallback.failed(cause);
        }
    }

    class CachingAsyncDataConsumer
    implements AsyncDataConsumer {
        private final AsyncExecCallback fallback;
        private final HttpResponse backendResponse;
        private final EntityDetails entityDetails;
        private final AtomicBoolean writtenThrough;
        private final AtomicReference<ByteArrayBuffer> bufferRef;
        private final AtomicReference<AsyncDataConsumer> dataConsumerRef;

        CachingAsyncDataConsumer(AsyncExecCallback fallback, HttpResponse backendResponse, EntityDetails entityDetails) {
            this.fallback = fallback;
            this.backendResponse = backendResponse;
            this.entityDetails = entityDetails;
            this.writtenThrough = new AtomicBoolean(false);
            this.bufferRef = new AtomicReference<ByteArrayBuffer>(entityDetails != null ? new ByteArrayBuffer(1024) : null);
            this.dataConsumerRef = new AtomicReference();
        }

        @Override
        public final void updateCapacity(CapacityChannel capacityChannel) throws IOException {
            AsyncDataConsumer dataConsumer = this.dataConsumerRef.get();
            if (dataConsumer != null) {
                dataConsumer.updateCapacity(capacityChannel);
            } else {
                capacityChannel.update(Integer.MAX_VALUE);
            }
        }

        @Override
        public final void consume(ByteBuffer src) throws IOException {
            ByteArrayBuffer buffer = this.bufferRef.get();
            if (buffer != null) {
                if (src.hasArray()) {
                    buffer.append(src.array(), src.arrayOffset() + src.position(), src.remaining());
                } else {
                    while (src.hasRemaining()) {
                        buffer.append(src.get());
                    }
                }
                if ((long)buffer.length() > AsyncCachingExec.this.cacheConfig.getMaxObjectSize()) {
                    LOG.debug("Backend response content length exceeds maximum");
                    this.bufferRef.set(null);
                    try {
                        AsyncDataConsumer dataConsumer = this.fallback.handleResponse(this.backendResponse, this.entityDetails);
                        if (dataConsumer != null) {
                            this.dataConsumerRef.set(dataConsumer);
                            this.writtenThrough.set(true);
                            dataConsumer.consume(ByteBuffer.wrap(buffer.array(), 0, buffer.length()));
                        }
                    } catch (HttpException ex) {
                        this.fallback.failed(ex);
                    }
                }
            } else {
                AsyncDataConsumer dataConsumer = this.dataConsumerRef.get();
                if (dataConsumer != null) {
                    dataConsumer.consume(src);
                }
            }
        }

        @Override
        public final void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
            AsyncDataConsumer dataConsumer = this.dataConsumerRef.getAndSet(null);
            if (dataConsumer != null) {
                dataConsumer.streamEnd(trailers);
            }
        }

        @Override
        public void releaseResources() {
            AsyncDataConsumer dataConsumer = this.dataConsumerRef.getAndSet(null);
            if (dataConsumer != null) {
                dataConsumer.releaseResources();
            }
        }
    }

    static class AsyncExecCallbackWrapper
    implements AsyncExecCallback {
        private final AsyncExecCallback asyncExecCallback;
        private final Runnable command;

        AsyncExecCallbackWrapper(AsyncExecCallback asyncExecCallback, Runnable command) {
            this.asyncExecCallback = asyncExecCallback;
            this.command = command;
        }

        @Override
        public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails) throws HttpException, IOException {
            return null;
        }

        @Override
        public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
        }

        @Override
        public void completed() {
            this.command.run();
        }

        @Override
        public void failed(Exception cause) {
            this.asyncExecCallback.failed(cause);
        }
    }
}

