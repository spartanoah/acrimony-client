/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Date;
import java.util.Iterator;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.CacheValidityPolicy;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CachedResponseSuitabilityChecker {
    private static final Logger LOG = LoggerFactory.getLogger(CachedResponseSuitabilityChecker.class);
    private final boolean sharedCache;
    private final boolean useHeuristicCaching;
    private final float heuristicCoefficient;
    private final TimeValue heuristicDefaultLifetime;
    private final CacheValidityPolicy validityStrategy;

    CachedResponseSuitabilityChecker(CacheValidityPolicy validityStrategy, CacheConfig config) {
        this.validityStrategy = validityStrategy;
        this.sharedCache = config.isSharedCache();
        this.useHeuristicCaching = config.isHeuristicCachingEnabled();
        this.heuristicCoefficient = config.getHeuristicCoefficient();
        this.heuristicDefaultLifetime = config.getHeuristicDefaultLifetime();
    }

    CachedResponseSuitabilityChecker(CacheConfig config) {
        this(new CacheValidityPolicy(), config);
    }

    private boolean isFreshEnough(HttpCacheEntry entry, HttpRequest request, Date now) {
        if (this.validityStrategy.isResponseFresh(entry, now)) {
            return true;
        }
        if (this.useHeuristicCaching && this.validityStrategy.isResponseHeuristicallyFresh(entry, now, this.heuristicCoefficient, this.heuristicDefaultLifetime)) {
            return true;
        }
        if (this.originInsistsOnFreshness(entry)) {
            return false;
        }
        long maxStale = this.getMaxStale(request);
        if (maxStale == -1L) {
            return false;
        }
        return maxStale > this.validityStrategy.getStaleness(entry, now).toSeconds();
    }

    private boolean originInsistsOnFreshness(HttpCacheEntry entry) {
        if (this.validityStrategy.mustRevalidate(entry)) {
            return true;
        }
        if (!this.sharedCache) {
            return false;
        }
        return this.validityStrategy.proxyRevalidate(entry) || this.validityStrategy.hasCacheControlDirective(entry, "s-maxage");
    }

    private long getMaxStale(HttpRequest request) {
        long maxStale = -1L;
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"max-stale".equals(elt.getName())) continue;
            if ((elt.getValue() == null || elt.getValue().trim().isEmpty()) && maxStale == -1L) {
                maxStale = Long.MAX_VALUE;
                continue;
            }
            try {
                long val2 = Long.parseLong(elt.getValue());
                if (val2 < 0L) {
                    val2 = 0L;
                }
                if (maxStale != -1L && val2 >= maxStale) continue;
                maxStale = val2;
            } catch (NumberFormatException nfe) {
                maxStale = 0L;
            }
        }
        return maxStale;
    }

    public boolean canCachedResponseBeUsed(HttpHost host, HttpRequest request, HttpCacheEntry entry, Date now) {
        if (!this.isFreshEnough(entry, request, now)) {
            LOG.debug("Cache entry is not fresh enough");
            return false;
        }
        if (this.isGet(request) && !this.validityStrategy.contentLengthHeaderMatchesActualLength(entry)) {
            LOG.debug("Cache entry Content-Length and header information do not match");
            return false;
        }
        if (this.hasUnsupportedConditionalHeaders(request)) {
            LOG.debug("Request contains unsupported conditional headers");
            return false;
        }
        if (!this.isConditional(request) && entry.getStatus() == 304) {
            LOG.debug("Unconditional request and non-modified cached response");
            return false;
        }
        if (this.isConditional(request) && !this.allConditionalsMatch(request, entry, now)) {
            LOG.debug("Conditional request and with mismatched conditions");
            return false;
        }
        if (this.hasUnsupportedCacheEntryForGet(request, entry)) {
            LOG.debug("HEAD response caching enabled but the cache entry does not contain a request method, entity or a 204 response");
            return false;
        }
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if ("no-cache".equals(elt.getName())) {
                LOG.debug("Response contained NO CACHE directive, cache was not suitable");
                return false;
            }
            if ("no-store".equals(elt.getName())) {
                LOG.debug("Response contained NO STORE directive, cache was not suitable");
                return false;
            }
            if ("max-age".equals(elt.getName())) {
                try {
                    int maxAge = Integer.parseInt(elt.getValue());
                    if (this.validityStrategy.getCurrentAge(entry, now).toSeconds() > (long)maxAge) {
                        LOG.debug("Response from cache was not suitable due to max age");
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    LOG.debug("Response from cache was malformed: {}", (Object)ex.getMessage());
                    return false;
                }
            }
            if ("max-stale".equals(elt.getName())) {
                try {
                    int maxStale = Integer.parseInt(elt.getValue());
                    if (this.validityStrategy.getFreshnessLifetime(entry).toSeconds() > (long)maxStale) {
                        LOG.debug("Response from cache was not suitable due to max stale freshness");
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    LOG.debug("Response from cache was malformed: {}", (Object)ex.getMessage());
                    return false;
                }
            }
            if (!"min-fresh".equals(elt.getName())) continue;
            try {
                long minFresh = Long.parseLong(elt.getValue());
                if (minFresh < 0L) {
                    return false;
                }
                TimeValue age = this.validityStrategy.getCurrentAge(entry, now);
                TimeValue freshness = this.validityStrategy.getFreshnessLifetime(entry);
                if (freshness.toSeconds() - age.toSeconds() >= minFresh) continue;
                LOG.debug("Response from cache was not suitable due to min fresh freshness requirement");
                return false;
            } catch (NumberFormatException ex) {
                LOG.debug("Response from cache was malformed: {}", (Object)ex.getMessage());
                return false;
            }
        }
        LOG.debug("Response from cache was suitable");
        return true;
    }

    private boolean isGet(HttpRequest request) {
        return request.getMethod().equals("GET");
    }

    private boolean entryIsNotA204Response(HttpCacheEntry entry) {
        return entry.getStatus() != 204;
    }

    private boolean cacheEntryDoesNotContainMethodAndEntity(HttpCacheEntry entry) {
        return entry.getRequestMethod() == null && entry.getResource() == null;
    }

    private boolean hasUnsupportedCacheEntryForGet(HttpRequest request, HttpCacheEntry entry) {
        return this.isGet(request) && this.cacheEntryDoesNotContainMethodAndEntity(entry) && this.entryIsNotA204Response(entry);
    }

    public boolean isConditional(HttpRequest request) {
        return this.hasSupportedEtagValidator(request) || this.hasSupportedLastModifiedValidator(request);
    }

    public boolean allConditionalsMatch(HttpRequest request, HttpCacheEntry entry, Date now) {
        boolean lastModifiedValidatorMatches;
        boolean hasEtagValidator = this.hasSupportedEtagValidator(request);
        boolean hasLastModifiedValidator = this.hasSupportedLastModifiedValidator(request);
        boolean etagValidatorMatches = hasEtagValidator && this.etagValidatorMatches(request, entry);
        boolean bl = lastModifiedValidatorMatches = hasLastModifiedValidator && this.lastModifiedValidatorMatches(request, entry, now);
        if (hasEtagValidator && hasLastModifiedValidator && (!etagValidatorMatches || !lastModifiedValidatorMatches)) {
            return false;
        }
        if (hasEtagValidator && !etagValidatorMatches) {
            return false;
        }
        return !hasLastModifiedValidator || lastModifiedValidatorMatches;
    }

    private boolean hasUnsupportedConditionalHeaders(HttpRequest request) {
        return request.getFirstHeader("If-Range") != null || request.getFirstHeader("If-Match") != null || this.hasValidDateField(request, "If-Unmodified-Since");
    }

    private boolean hasSupportedEtagValidator(HttpRequest request) {
        return request.containsHeader("If-None-Match");
    }

    private boolean hasSupportedLastModifiedValidator(HttpRequest request) {
        return this.hasValidDateField(request, "If-Modified-Since");
    }

    private boolean etagValidatorMatches(HttpRequest request, HttpCacheEntry entry) {
        Header etagHeader = entry.getFirstHeader("ETag");
        String etag = etagHeader != null ? etagHeader.getValue() : null;
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "If-None-Match");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            String reqEtag = elt.toString();
            if ((!"*".equals(reqEtag) || etag == null) && !reqEtag.equals(etag)) continue;
            return true;
        }
        return false;
    }

    private boolean lastModifiedValidatorMatches(HttpRequest request, HttpCacheEntry entry, Date now) {
        Date lastModified = DateUtils.parseDate(entry, "Last-Modified");
        if (lastModified == null) {
            return false;
        }
        for (Header h : request.getHeaders("If-Modified-Since")) {
            Date ifModifiedSince = DateUtils.parseDate(h.getValue());
            if (ifModifiedSince == null || !ifModifiedSince.after(now) && !lastModified.after(ifModifiedSince)) continue;
            return false;
        }
        return true;
    }

    private boolean hasValidDateField(HttpRequest request, String headerName) {
        int i$ = 0;
        Header[] arr$ = request.getHeaders(headerName);
        int len$ = arr$.length;
        if (i$ < len$) {
            Header h = arr$[i$];
            Date date = DateUtils.parseDate(h.getValue());
            return date != null;
        }
        return false;
    }
}

