/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.ModalCloseable;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class CloseableHttpClient
implements HttpClient,
ModalCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(CloseableHttpClient.class);

    protected abstract CloseableHttpResponse doExecute(HttpHost var1, ClassicHttpRequest var2, HttpContext var3) throws IOException;

    @Override
    public CloseableHttpResponse execute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
        return this.doExecute(target, request, context);
    }

    @Override
    public CloseableHttpResponse execute(ClassicHttpRequest request, HttpContext context) throws IOException {
        Args.notNull(request, "HTTP request");
        return this.doExecute(CloseableHttpClient.determineTarget(request), request, context);
    }

    private static HttpHost determineTarget(ClassicHttpRequest request) throws ClientProtocolException {
        try {
            return RoutingSupport.determineHost(request);
        } catch (HttpException ex) {
            throw new ClientProtocolException(ex);
        }
    }

    @Override
    public CloseableHttpResponse execute(ClassicHttpRequest request) throws IOException {
        return this.execute(request, (HttpContext)null);
    }

    @Override
    public CloseableHttpResponse execute(HttpHost target, ClassicHttpRequest request) throws IOException {
        return this.doExecute(target, request, null);
    }

    @Override
    public <T> T execute(ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
        return this.execute(request, null, responseHandler);
    }

    @Override
    public <T> T execute(ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
        HttpHost target = CloseableHttpClient.determineTarget(request);
        return this.execute(target, request, context, responseHandler);
    }

    @Override
    public <T> T execute(HttpHost target, ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
        return this.execute(target, request, null, responseHandler);
    }

    @Override
    public <T> T execute(HttpHost target, ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
        Args.notNull(responseHandler, "Response handler");
        Throwable throwable = null;
        try (CloseableHttpResponse response = this.execute(target, request, context);){
            T result = responseHandler.handleResponse(response);
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            T t = result;
            return t;
        } catch (HttpException t) {
            try {
                HttpEntity entity = response.getEntity();
                try {
                    EntityUtils.consume(entity);
                } catch (Exception t2) {
                    LOG.warn("Error consuming content after an exception.", t2);
                }
                throw new ClientProtocolException(t);
            } catch (Throwable throwable2) {
                throwable = throwable2;
                throw throwable2;
            }
        }
    }
}

