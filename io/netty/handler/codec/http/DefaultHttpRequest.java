/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http;

import io.netty.handler.codec.http.DefaultHttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

public class DefaultHttpRequest
extends DefaultHttpMessage
implements HttpRequest {
    private HttpMethod method;
    private String uri;

    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        this(httpVersion, method, uri, true);
    }

    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
        super(httpVersion, validateHeaders);
        if (method == null) {
            throw new NullPointerException("method");
        }
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.method = method;
        this.uri = uri;
    }

    @Override
    public HttpMethod getMethod() {
        return this.method;
    }

    @Override
    public String getUri() {
        return this.uri;
    }

    @Override
    public HttpRequest setMethod(HttpMethod method) {
        if (method == null) {
            throw new NullPointerException("method");
        }
        this.method = method;
        return this;
    }

    @Override
    public HttpRequest setUri(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.uri = uri;
        return this;
    }

    @Override
    public HttpRequest setProtocolVersion(HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(StringUtil.simpleClassName(this));
        buf.append("(decodeResult: ");
        buf.append(this.getDecoderResult());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
        buf.append(this.getMethod());
        buf.append(' ');
        buf.append(this.getUri());
        buf.append(' ');
        buf.append(this.getProtocolVersion().text());
        buf.append(StringUtil.NEWLINE);
        this.appendHeaders(buf);
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }
}

