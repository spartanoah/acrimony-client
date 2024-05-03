/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import javax.net.ssl.SSLSession;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class HttpCoreContext
implements HttpContext {
    public static final String CONNECTION_ENDPOINT = "http.connection-endpoint";
    public static final String SSL_SESSION = "http.ssl-session";
    public static final String HTTP_REQUEST = "http.request";
    public static final String HTTP_RESPONSE = "http.response";
    private final HttpContext context;

    public static HttpCoreContext create() {
        return new HttpCoreContext();
    }

    public static HttpCoreContext adapt(HttpContext context) {
        if (context == null) {
            return new HttpCoreContext();
        }
        if (context instanceof HttpCoreContext) {
            return (HttpCoreContext)context;
        }
        return new HttpCoreContext(context);
    }

    public HttpCoreContext(HttpContext context) {
        this.context = context;
    }

    public HttpCoreContext() {
        this.context = new BasicHttpContext();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.context.getProtocolVersion();
    }

    @Override
    public void setProtocolVersion(ProtocolVersion version) {
        this.context.setProtocolVersion(version);
    }

    @Override
    public Object getAttribute(String id) {
        return this.context.getAttribute(id);
    }

    @Override
    public Object setAttribute(String id, Object obj) {
        return this.context.setAttribute(id, obj);
    }

    @Override
    public Object removeAttribute(String id) {
        return this.context.removeAttribute(id);
    }

    public <T> T getAttribute(String attribname, Class<T> clazz) {
        Args.notNull(clazz, "Attribute class");
        Object obj = this.getAttribute(attribname);
        if (obj == null) {
            return null;
        }
        return clazz.cast(obj);
    }

    public SSLSession getSSLSession() {
        return this.getAttribute(SSL_SESSION, SSLSession.class);
    }

    public EndpointDetails getEndpointDetails() {
        return this.getAttribute(CONNECTION_ENDPOINT, EndpointDetails.class);
    }

    public HttpRequest getRequest() {
        return this.getAttribute(HTTP_REQUEST, HttpRequest.class);
    }

    public HttpResponse getResponse() {
        return this.getAttribute(HTTP_RESPONSE, HttpResponse.class);
    }
}

