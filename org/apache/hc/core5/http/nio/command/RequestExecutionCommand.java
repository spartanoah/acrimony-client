/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.command;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.command.ExecutableCommand;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Internal
public final class RequestExecutionCommand
extends ExecutableCommand {
    private final AsyncClientExchangeHandler exchangeHandler;
    private final HandlerFactory<AsyncPushConsumer> pushHandlerFactory;
    private final CancellableDependency cancellableDependency;
    private final HttpContext context;

    public RequestExecutionCommand(AsyncClientExchangeHandler exchangeHandler, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, CancellableDependency cancellableDependency, HttpContext context) {
        this.exchangeHandler = Args.notNull(exchangeHandler, "Handler");
        this.pushHandlerFactory = pushHandlerFactory;
        this.cancellableDependency = cancellableDependency;
        this.context = context;
    }

    public RequestExecutionCommand(AsyncClientExchangeHandler exchangeHandler, HandlerFactory<AsyncPushConsumer> pushHandlerFactory, HttpContext context) {
        this(exchangeHandler, pushHandlerFactory, null, context);
    }

    public RequestExecutionCommand(AsyncClientExchangeHandler exchangeHandler, HttpContext context) {
        this(exchangeHandler, null, null, context);
    }

    public AsyncClientExchangeHandler getExchangeHandler() {
        return this.exchangeHandler;
    }

    public HandlerFactory<AsyncPushConsumer> getPushHandlerFactory() {
        return this.pushHandlerFactory;
    }

    @Override
    public CancellableDependency getCancellableDependency() {
        return this.cancellableDependency;
    }

    public HttpContext getContext() {
        return this.context;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void failed(Exception ex) {
        try {
            this.exchangeHandler.failed(ex);
        } finally {
            this.exchangeHandler.releaseResources();
        }
    }

    @Override
    public boolean cancel() {
        this.exchangeHandler.cancel();
        return true;
    }
}

