/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Iterator;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Args;

public abstract class AbstractMessageWrapper
implements HttpMessage {
    private final HttpMessage message;

    public AbstractMessageWrapper(HttpMessage message) {
        this.message = Args.notNull(message, "Message");
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.message.setVersion(version);
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.message.getVersion();
    }

    @Override
    public void addHeader(Header header) {
        this.message.addHeader(header);
    }

    @Override
    public void addHeader(String name, Object value) {
        this.message.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        this.message.setHeader(header);
    }

    @Override
    public void setHeader(String name, Object value) {
        this.message.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header ... headers) {
        this.message.setHeaders(headers);
    }

    @Override
    public boolean removeHeader(Header header) {
        return this.message.removeHeader(header);
    }

    @Override
    public boolean removeHeaders(String name) {
        return this.message.removeHeaders(name);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.message.containsHeader(name);
    }

    @Override
    public int countHeaders(String name) {
        return this.message.countHeaders(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return this.message.getHeaders(name);
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        return this.message.getHeader(name);
    }

    @Override
    public Header getFirstHeader(String name) {
        return this.message.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return this.message.getLastHeader(name);
    }

    @Override
    public Header[] getHeaders() {
        return this.message.getHeaders();
    }

    @Override
    public Iterator<Header> headerIterator() {
        return this.message.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return this.message.headerIterator(name);
    }

    public String toString() {
        return this.message.toString();
    }
}

