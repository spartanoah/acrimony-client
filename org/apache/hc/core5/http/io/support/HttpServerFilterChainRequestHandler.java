/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.io.support.HttpServerFilterChainElement;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class HttpServerFilterChainRequestHandler
implements HttpServerRequestHandler {
    private final HttpServerFilterChainElement filterChain;

    public HttpServerFilterChainRequestHandler(HttpServerFilterChainElement filterChain) {
        this.filterChain = Args.notNull(filterChain, "Filter chain");
    }

    @Override
    public void handle(ClassicHttpRequest request, final HttpServerRequestHandler.ResponseTrigger trigger, HttpContext context) throws HttpException, IOException {
        this.filterChain.handle(request, new HttpFilterChain.ResponseTrigger(){

            @Override
            public void sendInformation(ClassicHttpResponse response) throws HttpException, IOException {
                trigger.sendInformation(response);
            }

            @Override
            public void submitResponse(ClassicHttpResponse response) throws HttpException, IOException {
                trigger.submitResponse(response);
            }
        }, context);
    }
}

