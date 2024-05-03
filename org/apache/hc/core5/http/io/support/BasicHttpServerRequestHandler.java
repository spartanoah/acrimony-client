/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicHttpServerRequestHandler
implements HttpServerRequestHandler {
    private final HttpRequestMapper<HttpRequestHandler> handlerMapper;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public BasicHttpServerRequestHandler(HttpRequestMapper<HttpRequestHandler> handlerMapper, HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.handlerMapper = Args.notNull(handlerMapper, "Handler mapper");
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    public BasicHttpServerRequestHandler(HttpRequestMapper<HttpRequestHandler> handlerMapper) {
        this(handlerMapper, null);
    }

    @Override
    public void handle(ClassicHttpRequest request, HttpServerRequestHandler.ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
        HttpRequestHandler handler;
        ClassicHttpResponse response = this.responseFactory.newHttpResponse(200);
        HttpRequestHandler httpRequestHandler = handler = this.handlerMapper != null ? this.handlerMapper.resolve(request, context) : null;
        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(501);
        }
        responseTrigger.submitResponse(response);
    }
}

