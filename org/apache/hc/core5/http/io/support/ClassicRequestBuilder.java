/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Args;

public class ClassicRequestBuilder {
    private String method;
    private URI uri;
    private Charset charset;
    private ProtocolVersion version;
    private HeaderGroup headerGroup;
    private HttpEntity entity;
    private List<NameValuePair> parameters;

    ClassicRequestBuilder() {
    }

    ClassicRequestBuilder(String method) {
        this.method = method;
    }

    ClassicRequestBuilder(Method method) {
        this(method.name());
    }

    ClassicRequestBuilder(String method, URI uri) {
        this.method = method;
        this.uri = uri;
    }

    ClassicRequestBuilder(Method method, URI uri) {
        this(method.name(), uri);
    }

    ClassicRequestBuilder(Method method, String uri) {
        this(method.name(), uri != null ? URI.create(uri) : null);
    }

    ClassicRequestBuilder(String method, String uri) {
        this(method, uri != null ? URI.create(uri) : null);
    }

    public static ClassicRequestBuilder create(String method) {
        Args.notBlank(method, "HTTP method");
        return new ClassicRequestBuilder(method);
    }

    public static ClassicRequestBuilder get() {
        return new ClassicRequestBuilder(Method.GET);
    }

    public static ClassicRequestBuilder get(URI uri) {
        return new ClassicRequestBuilder(Method.GET, uri);
    }

    public static ClassicRequestBuilder get(String uri) {
        return new ClassicRequestBuilder(Method.GET, uri);
    }

    public static ClassicRequestBuilder head() {
        return new ClassicRequestBuilder(Method.HEAD);
    }

    public static ClassicRequestBuilder head(URI uri) {
        return new ClassicRequestBuilder(Method.HEAD, uri);
    }

    public static ClassicRequestBuilder head(String uri) {
        return new ClassicRequestBuilder(Method.HEAD, uri);
    }

    public static ClassicRequestBuilder patch() {
        return new ClassicRequestBuilder(Method.PATCH);
    }

    public static ClassicRequestBuilder patch(URI uri) {
        return new ClassicRequestBuilder(Method.PATCH, uri);
    }

    public static ClassicRequestBuilder patch(String uri) {
        return new ClassicRequestBuilder(Method.PATCH, uri);
    }

    public static ClassicRequestBuilder post() {
        return new ClassicRequestBuilder(Method.POST);
    }

    public static ClassicRequestBuilder post(URI uri) {
        return new ClassicRequestBuilder(Method.POST, uri);
    }

    public static ClassicRequestBuilder post(String uri) {
        return new ClassicRequestBuilder(Method.POST, uri);
    }

    public static ClassicRequestBuilder put() {
        return new ClassicRequestBuilder(Method.PUT);
    }

    public static ClassicRequestBuilder put(URI uri) {
        return new ClassicRequestBuilder(Method.PUT, uri);
    }

    public static ClassicRequestBuilder put(String uri) {
        return new ClassicRequestBuilder(Method.PUT, uri);
    }

    public static ClassicRequestBuilder delete() {
        return new ClassicRequestBuilder(Method.DELETE);
    }

    public static ClassicRequestBuilder delete(URI uri) {
        return new ClassicRequestBuilder(Method.DELETE, uri);
    }

    public static ClassicRequestBuilder delete(String uri) {
        return new ClassicRequestBuilder(Method.DELETE, uri);
    }

    public static ClassicRequestBuilder trace() {
        return new ClassicRequestBuilder(Method.TRACE);
    }

    public static ClassicRequestBuilder trace(URI uri) {
        return new ClassicRequestBuilder(Method.TRACE, uri);
    }

    public static ClassicRequestBuilder trace(String uri) {
        return new ClassicRequestBuilder(Method.TRACE, uri);
    }

    public static ClassicRequestBuilder options() {
        return new ClassicRequestBuilder(Method.OPTIONS);
    }

    public static ClassicRequestBuilder options(URI uri) {
        return new ClassicRequestBuilder(Method.OPTIONS, uri);
    }

