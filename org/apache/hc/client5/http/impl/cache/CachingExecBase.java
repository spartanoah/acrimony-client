/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheValidityPolicy;
import org.apache.hc.client5.http.impl.cache.CacheableRequestPolicy;
import org.apache.hc.client5.http.impl.cache.CachedHttpResponseGenerator;
import org.apache.hc.client5.http.impl.cache.CachedResponseSuitabilityChecker;
import org.apache.hc.client5.http.impl.cache.RequestProtocolCompliance;
import org.apache.hc.client5.http.impl.cache.RequestProtocolError;
import org.apache.hc.client5.http.impl.cache.ResponseCachingPolicy;
import org.apache.hc.client5.http.impl.cache.ResponseProtocolCompliance;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingExecBase {
    static final boolean SUPPORTS_RANGE_AND_CONTENT_RANGE_HEADERS = false;
    final AtomicLong cacheHits = new AtomicLong();
    final AtomicLong cacheMisses = new AtomicLong();
    final AtomicLong cacheUpdates = new AtomicLong();
    final Map<ProtocolVersion, String> viaHeaders = new ConcurrentHashMap<ProtocolVersion, String>(4);
    final ResponseCachingPolicy responseCachingPolicy;
    final CacheValidityPolicy validityPolicy;
    final CachedHttpResponseGenerator responseGenerator;
    final CacheableRequestPolicy cacheableRequestPolicy;
    final CachedResponseSuitabilityChecker suitabilityChecker;
    final ResponseProtocolCompliance responseCompliance;
    final RequestProtocolCompliance requestCompliance;
    final CacheConfig cacheConfig;
    private static final Logger LOG = LoggerFactory.getLogger(CachingExecBase.class);

    CachingExecBase(CacheValidityPolicy validityPolicy, ResponseCachingPolicy responseCachingPolicy, CachedHttpResponseGenerator responseGenerator, CacheableRequestPolicy cacheableRequestPolicy, CachedResponseSuitabilityChecker suitabilityChecker, ResponseProtocolCompliance responseCompliance, RequestProtocolCompliance requestCompliance, CacheConfig config) {
        this.responseCachingPolicy = responseCachingPolicy;
        this.validityPolicy = validityPolicy;
        this.responseGenerator = responseGenerator;
        this.cacheableRequestPolicy = cacheableRequestPolicy;
        this.suitabilityChecker = suitabilityChecker;
        this.requestCompliance = requestCompliance;
        this.responseCompliance = responseCompliance;
        this.cacheConfig = config != null ? config : CacheConfig.DEFAULT;
    }

    CachingExecBase(CacheConfig config) {
        this.cacheConfig = config != null ? config : CacheConfig.DEFAULT;
        this.validityPolicy = new CacheValidityPolicy();
        this.responseGenerator = new CachedHttpResponseGenerator(this.validityPolicy);
        this.cacheableRequestPolicy = new CacheableRequestPolicy();
        this.suitabilityChecker = new CachedResponseSuitabilityChecker(this.validityPolicy, this.cacheConfig);
        this.responseCompliance = new ResponseProtocolCompliance();
        this.requestCompliance = new RequestProtocolCompliance(this.cacheConfig.isWeakETagOnPutDeleteAllowed());
        this.responseCachingPolicy = new ResponseCachingPolicy(this.cacheConfig.getMaxObjectSize(), this.cacheConfig.isSharedCache(), this.cacheConfig.isNeverCacheHTTP10ResponsesWithQuery(), this.cacheConfig.is303CachingEnabled());
    }

    public long getCacheHits() {
        return this.cacheHits.get();
    }

    public long getCacheMisses() {
        return this.cacheMisses.get();
    }

    public long getCacheUpdates() {
        return this.cacheUpdates.get();
    }

    SimpleHttpResponse getFatallyNoncompliantResponse(HttpRequest request, HttpContext context) {
        List<RequestProtocolError> fatalError = this.requestCompliance.requestIsFatallyNonCompliant(request);
        if (fatalError != null && !fatalError.isEmpty()) {
            this.setResponseStatus(context, CacheResponseStatus.CACHE_MODULE_RESPONSE);
            return this.responseGenerator.getErrorForRequest(fatalError.get(0));
        }
        return null;
    }

    void recordCacheMiss(HttpHost target, HttpRequest request) {
        this.cacheMisses.getAndIncrement();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache miss [host: {}; uri: {}]", (Object)target, (Object)request.getRequestUri());
        }
    }

    void recordCacheHit(HttpHost target, HttpRequest request) {
        this.cacheHits.getAndIncrement();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache hit [host: {}; uri: {}]", (Object)target, (Object)request.getRequestUri());
        }
    }

    void recordCacheFailure(HttpHost target, HttpRequest request) {
        this.cacheMisses.getAndIncrement();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache failure [host: {}; uri: {}]", (Object)target, (Object)request.getRequestUri());
        }
    }

    void recordCacheUpdate(HttpContext context) {
        this.cacheUpdates.getAndIncrement();
        this.setResponseStatus(context, CacheResponseStatus.VALIDATED);
    }

    SimpleHttpResponse generateCachedResponse(HttpRequest request, HttpContext context, HttpCacheEntry entry, Date now) throws ResourceIOException {
        SimpleHttpResponse cachedResponse = request.containsHeader("If-None-Match") || request.containsHeader("If-Modified-Since") ? this.responseGenerator.generateNotModifiedResponse(entry) : this.responseGenerator.generateResponse(request, entry);
        this.setResponseStatus(context, CacheResponseStatus.CACHE_HIT);
        if (TimeValue.isPositive(this.validityPolicy.getStaleness(entry, now))) {
            cachedResponse.addHeader("Warning", "110 localhost \"Response is stale\"");
        }
        return cachedResponse;
    }

    SimpleHttpResponse handleRevalidationFailure(HttpRequest request, HttpContext context, HttpCacheEntry entry, Date now) throws IOException {
        if (this.staleResponseNotAllowed(request, entry, now)) {
            return this.generateGatewayTimeout(context);
        }
        return this.unvalidatedCacheHit(request, context, entry);
    }

    SimpleHttpResponse generateGatewayTimeout(HttpContext context) {
        this.setResponseStatus(context, CacheResponseStatus.CACHE_MODULE_RESPONSE);
        return SimpleHttpResponse.create(504, "Gateway Timeout");
    }

    SimpleHttpResponse unvalidatedCacheHit(HttpRequest request, HttpContext context, HttpCacheEntry entry) throws IOException {
        SimpleHttpResponse cachedResponse = this.responseGenerator.generateResponse(request, entry);
        this.setResponseStatus(context, CacheResponseStatus.CACHE_HIT);
        cachedResponse.addHeader("Warning", "111 localhost \"Revalidation failed\"");
        return cachedResponse;
    }

    boolean staleResponseNotAllowed(HttpRequest request, HttpCacheEntry entry, Date now) {
        return this.validityPolicy.mustRevalidate(entry) || this.cacheConfig.isSharedCache() && this.validityPolicy.proxyRevalidate(entry) || this.explicitFreshnessRequest(request, entry, now);
    }

    boolean mayCallBackend(HttpRequest request) {
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"only-if-cached".equals(elt.getName())) continue;
            LOG.debug("Request marked only-if-cached");
            return false;
        }
        return true;
    }

    boolean explicitFreshnessRequest(HttpRequest request, HttpCacheEntry entry, Date now) {
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if ("max-stale".equals(elt.getName())) {
                try {
                    int maxStale = Integer.parseInt(elt.getValue());
                    TimeValue age = this.validityPolicy.getCurrentAge(entry, now);
                    TimeValue lifetime = this.validityPolicy.getFreshnessLifetime(entry);
                    if (age.toSeconds() - lifetime.toSeconds() <= (long)maxStale) continue;
                    return true;
                } catch (NumberFormatException nfe) {
                    return true;
                }
            }
            if (!"min-fresh".equals(elt.getName()) && !"max-age".equals(elt.getName())) continue;
            return true;
        }
        return false;
    }

    String generateViaHeader(HttpMessage msg) {
        if (msg.getVersion() == null) {
            msg.setVersion(HttpVersion.DEFAULT);
        }
        ProtocolVersion pv = msg.getVersion();
        String existingEntry = this.viaHeaders.get(msg.getVersion());
        if (existingEntry != null) {
            return existingEntry;
        }
        VersionInfo vi = VersionInfo.loadVersionInfo("org.apache.hc.client5", this.getClass().getClassLoader());
        String release = vi != null ? vi.getRelease() : "UNAVAILABLE";
        int major = pv.getMajor();
        int minor = pv.getMinor();
        String value = URIScheme.HTTP.same(pv.getProtocol()) ? String.format("%d.%d localhost (Apache-HttpClient/%s (cache))", major, minor, release) : String.format("%s/%d.%d localhost (Apache-HttpClient/%s (cache))", pv.getProtocol(), major, minor, release);
        this.viaHeaders.put(pv, value);
        return value;
    }

    void setResponseStatus(HttpContext context, CacheResponseStatus value) {
        if (context != null) {
            context.setAttribute("http.cache.response.status", (Object)value);
        }
    }

    boolean supportsRangeAndContentRangeHeaders() {
        return false;
    }

    Date getCurrentDate() {
        return new Date();
    }

    boolean clientRequestsOurOptions(HttpRequest request) {
        if (!"OPTIONS".equals(request.getMethod())) {
            return false;
        }
        if (!"*".equals(request.getRequestUri())) {
            return false;
        }
        Header h = request.getFirstHeader("Max-Forwards");
        return "0".equals(h != null ? h.getValue() : null);
    }

    boolean revalidationResponseIsTooOld(HttpResponse backendResponse, HttpCacheEntry cacheEntry) {
        return DateUtils.isBefore(backendResponse, cacheEntry, "Date");
    }

    boolean shouldSendNotModifiedResponse(HttpRequest request, HttpCacheEntry responseEntry) {
        return this.suitabilityChecker.isConditional(request) && this.suitabilityChecker.allConditionalsMatch(request, responseEntry, new Date());
    }

    boolean staleIfErrorAppliesTo(int statusCode) {
        return statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    void storeRequestIfModifiedSinceFor304Response(HttpRequest request, HttpResponse backendResponse) {
        Header h;
        if (backendResponse.getCode() == 304 && (h = request.getFirstHeader("If-Modified-Since")) != null) {
            backendResponse.addHeader("Last-Modified", h.getValue());
        }
    }
}

