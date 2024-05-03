/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.impl.cache.WarningValue;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.MessageSupport;

class ResponseProtocolCompliance {
    private static final String UNEXPECTED_100_CONTINUE = "The incoming request did not contain a 100-continue header, but the response was a Status 100, continue.";
    private static final String UNEXPECTED_PARTIAL_CONTENT = "partial content was returned for a request that did not ask for it";

    ResponseProtocolCompliance() {
    }

    public void ensureProtocolCompliance(HttpRequest originalRequest, HttpRequest request, HttpResponse response) throws IOException {
        this.requestDidNotExpect100ContinueButResponseIsOne(originalRequest, response);
        this.transferEncodingIsNotReturnedTo1_0Client(originalRequest, response);
        this.ensurePartialContentIsNotSentToAClientThatDidNotRequestIt(request, response);
        this.ensure200ForOPTIONSRequestWithNoBodyHasContentLengthZero(request, response);
        this.ensure206ContainsDateHeader(response);
        this.ensure304DoesNotContainExtraEntityHeaders(response);
        this.identityIsNotUsedInContentEncoding(response);
        this.warningsWithNonMatchingWarnDatesAreRemoved(response);
    }

    private void warningsWithNonMatchingWarnDatesAreRemoved(HttpResponse response) {
        Date responseDate = DateUtils.parseDate(response, "Date");
        if (responseDate == null) {
            return;
        }
        Header[] warningHeaders = response.getHeaders("Warning");
        if (warningHeaders == null || warningHeaders.length == 0) {
            return;
        }
        ArrayList<BasicHeader> newWarningHeaders = new ArrayList<BasicHeader>();
        boolean modified = false;
        for (Header h : warningHeaders) {
            for (WarningValue wv : WarningValue.getWarningValues(h)) {
                Date warnDate = wv.getWarnDate();
                if (warnDate == null || warnDate.equals(responseDate)) {
                    newWarningHeaders.add(new BasicHeader("Warning", wv.toString()));
                    continue;
                }
                modified = true;
            }
        }
        if (modified) {
            response.removeHeaders("Warning");
            for (Header header : newWarningHeaders) {
                response.addHeader(header);
            }
        }
    }

    private void identityIsNotUsedInContentEncoding(HttpResponse response) {
        Header[] hdrs = response.getHeaders("Content-Encoding");
        if (hdrs == null || hdrs.length == 0) {
            return;
        }
        ArrayList<BasicHeader> newHeaders = new ArrayList<BasicHeader>();
        boolean modified = false;
        for (Header h : hdrs) {
            StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (HeaderElement elt : MessageSupport.parse(h)) {
                if ("identity".equalsIgnoreCase(elt.getName())) {
                    modified = true;
                    continue;
                }
                if (!first) {
                    buf.append(",");
                }
                buf.append(elt);
                first = false;
            }
            String newHeaderValue = buf.toString();
            if (newHeaderValue.isEmpty()) continue;
            newHeaders.add(new BasicHeader("Content-Encoding", newHeaderValue));
        }
        if (!modified) {
            return;
        }
        response.removeHeaders("Content-Encoding");
        for (Header header : newHeaders) {
            response.addHeader(header);
        }
    }

    private void ensure206ContainsDateHeader(HttpResponse response) {
        if (response.getFirstHeader("Date") == null) {
            response.addHeader("Date", DateUtils.formatDate(new Date()));
        }
    }

    private void ensurePartialContentIsNotSentToAClientThatDidNotRequestIt(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getFirstHeader("Range") != null || response.getCode() != 206) {
            return;
        }
        throw new ClientProtocolException(UNEXPECTED_PARTIAL_CONTENT);
    }

    private void ensure200ForOPTIONSRequestWithNoBodyHasContentLengthZero(HttpRequest request, HttpResponse response) {
        if (!request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }
        if (response.getCode() != 200) {
            return;
        }
        if (response.getFirstHeader("Content-Length") == null) {
            response.addHeader("Content-Length", "0");
        }
    }

    private void ensure304DoesNotContainExtraEntityHeaders(HttpResponse response) {
        String[] disallowedEntityHeaders = new String[]{"Allow", "Content-Encoding", "Content-Language", "Content-Length", "Content-MD5", "Content-Range", "Content-Type", "Last-Modified"};
        if (response.getCode() == 304) {
            for (String hdr : disallowedEntityHeaders) {
                response.removeHeaders(hdr);
            }
        }
    }

    private void requestDidNotExpect100ContinueButResponseIsOne(HttpRequest originalRequest, HttpResponse response) throws IOException {
        if (response.getCode() != 100) {
            return;
        }
        Header header = originalRequest.getFirstHeader("Expect");
        if (header != null && header.getValue().equalsIgnoreCase("100-continue")) {
            return;
        }
        throw new ClientProtocolException(UNEXPECTED_100_CONTINUE);
    }

    private void transferEncodingIsNotReturnedTo1_0Client(HttpRequest originalRequest, HttpResponse response) {
        ProtocolVersion version;
        ProtocolVersion protocolVersion = version = originalRequest.getVersion() != null ? originalRequest.getVersion() : HttpVersion.DEFAULT;
        if (version.compareToVersion(HttpVersion.HTTP_1_1) >= 0) {
            return;
        }
        this.removeResponseTransferEncoding(response);
    }

    private void removeResponseTransferEncoding(HttpResponse response) {
        response.removeHeaders("TE");
        response.removeHeaders("Transfer-Encoding");
    }
}

