/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.execchain;

import java.io.IOException;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.execchain.ConnectionHolder;
import org.apache.http.impl.execchain.ResponseEntityProxy;
import org.apache.http.params.HttpParams;

class HttpResponseProxy
implements CloseableHttpResponse {
    private final HttpResponse original;
    private final ConnectionHolder connHolder;

    public HttpResponseProxy(HttpResponse original, ConnectionHolder connHolder) {
        this.original = original;
        this.connHolder = connHolder;
        ResponseEntityProxy.enchance(original, connHolder);
    }

    @Override
    public void close() throws IOException {
        if (this.connHolder != null) {
            this.connHolder.close();
        }
    }

    @Override
    public StatusLine getStatusLine() {
        return this.original.getStatusLine();
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        this.original.setStatusLine(statusline);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        this.original.setStatusLine(ver, code);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        this.original.setStatusLine(ver, code, reason);
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        this.original.setStatusCode(code);
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        this.original.setReasonPhrase(reason);
    }

    @Override
    public HttpEntity getEntity() {
        return this.original.getEntity();
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.original.setEntity(entity);
    }

    @Override
    public Locale getLocale() {
        return this.original.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        this.original.setLocale(loc);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.original.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(String name) {
        return this.original.containsHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return this.original.getHeaders(name);
    }

    @Override
    public Header getFirstHeader(String name) {
        return this.original.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return this.original.getLastHeader(name);
    }

    @Override
    public Header[] getAllHeaders() {
        return this.original.getAllHeaders();
    }

    @Override
    public void addHeader(Header header) {
        this.original.addHeader(header);
    }

    @Override
    public void addHeader(String name, String value) {
        this.original.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        this.original.setHeader(header);
    }

    @Override
    public void setHeader(String name, String value) {
        this.original.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        this.original.setHeaders(headers);
    }

    @Override
    public void removeHeader(Header header) {
        this.original.removeHeader(header);
    }

    @Override
    public void removeHeaders(String name) {
        this.original.removeHeaders(name);
    }

    @Override
    public HeaderIterator headerIterator() {
        return this.original.headerIterator();
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return this.original.headerIterator(name);
    }

    @Override
    public HttpParams getParams() {
        return this.original.getParams();
    }

    @Override
    public void setParams(HttpParams params) {
        this.original.setParams(params);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("HttpResponseProxy{");
        sb.append(this.original);
        sb.append('}');
        return sb.toString();
    }
}

