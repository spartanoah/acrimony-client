/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityProducer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.util.Args;

public class AsyncRequestBuilder {
    private String method;
    private URI uri;
    private Charset charset;
    private ProtocolVersion version;
    private HeaderGroup headerGroup;
    private AsyncEntityProducer entityProducer;
    private List<NameValuePair> parameters;

    AsyncRequestBuilder() {
    }

    AsyncRequestBuilder(String method) {
        this.method = method;
    }

    AsyncRequestBuilder(Method method) {
        this(method.name());
    }

    AsyncRequestBuilder(String method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    AsyncRequestBuilder(Method method, URI uri) {
        this(method.name(), uri);
    }

    AsyncRequestBuilder(Method method, String uri) {
        this(method.name(), uri != null ? URI.create(uri) : null);
    }

    AsyncRequestBuilder(String method, String uri) {
        this(method, uri != null ? URI.create(uri) : null);
    }

    public static AsyncRequestBuilder create(String method) {
        Args.notBlank(method, "HTTP method");
        return new AsyncRequestBuilder(method);
    }

    public static AsyncRequestBuilder get() {
        return new AsyncRequestBuilder(Method.GET);
    }

    public static AsyncRequestBuilder get(URI uri) {
        return new AsyncRequestBuilder(Method.GET, uri);
    }

    public static AsyncRequestBuilder get(String uri) {
        return new AsyncRequestBuilder(Method.GET, uri);
    }

    public static AsyncRequestBuilder head() {
        return new AsyncRequestBuilder(Method.HEAD);
    }

    public static AsyncRequestBuilder head(URI uri) {
        return new AsyncRequestBuilder(Method.HEAD, uri);
    }

    public static AsyncRequestBuilder head(String uri) {
        return new AsyncRequestBuilder(Method.HEAD, uri);
    }

    public static AsyncRequestBuilder patch() {
        return new AsyncRequestBuilder(Method.PATCH);
    }

    public static AsyncRequestBuilder patch(URI uri) {
        return new AsyncRequestBuilder(Method.PATCH, uri);
    }

    public static AsyncRequestBuilder patch(String uri) {
        return new AsyncRequestBuilder(Method.PATCH, uri);
    }

    public static AsyncRequestBuilder post() {
        return new AsyncRequestBuilder(Method.POST);
    }

    public static AsyncRequestBuilder post(URI uri) {
        return new AsyncRequestBuilder(Method.POST, uri);
    }

    public static AsyncRequestBuilder post(String uri) {
        return new AsyncRequestBuilder(Method.POST, uri);
    }

    public static AsyncRequestBuilder put() {
        return new AsyncRequestBuilder(Method.PUT);
    }

    public static AsyncRequestBuilder put(URI uri) {
        return new AsyncRequestBuilder(Method.PUT, uri);
    }

    public static AsyncRequestBuilder put(String uri) {
        return new AsyncRequestBuilder(Method.PUT, uri);
    }

    public static AsyncRequestBuilder delete() {
        return new AsyncRequestBuilder(Method.DELETE);
    }

    public static AsyncRequestBuilder delete(URI uri) {
        return new AsyncRequestBuilder(Method.DELETE, uri);
    }

    public static AsyncRequestBuilder delete(String uri) {
        return new AsyncRequestBuilder(Method.DELETE, uri);
    }

    public static AsyncRequestBuilder trace() {
        return new AsyncRequestBuilder(Method.TRACE);
    }

    public static AsyncRequestBuilder trace(URI uri) {
        return new AsyncRequestBuilder(Method.TRACE, uri);
    }

    public static AsyncRequestBuilder trace(String uri) {
        return new AsyncRequestBuilder(Method.TRACE, uri);
    }

    public static AsyncRequestBuilder options() {
        return new AsyncRequestBuilder(Method.OPTIONS);
    }

    public static AsyncRequestBuilder options(URI uri) {
        return new AsyncRequestBuilder(Method.OPTIONS, uri);
    }

    public static AsyncRequestBuilder options(String uri) {
        return new AsyncRequestBuilder(Method.OPTIONS, uri);
    }

    public AsyncRequestBuilder setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getMethod() {
        return this.method;
    }

    public URI getUri() {
        return this.uri;
    }

    public AsyncRequestBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public AsyncRequestBuilder setUri(String uri) {
        this.uri = uri != null ? URI.create(uri) : null;
        return this;
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public AsyncRequestBuilder setVersion(ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public Header[] getHeaders(String name) {
        return this.headerGroup != null ? this.headerGroup.getHeaders(name) : null;
    }

    public AsyncRequestBuilder setHeaders(Header ... headers) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeaders(headers);
        return this;
    }

    public Header getFirstHeader(String name) {
        return this.headerGroup != null ? this.headerGroup.getFirstHeader(name) : null;
    }

    public Header getLastHeader(String name) {
        return this.headerGroup != null ? this.headerGroup.getLastHeader(name) : null;
    }

    public AsyncRequestBuilder addHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(header);
        return this;
    }

    public AsyncRequestBuilder addHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public AsyncRequestBuilder removeHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.removeHeader(header);
        return this;
    }

