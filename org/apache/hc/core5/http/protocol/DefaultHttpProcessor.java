/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import java.util.List;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public final class DefaultHttpProcessor
implements HttpProcessor {
    private final HttpRequestInterceptor[] requestInterceptors;
    private final HttpResponseInterceptor[] responseInterceptors;

    public DefaultHttpProcessor(HttpRequestInterceptor[] requestInterceptors, HttpResponseInterceptor[] responseInterceptors) {
        int l;
        if (requestInterceptors != null) {
            l = requestInterceptors.length;
            this.requestInterceptors = new HttpRequestInterceptor[l];
            System.arraycopy(requestInterceptors, 0, this.requestInterceptors, 0, l);
        } else {
            this.requestInterceptors = new HttpRequestInterceptor[0];
        }
        if (responseInterceptors != null) {
            l = responseInterceptors.length;
            this.responseInterceptors = new HttpResponseInterceptor[l];
            System.arraycopy(responseInterceptors, 0, this.responseInterceptors, 0, l);
        } else {
            this.responseInterceptors = new HttpResponseInterceptor[0];
        }
    }

    public DefaultHttpProcessor(List<HttpRequestInterceptor> requestInterceptors, List<HttpResponseInterceptor> responseInterceptors) {
        int l;
        if (requestInterceptors != null) {
            l = requestInterceptors.size();
            this.requestInterceptors = requestInterceptors.toArray(new HttpRequestInterceptor[l]);
        } else {
            this.requestInterceptors = new HttpRequestInterceptor[0];
        }
        if (responseInterceptors != null) {
            l = responseInterceptors.size();
            this.responseInterceptors = responseInterceptors.toArray(new HttpResponseInterceptor[l]);
        } else {
            this.responseInterceptors = new HttpResponseInterceptor[0];
        }
    }

    public DefaultHttpProcessor(HttpRequestInterceptor ... requestInterceptors) {
        this(requestInterceptors, (HttpResponseInterceptor[])null);
    }

    public DefaultHttpProcessor(HttpResponseInterceptor ... responseInterceptors) {
        this((HttpRequestInterceptor[])null, responseInterceptors);
    }

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws IOException, HttpException {
        for (HttpRequestInterceptor requestInterceptor : this.requestInterceptors) {
            requestInterceptor.process(request, entity, context);
        }
    }

    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws IOException, HttpException {
        for (HttpResponseInterceptor responseInterceptor : this.responseInterceptors) {
            responseInterceptor.process(response, entity, context);
        }
    }
}

