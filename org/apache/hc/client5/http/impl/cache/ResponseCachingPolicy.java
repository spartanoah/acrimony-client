/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.MessageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResponseCachingPolicy {
    private static final String[] AUTH_CACHEABLE_PARAMS = new String[]{"s-maxage", "must-revalidate", "public"};
    private static final Set<Integer> CACHEABLE_STATUS_CODES = new HashSet<Integer>(Arrays.asList(200, 203, 300, 301, 410));
    private static final Logger LOG = LoggerFactory.getLogger(ResponseCachingPolicy.class);
    private final long maxObjectSizeBytes;
    private final boolean sharedCache;
    private final boolean neverCache1_0ResponsesWithQueryString;
    private final Set<Integer> uncacheableStatusCodes;

    public ResponseCachingPolicy(long maxObjectSizeBytes, boolean sharedCache, boolean neverCache1_0ResponsesWithQueryString, boolean allow303Caching) {
        this.maxObjectSizeBytes = maxObjectSizeBytes;
        this.sharedCache = sharedCache;
        this.neverCache1_0ResponsesWithQueryString = neverCache1_0ResponsesWithQueryString;
        this.uncacheableStatusCodes = allow303Caching ? new HashSet<Integer>(Arrays.asList(206)) : new HashSet<Integer>(Arrays.asList(206, 303));
    }

    public boolean isResponseCacheable(String httpMethod, HttpResponse response) {
        long contentLengthValue;
        boolean cacheable = false;
        if (!"GET".equals(httpMethod) && !"HEAD".equals(httpMethod)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} method response is not cacheable", (Object)httpMethod);
            }
            return false;
        }
        int status = response.getCode();
        if (CACHEABLE_STATUS_CODES.contains(status)) {
            cacheable = true;
        } else {
            if (this.uncacheableStatusCodes.contains(status)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} response is not cacheable", (Object)status);
                }
                return false;
            }
            if (this.unknownStatusCode(status)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} response is unknown", (Object)status);
                }
                return false;
            }
        }
        Header contentLength = response.getFirstHeader("Content-Length");
        if (contentLength != null && (contentLengthValue = Long.parseLong(contentLength.getValue())) > this.maxObjectSizeBytes) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Response content length exceeds {}", (Object)this.maxObjectSizeBytes);
            }
            return false;
        }
        if (response.countHeaders("Age") > 1) {
            LOG.debug("Multiple Age headers");
            return false;
        }
        if (response.countHeaders("Expires") > 1) {
            LOG.debug("Multiple Expires headers");
            return false;
        }
        if (response.countHeaders("Date") > 1) {
            LOG.debug("Multiple Date headers");
            return false;
        }
        Date date = DateUtils.parseDate(response, "Date");
        if (date == null) {
            LOG.debug("Invalid / missing Date header");
            return false;
        }
        Iterator<HeaderElement> it = MessageSupport.iterate(response, "Vary");
        while (it.hasNext()) {
            HeaderElement elem = it.next();
            if (!"*".equals(elem.getName())) continue;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Vary * found");
            }
            return false;
        }
        if (this.isExplicitlyNonCacheable(response)) {
            LOG.debug("Response is explicitly non-cacheable");
            return false;
        }
        return cacheable || this.isExplicitlyCacheable(response);
    }

    private boolean unknownStatusCode(int status) {
        if (status >= 100 && status <= 101) {
            return false;
        }
        if (status >= 200 && status <= 206) {
            return false;
        }
        if (status >= 300 && status <= 307) {
            return false;
        }
        if (status >= 400 && status <= 417) {
            return false;
        }
        return status < 500 || status > 505;
    }

    protected boolean isExplicitlyNonCacheable(HttpResponse response) {
        Iterator<HeaderElement> it = MessageSupport.iterate(response, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elem = it.next();
            if (!"no-store".equals(elem.getName()) && !"no-cache".equals(elem.getName()) && (!this.sharedCache || !"private".equals(elem.getName()))) continue;
            return true;
        }
        return false;
    }

    protected boolean hasCacheControlParameterFrom(HttpMessage msg, String[] params) {
        Iterator<HeaderElement> it = MessageSupport.iterate(msg, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elem = it.next();
            for (String param : params) {
                if (!param.equalsIgnoreCase(elem.getName())) continue;
                return true;
            }
        }
        return false;
    }

    protected boolean isExplicitlyCacheable(HttpResponse response) {
        if (response.getFirstHeader("Expires") != null) {
            return true;
        }
        String[] cacheableParams = new String[]{"max-age", "s-maxage", "must-revalidate", "proxy-revalidate", "public"};
        return this.hasCacheControlParameterFrom(response, cacheableParams);
    }

    public boolean isResponseCacheable(HttpRequest request, HttpResponse response) {
        ProtocolVersion version;
        ProtocolVersion protocolVersion = version = request.getVersion() != null ? request.getVersion() : HttpVersion.DEFAULT;
        if (version.compareToVersion(HttpVersion.HTTP_1_1) > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Protocol version {} is non-cacheable", (Object)version);
            }
            return false;
        }
        String[] uncacheableRequestDirectives = new String[]{"no-store"};
        if (this.hasCacheControlParameterFrom(request, uncacheableRequestDirectives)) {
            LOG.debug("Response is explcitily non-cacheable per cache control directive");
            return false;
        }
        if (request.getRequestUri().contains("?")) {
            if (this.neverCache1_0ResponsesWithQueryString && this.from1_0Origin(response)) {
                LOG.debug("Response is not cacheable as it had a query string");
                return false;
            }
            if (!this.isExplicitlyCacheable(response)) {
                LOG.debug("Response is not cacheable as it is missing explicit caching headers");
                return false;
            }
        }
        if (this.expiresHeaderLessOrEqualToDateHeaderAndNoCacheControl(response)) {
            LOG.debug("Expires header less or equal to Date header and no cache control directives");
            return false;
        }
        if (this.sharedCache && request.countHeaders("Authorization") > 0 && !this.hasCacheControlParameterFrom(response, AUTH_CACHEABLE_PARAMS)) {
            LOG.debug("Request contains private credentials");
            return false;
        }
        String method = request.getMethod();
        return this.isResponseCacheable(method, response);
    }

    private boolean expiresHeaderLessOrEqualToDateHeaderAndNoCacheControl(HttpResponse response) {
        if (response.getFirstHeader("Cache-Control") != null) {
            return false;
        }
        Header expiresHdr = response.getFirstHeader("Expires");
        Header dateHdr = response.getFirstHeader("Date");
        if (expiresHdr == null || dateHdr == null) {
            return false;
        }
        Date expires = DateUtils.parseDate(expiresHdr.getValue());
        Date date = DateUtils.parseDate(dateHdr.getValue());
        if (expires == null || date == null) {
            return false;
        }
        return expires.equals(date) || expires.before(date);
    }

    private boolean from1_0Origin(HttpResponse response) {
        Iterator<HeaderElement> it = MessageSupport.iterate(response, "Via");
        if (it.hasNext()) {
            HeaderElement elt = it.next();
            String proto = elt.toString().split("\\s")[0];
            if (proto.contains("/")) {
                return proto.equals("HTTP/1.0");
            }
            return proto.equals("1.0");
        }
        ProtocolVersion version = response.getVersion() != null ? response.getVersion() : HttpVersion.DEFAULT;
        return HttpVersion.HTTP_1_0.equals(version);
    }
}

