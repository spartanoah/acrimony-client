/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class HttpCacheEntry
implements MessageHeaders,
Serializable {
    private static final long serialVersionUID = -6300496422359477413L;
    private static final String REQUEST_METHOD_HEADER_NAME = "Hc-Request-Method";
    private final Date requestDate;
    private final Date responseDate;
    private final int status;
    private final HeaderGroup responseHeaders;
    private final Resource resource;
    private final Map<String, String> variantMap;
    private final Date date;

    public HttpCacheEntry(Date requestDate, Date responseDate, int status, Header[] responseHeaders, Resource resource, Map<String, String> variantMap) {
        Args.notNull(requestDate, "Request date");
        Args.notNull(responseDate, "Response date");
        Args.check(status >= 200, "Status code");
        Args.notNull(responseHeaders, "Response headers");
        this.requestDate = requestDate;
        this.responseDate = responseDate;
        this.status = status;
        this.responseHeaders = new HeaderGroup();
        this.responseHeaders.setHeaders(responseHeaders);
        this.resource = resource;
        this.variantMap = variantMap != null ? new HashMap<String, String>(variantMap) : null;
        this.date = this.parseDate();
    }

    public HttpCacheEntry(Date requestDate, Date responseDate, int status, Header[] responseHeaders, Resource resource) {
        this(requestDate, responseDate, status, responseHeaders, resource, new HashMap<String, String>());
    }

    private Date parseDate() {
        return DateUtils.parseDate(this, "Date");
    }

    public int getStatus() {
        return this.status;
    }

    public Date getRequestDate() {
        return this.requestDate;
    }

    public Date getResponseDate() {
        return this.responseDate;
    }

    @Override
    public Header[] getHeaders() {
        HeaderGroup filteredHeaders = new HeaderGroup();
        Iterator<Header> iterator = this.responseHeaders.headerIterator();
        while (iterator.hasNext()) {
            Header header = iterator.next();
            if (REQUEST_METHOD_HEADER_NAME.equals(header.getName())) continue;
            filteredHeaders.addHeader(header);
        }
        return filteredHeaders.getHeaders();
    }

    @Override
    public Header getFirstHeader(String name) {
        if (REQUEST_METHOD_HEADER_NAME.equalsIgnoreCase(name)) {
            return null;
        }
        return this.responseHeaders.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return this.responseHeaders.getLastHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        if (REQUEST_METHOD_HEADER_NAME.equalsIgnoreCase(name)) {
            return new Header[0];
        }
        return this.responseHeaders.getHeaders(name);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.responseHeaders.containsHeader(name);
    }

    @Override
    public int countHeaders(String name) {
        return this.responseHeaders.countHeaders(name);
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        return this.responseHeaders.getHeader(name);
    }

    @Override
    public Iterator<Header> headerIterator() {
        return this.responseHeaders.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return this.responseHeaders.headerIterator(name);
    }

    public Date getDate() {
        return this.date;
    }

    public Resource getResource() {
        return this.resource;
    }

    public boolean hasVariants() {
        return this.getFirstHeader("Vary") != null;
    }

    public Map<String, String> getVariantMap() {
        return Collections.unmodifiableMap(this.variantMap);
    }

    public String getRequestMethod() {
        Header requestMethodHeader = this.responseHeaders.getFirstHeader(REQUEST_METHOD_HEADER_NAME);
        if (requestMethodHeader != null) {
            return requestMethodHeader.getValue();
        }
        return "GET";
    }

    public String toString() {
        return "[request date=" + this.requestDate + "; response date=" + this.responseDate + "; status=" + this.status + "]";
    }
}

