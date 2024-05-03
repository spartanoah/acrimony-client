/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.internal.ObjectUtil;
import java.util.List;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

public abstract class DelegatingSslContext
extends SslContext {
    private final SslContext ctx;

    protected DelegatingSslContext(SslContext ctx) {
        this.ctx = ObjectUtil.checkNotNull(ctx, "ctx");
    }

    @Override
    public final boolean isClient() {
        return this.ctx.isClient();
    }

    @Override
    public final List<String> cipherSuites() {
        return this.ctx.cipherSuites();
    }

    @Override
    public final long sessionCacheSize() {
        return this.ctx.sessionCacheSize();
    }

    @Override
    public final long sessionTimeout() {
        return this.ctx.sessionTimeout();
    }

    public final ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return this.ctx.applicationProtocolNegotiator();
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc) {
        SSLEngine engine = this.ctx.newEngine(alloc);
        this.initEngine(engine);
        return engine;
    }

    @Override
    public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        SSLEngine engine = this.ctx.newEngine(alloc, peerHost, peerPort);
        this.initEngine(engine);
        return engine;
    }

    protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls) {
        SslHandler handler = this.ctx.newHandler(alloc, startTls);
        this.initHandler(handler);
        return handler;
    }

    protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls) {
        SslHandler handler = this.ctx.newHandler(alloc, peerHost, peerPort, startTls);
        this.initHandler(handler);
        return handler;
    }

    protected SslHandler newHandler(ByteBufAllocator alloc, boolean startTls, Executor executor) {
        SslHandler handler = this.ctx.newHandler(alloc, startTls, executor);
        this.initHandler(handler);
        return handler;
    }

    protected SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls, Executor executor) {
        SslHandler handler = this.ctx.newHandler(alloc, peerHost, peerPort, startTls, executor);
        this.initHandler(handler);
        return handler;
    }

    public final SSLSessionContext sessionContext() {
        return this.ctx.sessionContext();
    }

    protected abstract void initEngine(SSLEngine var1);

    protected void initHandler(SslHandler handler) {
        this.initEngine(handler.engine());
    }
}

