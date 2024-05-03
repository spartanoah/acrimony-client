/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncFilterHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

public final class AsyncServerFilterChainElement {
    private final AsyncFilterHandler handler;
    private final AsyncServerFilterChainElement next;
    private final AsyncFilterChain filterChain;

    public AsyncServerFilterChainElement(AsyncFilterHandler handler, final AsyncServerFilterChainElement next) {
        this.handler = handler;
        this.next = next;
        this.filterChain = new AsyncFilterChain(){

            @Override
            public AsyncDataConsumer proceed(HttpRequest request, EntityDetails entityDetails, HttpContext context, AsyncFilterChain.ResponseTrigger responseTrigger) throws HttpException, IOException {
                return next.handle(request, entityDetails, context, responseTrigger);
            }
        };
    }

    public AsyncDataConsumer handle(HttpRequest request, EntityDetails entityDetails, HttpContext context, AsyncFilterChain.ResponseTrigger responseTrigger) throws HttpException, IOException {
        return this.handler.handle(request, entityDetails, context, responseTrigger, this.filterChain);
    }

    public String toString() {
        return "{handler=" + this.handler.getClass() + ", next=" + (this.next != null ? this.next.handler.getClass() : "null") + '}';
    }
}