    public AsyncRequestBuilder removeHeaders(String name) {
        if (name == null || this.headerGroup == null) {
            return this;
        }
        Iterator<Header> i = this.headerGroup.headerIterator();
        while (i.hasNext()) {
            Header header = i.next();
            if (!name.equalsIgnoreCase(header.getName())) continue;
            i.remove();
        }
        return this;
    }

    public AsyncRequestBuilder setHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(header);
        return this;
    }

    public AsyncRequestBuilder setHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(new BasicHeader(name, value));
        return this;
    }

    public List<NameValuePair> getParameters() {
        return this.parameters != null ? new ArrayList<NameValuePair>(this.parameters) : new ArrayList();
    }

    public AsyncRequestBuilder addParameter(NameValuePair nvp) {
        Args.notNull(nvp, "Name value pair");
        if (this.parameters == null) {
            this.parameters = new LinkedList<NameValuePair>();
        }
        this.parameters.add(nvp);
        return this;
    }

    public AsyncRequestBuilder addParameter(String name, String value) {
        return this.addParameter(new BasicNameValuePair(name, value));
    }

    public AsyncRequestBuilder addParameters(NameValuePair ... nvps) {
        for (NameValuePair nvp : nvps) {
            this.addParameter(nvp);
        }
        return this;
    }

    public AsyncEntityProducer getEntity() {
        return this.entityProducer;
    }

    public AsyncRequestBuilder setEntity(AsyncEntityProducer entityProducer) {
        this.entityProducer = entityProducer;
        return this;
    }

    public AsyncRequestBuilder setEntity(String content, ContentType contentType) {
        this.entityProducer = new BasicAsyncEntityProducer(content, contentType);
        return this;
    }

    public AsyncRequestBuilder setEntity(String content) {
        this.entityProducer = new BasicAsyncEntityProducer(content);
        return this;
    }

    public AsyncRequestBuilder setEntity(byte[] content, ContentType contentType) {
        this.entityProducer = new BasicAsyncEntityProducer(content, contentType);
        return this;
    }

    public AsyncRequestProducer build() {
        URI uriCopy = this.uri != null ? this.uri : URI.create("/");
        AsyncEntityProducer entityProducerCopy = this.entityProducer;
        if (this.parameters != null && !this.parameters.isEmpty()) {
            if (entityProducerCopy == null && (Method.POST.isSame(this.method) || Method.PUT.isSame(this.method))) {
                String content = URLEncodedUtils.format(this.parameters, this.charset != null ? this.charset : ContentType.APPLICATION_FORM_URLENCODED.getCharset());
                entityProducerCopy = new StringAsyncEntityProducer(content, ContentType.APPLICATION_FORM_URLENCODED);
            } else {
                try {
                    uriCopy = new URIBuilder(uriCopy).setCharset(this.charset).addParameters(this.parameters).build();
                } catch (URISyntaxException ex) {
                    // empty catch block
                }
            }
        }
        if (entityProducerCopy != null && Method.TRACE.isSame(this.method)) {
            throw new IllegalStateException((Object)((Object)Method.TRACE) + " requests may not include an entity.");
        }
        BasicHttpRequest request = new BasicHttpRequest(this.method, uriCopy);
        if (this.headerGroup != null) {
            request.setHeaders(this.headerGroup.getHeaders());
        }
        if (this.version != null) {
            request.setVersion(this.version);
        }
        return new BasicRequestProducer(request, entityProducerCopy);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AsyncRequestBuilder [method=");
        builder.append(this.method);
        builder.append(", charset=");
        builder.append(this.charset);
        builder.append(", version=");
        builder.append(this.version);
        builder.append(", uri=");
        builder.append(this.uri);
        builder.append(", headerGroup=");
        builder.append(this.headerGroup);
        builder.append(", entity=");
        builder.append(this.entityProducer != null ? this.entityProducer.getClass() : null);
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append("]");
        return builder.toString();
    }
}

