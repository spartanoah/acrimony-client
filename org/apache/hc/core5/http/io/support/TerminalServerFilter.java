/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public final class TerminalServerFilter
implements HttpFilterHandler {
    private final HttpRequestMapper<HttpRequestHandler> handlerMapper;
    private final HttpResponseFactory<ClassicHttpResponse> responseFactory;

    public TerminalServerFilter(HttpRequestMapper<HttpRequestHandler> handlerMapper, HttpResponseFactory<ClassicHttpResponse> responseFactory) {
        this.handlerMapper = Args.notNull(handlerMapper, "Handler mapper");
        this.responseFactory = responseFactory != null ? responseFactory : DefaultClassicHttpResponseFactory.INSTANCE;
    }

    @Override
    public final void handle(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger, HttpContext context, HttpFilterChain chain) throws HttpException, IOException {
        ClassicHttpResponse response = this.responseFactory.newHttpResponse(200);
        HttpRequestHandler handler = this.handlerMapper.resolve(request, context);
        if (handler != null) {
            handler.handle(request, response, context);
        } else {
            response.setCode(501);
        }
        responseTrigger.submitResponse(response);
    }
}

