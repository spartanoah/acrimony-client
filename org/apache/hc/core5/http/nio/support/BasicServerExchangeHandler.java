/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.support.AbstractServerExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicServerExchangeHandler<T>
extends AbstractServerExchangeHandler<T> {
    private final AsyncServerRequestHandler<T> requestHandler;

    public BasicServerExchangeHandler(AsyncServerRequestHandler<T> requestHandler) {
        this.requestHandler = Args.notNull(requestHandler, "Response handler");
    }

    @Override
    protected AsyncRequestConsumer<T> supplyConsumer(HttpRequest request, EntityDetails entityDetails, HttpContext context) throws HttpException {
        return this.requestHandler.prepare(request, entityDetails, context);
    }

    @Override
    protected void handle(T requestMessage, AsyncServerRequestHandler.ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
        this.requestHandler.handle(requestMessage, responseTrigger, context);
    }
}

