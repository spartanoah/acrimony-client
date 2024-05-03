/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import org.apache.hc.client5.http.classic.ExecRuntime;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Args;

public final class CloseableHttpResponse
implements ClassicHttpResponse {
    private final ClassicHttpResponse response;
    private final ExecRuntime execRuntime;

    static CloseableHttpResponse adapt(ClassicHttpResponse response) {
        if (response == null) {
            return null;
        }
        return response instanceof CloseableHttpResponse ? (CloseableHttpResponse)response : new CloseableHttpResponse(response, null);
    }

    CloseableHttpResponse(ClassicHttpResponse response, ExecRuntime execRuntime) {
        this.response = Args.notNull(response, "Response");
        this.execRuntime = execRuntime;
    }

    @Override
    public int getCode() {
        return this.response.getCode();
    }

    @Override
    public HttpEntity getEntity() {
        return this.response.getEntity();
    }

    @Override
    public boolean containsHeader(String name) {
        return this.response.containsHeader(name);
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.response.setVersion(version);
    }

    @Override
    public void setCode(int code) {
        this.response.setCode(code);
    }

    @Override
    public String getReasonPhrase() {
        return this.response.getReasonPhrase();
    }

    @Override
    public int countHeaders(String name) {
        return this.response.countHeaders(name);
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.response.setEntity(entity);
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.response.getVersion();
    }

    @Override
    public void setReasonPhrase(String reason) {
        this.response.setReasonPhrase(reason);
    }

    @Override
    public Header[] getHeaders(String name) {
        return this.response.getHeaders(name);
    }

    @Override
    public void addHeader(Header header) {
        this.response.addHeader(header);
    }

    @Override
    public Locale getLocale() {
        return this.response.getLocale();
    }

    @Override
    public void addHeader(String name, Object value) {
        this.response.addHeader(name, value);
    }

    @Override
    public void setLocale(Locale loc) {
        this.response.setLocale(loc);
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        return this.response.getHeader(name);
    }

    @Override
    public void setHeader(Header header) {
        this.response.setHeader(header);
    }

    @Override
    public Header getFirstHeader(String name) {
        return this.response.getFirstHeader(name);
    }

    @Override
    public void setHeader(String name, Object value) {
        this.response.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header ... headers) {
        this.response.setHeaders(headers);
    }

    @Override
    public boolean removeHeader(Header header) {
        return this.response.removeHeader(header);
    }

    @Override
    public boolean removeHeaders(String name) {
        return this.response.removeHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return this.response.getLastHeader(name);
    }

    @Override
    public Header[] getHeaders() {
        return this.response.getHeaders();
    }

    @Override
    public Iterator<Header> headerIterator() {
        return this.response.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return this.response.headerIterator(name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        if (this.execRuntime != null) {
            try {
                this.response.close();
                this.execRuntime.disconnectEndpoint();
            } finally {
                this.execRuntime.discardEndpoint();
            }
        } else {
            this.response.close();
        }
    }

    public String toString() {
        return this.response.toString();
    }
}

