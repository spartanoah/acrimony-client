/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.MisdirectedRequestException;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.support.ImmediateResponseExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public final class DefaultAsyncResponseExchangeHandlerFactory
implements HandlerFactory<AsyncServerExchangeHandler> {
    private final HttpRequestMapper<Supplier<AsyncServerExchangeHandler>> mapper;
    private final Decorator<AsyncServerExchangeHandler> decorator;

    public DefaultAsyncResponseExchangeHandlerFactory(HttpRequestMapper<Supplier<AsyncServerExchangeHandler>> mapper, Decorator<AsyncServerExchangeHandler> decorator) {
        this.mapper = Args.notNull(mapper, "Request handler mapper");
        this.decorator = decorator;
    }

    public DefaultAsyncResponseExchangeHandlerFactory(HttpRequestMapper<Supplier<AsyncServerExchangeHandler>> mapper) {
        this(mapper, null);
    }

    private AsyncServerExchangeHandler createHandler(HttpRequest request, HttpContext context) throws HttpException {
        try {
            Supplier<AsyncServerExchangeHandler> supplier = this.mapper.resolve(request, context);
            return supplier != null ? supplier.get() : new ImmediateResponseExchangeHandler(404, "Resource not found");
        } catch (MisdirectedRequestException ex) {
            return new ImmediateResponseExchangeHandler(421, "Not authoritative");
        }
    }

    @Override
    public AsyncServerExchangeHandler create(HttpRequest request, HttpContext context) throws HttpException {
        AsyncServerExchangeHandler handler = this.createHandler(request, context);
        if (handler != null) {
            return this.decorator != null ? this.decorator.decorate(handler) : handler;
        }
        return null;
    }
}