    public static ClassicRequestBuilder options(String uri) {
        return new ClassicRequestBuilder(Method.OPTIONS, uri);
    }

    public ClassicRequestBuilder setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getMethod() {
        return this.method;
    }

    public ProtocolVersion getVersion() {
        return this.version;
    }

    public ClassicRequestBuilder setVersion(ProtocolVersion version) {
        this.version = version;
        return this;
    }

    public URI getUri() {
        return this.uri;
    }

    public ClassicRequestBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public ClassicRequestBuilder setUri(String uri) {
        this.uri = uri != null ? URI.create(uri) : null;
        return this;
    }

    public Header[] getHeaders(String name) {
        return this.headerGroup != null ? this.headerGroup.getHeaders(name) : null;
    }

    public ClassicRequestBuilder setHeaders(Header ... headers) {
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

    public ClassicRequestBuilder addHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(header);
        return this;
    }

    public ClassicRequestBuilder addHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.addHeader(new BasicHeader(name, value));
        return this;
    }

    public ClassicRequestBuilder removeHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.removeHeader(header);
        return this;
    }

    public ClassicRequestBuilder removeHeaders(String name) {
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

    public ClassicRequestBuilder setHeader(Header header) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(header);
        return this;
    }

    public ClassicRequestBuilder setHeader(String name, String value) {
        if (this.headerGroup == null) {
            this.headerGroup = new HeaderGroup();
        }
        this.headerGroup.setHeader(new BasicHeader(name, value));
        return this;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public ClassicRequestBuilder setEntity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    public ClassicRequestBuilder setEntity(String content, ContentType contentType) {
        this.entity = new StringEntity(content, contentType);
        return this;
    }

    public ClassicRequestBuilder setEntity(String content) {
        this.entity = new StringEntity(content);
        return this;
    }

    public ClassicRequestBuilder setEntity(byte[] content, ContentType contentType) {
        this.entity = new ByteArrayEntity(content, contentType);
        return this;
    }

    public List<NameValuePair> getParameters() {
        return this.parameters != null ? new ArrayList<NameValuePair>(this.parameters) : new ArrayList();
    }

    public ClassicRequestBuilder addParameter(NameValuePair nvp) {
        Args.notNull(nvp, "Name value pair");
        if (this.parameters == null) {
            this.parameters = new LinkedList<NameValuePair>();
        }
        this.parameters.add(nvp);
        return this;
    }

    public ClassicRequestBuilder addParameter(String name, String value) {
        return this.addParameter(new BasicNameValuePair(name, value));
    }

    public ClassicRequestBuilder addParameters(NameValuePair ... nvps) {
        for (NameValuePair nvp : nvps) {
            this.addParameter(nvp);
        }
        return this;
    }

    public ClassicHttpRequest build() {
        URI uriCopy = this.uri != null ? this.uri : URI.create("/");
        HttpEntity entityCopy = this.entity;
        if (this.parameters != null && !this.parameters.isEmpty()) {
            if (entityCopy == null && (Method.POST.isSame(this.method) || Method.PUT.isSame(this.method))) {
                entityCopy = HttpEntities.createUrlEncoded(this.parameters, this.charset);
            } else {
                try {
                    uriCopy = new URIBuilder(uriCopy).setCharset(this.charset).addParameters(this.parameters).build();
                } catch (URISyntaxException ex) {
                    // empty catch block
                }
            }
        }
        if (entityCopy != null && Method.TRACE.isSame(this.method)) {
            throw new IllegalStateException((Object)((Object)Method.TRACE) + " requests may not include an entity");
        }
        BasicClassicHttpRequest result = new BasicClassicHttpRequest(this.method, uriCopy);
        result.setVersion(this.version != null ? this.version : HttpVersion.HTTP_1_1);
        if (this.headerGroup != null) {
            result.setHeaders(this.headerGroup.getHeaders());
        }
        result.setEntity(entityCopy);
        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClassicRequestBuilder [method=");
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
        builder.append(this.entity != null ? this.entity.getClass() : null);
        builder.append(", parameters=");
        builder.append(this.parameters);
        builder.append("]");
        return builder.toString();
    }
}

