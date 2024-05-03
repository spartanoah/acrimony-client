/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.ExecSupport;
import org.apache.hc.client5.http.impl.cache.BasicHttpCache;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheValidityPolicy;
import org.apache.hc.client5.http.impl.cache.CacheableRequestPolicy;
import org.apache.hc.client5.http.impl.cache.CachedHttpResponseGenerator;
import org.apache.hc.client5.http.impl.cache.CachedResponseSuitabilityChecker;
import org.apache.hc.client5.http.impl.cache.CachingExecBase;
import org.apache.hc.client5.http.impl.cache.CombinedEntity;
import org.apache.hc.client5.http.impl.cache.ConditionalRequestBuilder;
import org.apache.hc.client5.http.impl.cache.DefaultCacheRevalidator;
import org.apache.hc.client5.http.impl.cache.HttpCache;
import org.apache.hc.client5.http.impl.cache.RequestProtocolCompliance;
import org.apache.hc.client5.http.impl.cache.ResponseCachingPolicy;
import org.apache.hc.client5.http.impl.cache.ResponseProtocolCompliance;
import org.apache.hc.client5.http.impl.cache.Variant;
import org.apache.hc.client5.http.impl.classic.ClassicRequestCopier;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CachingExec
extends CachingExecBase
implements ExecChainHandler {
    private final HttpCache responseCache;
    private final DefaultCacheRevalidator cacheRevalidator;
    private final ConditionalRequestBuilder<ClassicHttpRequest> conditionalRequestBuilder;
    private static final Logger LOG = LoggerFactory.getLogger(CachingExec.class);

    CachingExec(HttpCache cache, DefaultCacheRevalidator cacheRevalidator, CacheConfig config) {
        super(config);
        this.responseCache = Args.notNull(cache, "Response cache");
        this.cacheRevalidator = cacheRevalidator;
        this.conditionalRequestBuilder = new ConditionalRequestBuilder<ClassicHttpRequest>(ClassicRequestCopier.INSTANCE);
    }

    CachingExec(HttpCache responseCache, CacheValidityPolicy validityPolicy, ResponseCachingPolicy responseCachingPolicy, CachedHttpResponseGenerator responseGenerator, CacheableRequestPolicy cacheableRequestPolicy, CachedResponseSuitabilityChecker suitabilityChecker, ResponseProtocolCompliance responseCompliance, RequestProtocolCompliance requestCompliance, DefaultCacheRevalidator cacheRevalidator, ConditionalRequestBuilder<ClassicHttpRequest> conditionalRequestBuilder, CacheConfig config) {
        super(validityPolicy, responseCachingPolicy, responseGenerator, cacheableRequestPolicy, suitabilityChecker, responseCompliance, requestCompliance, config);
        this.responseCache = responseCache;
        this.cacheRevalidator = cacheRevalidator;
        this.conditionalRequestBuilder = conditionalRequestBuilder;
    }

    CachingExec(HttpCache cache, ScheduledExecutorService executorService, SchedulingStrategy schedulingStrategy, CacheConfig config) {
        this(cache, executorService != null ? new DefaultCacheRevalidator(executorService, schedulingStrategy) : null, config);
    }

    CachingExec(ResourceFactory resourceFactory, HttpCacheStorage storage, ScheduledExecutorService executorService, SchedulingStrategy schedulingStrategy, CacheConfig config) {
        this(new BasicHttpCache(resourceFactory, storage), executorService, schedulingStrategy, config);
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        Args.notNull(request, "HTTP request");
        Args.notNull(scope, "Scope");
        HttpRoute route = scope.route;
        HttpClientContext context = scope.clientContext;
        context.setAttribute("http.route", scope.route);
        context.setAttribute("http.request", request);
        URIAuthority authority = request.getAuthority();
        String scheme = request.getScheme();
        HttpHost target = authority != null ? new HttpHost(scheme, authority) : route.getTargetHost();
        String via = this.generateViaHeader(request);
        this.setResponseStatus(context, CacheResponseStatus.CACHE_MISS);
        if (this.clientRequestsOurOptions(request)) {
            this.setResponseStatus(context, CacheResponseStatus.CACHE_MODULE_RESPONSE);
            return new BasicClassicHttpResponse(501);
        }
        SimpleHttpResponse fatalErrorResponse = this.getFatallyNoncompliantResponse(request, context);
        if (fatalErrorResponse != null) {
            return CachingExec.convert(fatalErrorResponse, scope);
        }
        this.requestCompliance.makeRequestCompliant(request);
        request.addHeader("Via", via);
        if (!this.cacheableRequestPolicy.isServableFromCache(request)) {
            LOG.debug("Request is not servable from cache");
            this.responseCache.flushCacheEntriesInvalidatedByRequest(target, request);
            return this.callBackend(target, request, scope, chain);
        }
        HttpCacheEntry entry = this.responseCache.getCacheEntry(target, request);
        if (entry == null) {
            LOG.debug("Cache miss");
            return this.handleCacheMiss(target, request, scope, chain);
        }
        return this.handleCacheHit(target, request, scope, chain, entry);
    }

    private static ClassicHttpResponse convert(SimpleHttpResponse cacheResponse, ExecChain.Scope scope) {
        if (cacheResponse == null) {
            return null;
        }
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(cacheResponse.getCode(), cacheResponse.getReasonPhrase());
        Iterator<Header> it = cacheResponse.headerIterator();
        while (it.hasNext()) {
            response.addHeader(it.next());
        }
        response.setVersion(cacheResponse.getVersion() != null ? cacheResponse.getVersion() : HttpVersion.DEFAULT);
        SimpleBody body = cacheResponse.getBody();
        if (body != null) {
            if (body.isText()) {
                response.setEntity(new StringEntity(body.getBodyText(), body.getContentType()));
            } else {
                response.setEntity(new ByteArrayEntity(body.getBodyBytes(), body.getContentType()));
            }
        }
        scope.clientContext.setAttribute("http.response", response);
        return response;
    }

    ClassicHttpResponse callBackend(HttpHost target, ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        Date requestDate = this.getCurrentDate();
        LOG.debug("Calling the backend");
        ClassicHttpResponse backendResponse = chain.proceed(request, scope);
        try {
            backendResponse.addHeader("Via", this.generateViaHeader(backendResponse));
            return this.handleBackendResponse(target, request, scope, requestDate, this.getCurrentDate(), backendResponse);
        } catch (IOException | RuntimeException ex) {
            backendResponse.close();
            throw ex;
        }
    }

    private ClassicHttpResponse handleCacheHit(final HttpHost target, final ClassicHttpRequest request, ExecChain.Scope scope, final ExecChain chain, final HttpCacheEntry entry) throws IOException, HttpException {
        HttpClientContext context = scope.clientContext;
        context.setAttribute("http.request", request);
        this.recordCacheHit(target, request);
        Date now = this.getCurrentDate();
        if (this.suitabilityChecker.canCachedResponseBeUsed(target, request, entry, now)) {
            LOG.debug("Cache hit");
            try {
                return CachingExec.convert(this.generateCachedResponse(request, context, entry, now), scope);
            } catch (ResourceIOException ex) {
                this.recordCacheFailure(target, request);
                if (!this.mayCallBackend(request)) {
                    return CachingExec.convert(this.generateGatewayTimeout(context), scope);
                }
                this.setResponseStatus(scope.clientContext, CacheResponseStatus.FAILURE);
                return chain.proceed(request, scope);
            }
        }
        if (!this.mayCallBackend(request)) {
            LOG.debug("Cache entry not suitable but only-if-cached requested");
            return CachingExec.convert(this.generateGatewayTimeout(context), scope);
        }
        if (entry.getStatus() != 304 || this.suitabilityChecker.isConditional(request)) {
            LOG.debug("Revalidating cache entry");
            try {
                if (this.cacheRevalidator != null && !this.staleResponseNotAllowed(request, entry, now) && this.validityPolicy.mayReturnStaleWhileRevalidating(entry, now)) {
                    LOG.debug("Serving stale with asynchronous revalidation");
                    String exchangeId = ExecSupport.getNextExchangeId();
                    final ExecChain.Scope fork = new ExecChain.Scope(exchangeId, scope.route, scope.originalRequest, scope.execRuntime.fork(null), HttpClientContext.create());
                    SimpleHttpResponse response = this.generateCachedResponse(request, context, entry, now);
                    this.cacheRevalidator.revalidateCacheEntry(this.responseCache.generateKey(target, request, entry), new DefaultCacheRevalidator.RevalidationCall(){

                        @Override
                        public ClassicHttpResponse execute() throws HttpException, IOException {
                            return CachingExec.this.revalidateCacheEntry(target, request, fork, chain, entry);
                        }
                    });
                    return CachingExec.convert(response, scope);
                }
                return this.revalidateCacheEntry(target, request, scope, chain, entry);
            } catch (IOException ioex) {
                return CachingExec.convert(this.handleRevalidationFailure(request, context, entry, now), scope);
            }
        }
        LOG.debug("Cache entry not usable; calling backend");
        return this.callBackend(target, request, scope, chain);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    ClassicHttpResponse revalidateCacheEntry(HttpHost target, ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain, HttpCacheEntry cacheEntry) throws IOException, HttpException {
        ClassicHttpResponse classicHttpResponse;
        Date requestDate = this.getCurrentDate();
        ClassicHttpRequest conditionalRequest = this.conditionalRequestBuilder.buildConditionalRequest(scope.originalRequest, cacheEntry);
        ClassicHttpResponse backendResponse = chain.proceed(conditionalRequest, scope);
        Date responseDate = this.getCurrentDate();
        if (this.revalidationResponseIsTooOld(backendResponse, cacheEntry)) {
            backendResponse.close();
            ClassicHttpRequest unconditional = this.conditionalRequestBuilder.buildUnconditionalRequest(scope.originalRequest);
            requestDate = this.getCurrentDate();
            backendResponse = chain.proceed(unconditional, scope);
            responseDate = this.getCurrentDate();
        }
        backendResponse.addHeader("Via", this.generateViaHeader(backendResponse));
        int statusCode = backendResponse.getCode();
        if (statusCode == 304 || statusCode == 200) {
            this.recordCacheUpdate(scope.clientContext);
        }
        if (statusCode == 304) {
            HttpCacheEntry updatedEntry = this.responseCache.updateCacheEntry(target, request, cacheEntry, backendResponse, requestDate, responseDate);
            if (!this.suitabilityChecker.isConditional(request) || !this.suitabilityChecker.allConditionalsMatch(request, updatedEntry, new Date())) return CachingExec.convert(this.responseGenerator.generateResponse(request, updatedEntry), scope);
            return CachingExec.convert(this.responseGenerator.generateNotModifiedResponse(updatedEntry), scope);
        }
        if (!this.staleIfErrorAppliesTo(statusCode) || this.staleResponseNotAllowed(request, cacheEntry, this.getCurrentDate()) || !this.validityPolicy.mayReturnStaleIfError(request, cacheEntry, responseDate)) return this.handleBackendResponse(target, conditionalRequest, scope, requestDate, responseDate, backendResponse);
        try {
            SimpleHttpResponse cachedResponse = this.responseGenerator.generateResponse(request, cacheEntry);
            cachedResponse.addHeader("Warning", "110 localhost \"Response is stale\"");
            classicHttpResponse = CachingExec.convert(cachedResponse, scope);
        } catch (Throwable throwable) {
            try {
                backendResponse.close();
                throw throwable;
            } catch (IOException | RuntimeException ex) {
                backendResponse.close();
                throw ex;
            }
        }
        backendResponse.close();
        return classicHttpResponse;
    }

    ClassicHttpResponse handleBackendResponse(HttpHost target, ClassicHttpRequest request, ExecChain.Scope scope, Date requestDate, Date responseDate, ClassicHttpResponse backendResponse) throws IOException {
        this.responseCompliance.ensureProtocolCompliance(scope.originalRequest, request, backendResponse);
        this.responseCache.flushCacheEntriesInvalidatedByExchange(target, request, backendResponse);
        boolean cacheable = this.responseCachingPolicy.isResponseCacheable(request, (HttpResponse)backendResponse);
        if (cacheable) {
            this.storeRequestIfModifiedSinceFor304Response(request, backendResponse);
            return this.cacheAndReturnResponse(target, request, backendResponse, scope, requestDate, responseDate);
        }
        LOG.debug("Backend response is not cacheable");
        this.responseCache.flushCacheEntriesFor(target, request);
        return backendResponse;
    }

    ClassicHttpResponse cacheAndReturnResponse(HttpHost target, HttpRequest request, ClassicHttpResponse backendResponse, ExecChain.Scope scope, Date requestSent, Date responseReceived) throws IOException {
        HttpCacheEntry cacheEntry;
        ByteArrayBuffer buf;
        LOG.debug("Caching backend response");
        HttpEntity entity = backendResponse.getEntity();
        if (entity != null) {
            int l;
            buf = new ByteArrayBuffer(1024);
            InputStream inStream = entity.getContent();
            byte[] tmp = new byte[2048];
            long total = 0L;
            while ((l = inStream.read(tmp)) != -1) {
                buf.append(tmp, 0, l);
                if ((total += (long)l) <= this.cacheConfig.getMaxObjectSize()) continue;
                LOG.debug("Backend response content length exceeds maximum");
                backendResponse.setEntity(new CombinedEntity(entity, buf));
                return backendResponse;
            }
        } else {
            buf = null;
        }
        backendResponse.close();
        if (this.cacheConfig.isFreshnessCheckEnabled()) {
            HttpCacheEntry existingEntry = this.responseCache.getCacheEntry(target, request);
            if (DateUtils.isAfter(existingEntry, backendResponse, "Date")) {
                LOG.debug("Backend already contains fresher cache entry");
                cacheEntry = existingEntry;
            } else {
                cacheEntry = this.responseCache.createCacheEntry(target, request, backendResponse, buf, requestSent, responseReceived);
                LOG.debug("Backend response successfully cached");
            }
        } else {
            cacheEntry = this.responseCache.createCacheEntry(target, request, backendResponse, buf, requestSent, responseReceived);
            LOG.debug("Backend response successfully cached (freshness check skipped)");
        }
        return CachingExec.convert(this.responseGenerator.generateResponse(request, cacheEntry), scope);
    }

    private ClassicHttpResponse handleCacheMiss(HttpHost target, ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        this.recordCacheMiss(target, request);
        if (!this.mayCallBackend(request)) {
            return new BasicClassicHttpResponse(504, "Gateway Timeout");
        }
        Map<String, Variant> variants = this.responseCache.getVariantCacheEntriesWithEtags(target, request);
        if (variants != null && !variants.isEmpty()) {
            return this.negotiateResponseFromVariants(target, request, scope, chain, variants);
        }
        return this.callBackend(target, request, scope, chain);
    }

    ClassicHttpResponse negotiateResponseFromVariants(HttpHost target, ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain, Map<String, Variant> variants) throws IOException, HttpException {
        ClassicHttpRequest conditionalRequest = this.conditionalRequestBuilder.buildConditionalRequestFromVariants(request, variants);
        Date requestDate = this.getCurrentDate();
        ClassicHttpResponse backendResponse = chain.proceed(conditionalRequest, scope);
        try {
            Date responseDate = this.getCurrentDate();
            backendResponse.addHeader("Via", this.generateViaHeader(backendResponse));
            if (backendResponse.getCode() != 304) {
                return this.handleBackendResponse(target, request, scope, requestDate, responseDate, backendResponse);
            }
            Header resultEtagHeader = backendResponse.getFirstHeader("ETag");
            if (resultEtagHeader == null) {
                LOG.warn("304 response did not contain ETag");
                EntityUtils.consume(backendResponse.getEntity());
                backendResponse.close();
                return this.callBackend(target, request, scope, chain);
            }
            String resultEtag = resultEtagHeader.getValue();
            Variant matchingVariant = variants.get(resultEtag);
            if (matchingVariant == null) {
                LOG.debug("304 response did not contain ETag matching one sent in If-None-Match");
                EntityUtils.consume(backendResponse.getEntity());
                backendResponse.close();
                return this.callBackend(target, request, scope, chain);
            }
            if (this.revalidationResponseIsTooOld(backendResponse, matchingVariant.getEntry()) && (request.getEntity() == null || request.getEntity().isRepeatable())) {
                EntityUtils.consume(backendResponse.getEntity());
                backendResponse.close();
                ClassicHttpRequest unconditional = this.conditionalRequestBuilder.buildUnconditionalRequest(request);
                return this.callBackend(target, unconditional, scope, chain);
            }
            this.recordCacheUpdate(scope.clientContext);
            HttpCacheEntry responseEntry = this.responseCache.updateVariantCacheEntry(target, conditionalRequest, backendResponse, matchingVariant, requestDate, responseDate);
            backendResponse.close();
            if (this.shouldSendNotModifiedResponse(request, responseEntry)) {
                return CachingExec.convert(this.responseGenerator.generateNotModifiedResponse(responseEntry), scope);
            }
            SimpleHttpResponse response = this.responseGenerator.generateResponse(request, responseEntry);
            this.responseCache.reuseVariantEntryFor(target, request, matchingVariant);
            return CachingExec.convert(response, scope);
        } catch (IOException | RuntimeException ex) {
            backendResponse.close();
            throw ex;
        }
    }
}

