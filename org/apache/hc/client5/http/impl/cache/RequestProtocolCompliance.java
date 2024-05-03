/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.hc.client5.http.impl.cache.RequestProtocolError;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.MessageSupport;

class RequestProtocolCompliance {
    private final boolean weakETagOnPutDeleteAllowed;
    private static final List<String> disallowedWithNoCache = Arrays.asList("min-fresh", "max-stale", "max-age");

    public RequestProtocolCompliance() {
        this.weakETagOnPutDeleteAllowed = false;
    }

    public RequestProtocolCompliance(boolean weakETagOnPutDeleteAllowed) {
        this.weakETagOnPutDeleteAllowed = weakETagOnPutDeleteAllowed;
    }

    public List<RequestProtocolError> requestIsFatallyNonCompliant(HttpRequest request) {
        ArrayList<RequestProtocolError> theErrors = new ArrayList<RequestProtocolError>();
        RequestProtocolError anError = this.requestHasWeakETagAndRange(request);
        if (anError != null) {
            theErrors.add(anError);
        }
        if (!this.weakETagOnPutDeleteAllowed && (anError = this.requestHasWeekETagForPUTOrDELETEIfMatch(request)) != null) {
            theErrors.add(anError);
        }
        if ((anError = this.requestContainsNoCacheDirectiveWithFieldName(request)) != null) {
            theErrors.add(anError);
        }
        return theErrors;
    }

    public void makeRequestCompliant(HttpRequest request) {
        this.decrementOPTIONSMaxForwardsIfGreaterThen0(request);
        this.stripOtherFreshnessDirectivesWithNoCache(request);
        if (this.requestVersionIsTooLow(request) || this.requestMinorVersionIsTooHighMajorVersionsMatch(request)) {
            request.setVersion(HttpVersion.HTTP_1_1);
        }
    }

    private void stripOtherFreshnessDirectivesWithNoCache(HttpRequest request) {
        ArrayList<HeaderElement> outElts = new ArrayList<HeaderElement>();
        boolean shouldStrip = false;
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!disallowedWithNoCache.contains(elt.getName())) {
                outElts.add(elt);
            }
            if (!"no-cache".equals(elt.getName())) continue;
            shouldStrip = true;
        }
        if (!shouldStrip) {
            return;
        }
        request.removeHeaders("Cache-Control");
        request.setHeader("Cache-Control", this.buildHeaderFromElements(outElts));
    }

    private String buildHeaderFromElements(List<HeaderElement> outElts) {
        StringBuilder newHdr = new StringBuilder("");
        boolean first = true;
        for (HeaderElement elt : outElts) {
            if (!first) {
                newHdr.append(",");
            } else {
                first = false;
            }
            newHdr.append(elt);
        }
        return newHdr.toString();
    }

    private void decrementOPTIONSMaxForwardsIfGreaterThen0(HttpRequest request) {
        if (!"OPTIONS".equals(request.getMethod())) {
            return;
        }
        Header maxForwards = request.getFirstHeader("Max-Forwards");
        if (maxForwards == null) {
            return;
        }
        request.removeHeaders("Max-Forwards");
        int currentMaxForwards = Integer.parseInt(maxForwards.getValue());
        request.setHeader("Max-Forwards", Integer.toString(currentMaxForwards - 1));
    }

    protected boolean requestMinorVersionIsTooHighMajorVersionsMatch(HttpRequest request) {
        ProtocolVersion requestProtocol = request.getVersion();
        if (requestProtocol == null) {
            return false;
        }
        if (requestProtocol.getMajor() != HttpVersion.HTTP_1_1.getMajor()) {
            return false;
        }
        return requestProtocol.getMinor() > HttpVersion.HTTP_1_1.getMinor();
    }

    protected boolean requestVersionIsTooLow(HttpRequest request) {
        ProtocolVersion requestProtocol = request.getVersion();
        return requestProtocol != null && requestProtocol.compareToVersion(HttpVersion.HTTP_1_1) < 0;
    }

    private RequestProtocolError requestHasWeakETagAndRange(HttpRequest request) {
        String method = request.getMethod();
        if (!"GET".equals(method)) {
            return null;
        }
        Header range = request.getFirstHeader("Range");
        if (range == null) {
            return null;
        }
        Header ifRange = request.getFirstHeader("If-Range");
        if (ifRange == null) {
            return null;
        }
        String val2 = ifRange.getValue();
        if (val2.startsWith("W/")) {
            return RequestProtocolError.WEAK_ETAG_AND_RANGE_ERROR;
        }
        return null;
    }

    private RequestProtocolError requestHasWeekETagForPUTOrDELETEIfMatch(HttpRequest request) {
        String method = request.getMethod();
        if (!"PUT".equals(method) && !"DELETE".equals(method)) {
            return null;
        }
        Header ifMatch = request.getFirstHeader("If-Match");
        if (ifMatch != null) {
            String val2 = ifMatch.getValue();
            if (val2.startsWith("W/")) {
                return RequestProtocolError.WEAK_ETAG_ON_PUTDELETE_METHOD_ERROR;
            }
        } else {
            Header ifNoneMatch = request.getFirstHeader("If-None-Match");
            if (ifNoneMatch == null) {
                return null;
            }
            String val2 = ifNoneMatch.getValue();
            if (val2.startsWith("W/")) {
                return RequestProtocolError.WEAK_ETAG_ON_PUTDELETE_METHOD_ERROR;
            }
        }
        return null;
    }

    private RequestProtocolError requestContainsNoCacheDirectiveWithFieldName(HttpRequest request) {
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            if (!"no-cache".equalsIgnoreCase(elt.getName()) || elt.getValue() == null) continue;
            return RequestProtocolError.NO_CACHE_DIRECTIVE_WITH_FIELD_NAME;
        }
        return null;
    }
}

