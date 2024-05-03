/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

public final class HttpServerFilterChainElement {
    private final HttpFilterHandler handler;
    private final HttpServerFilterChainElement next;
    private final HttpFilterChain filterChain;

    public HttpServerFilterChainElement(HttpFilterHandler handler, final HttpServerFilterChainElement next) {
        this.handler = handler;
        this.next = next;
        this.filterChain = new HttpFilterChain(){

            @Override
            public void proceed(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
                next.handle(request, responseTrigger, context);
            }
        };
    }

    public void handle(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger, HttpContext context) throws IOException, HttpException {
        this.handler.handle(request, responseTrigger, context, this.filterChain);
    }

    public String toString() {
        return "{handler=" + this.handler.getClass() + ", next=" + (this.next != null ? this.next.handler.getClass() : "null") + '}';
    }
}

