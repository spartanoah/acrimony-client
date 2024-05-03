/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl.ocsp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.internal.ObjectUtil;
import javax.net.ssl.SSLHandshakeException;

public abstract class OcspClientHandler
extends ChannelInboundHandlerAdapter {
    private final ReferenceCountedOpenSslEngine engine;

    protected OcspClientHandler(ReferenceCountedOpenSslEngine engine) {
        this.engine = ObjectUtil.checkNotNull(engine, "engine");
    }

    protected abstract boolean verify(ChannelHandlerContext var1, ReferenceCountedOpenSslEngine var2) throws Exception;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent) {
            ctx.pipeline().remove(this);
            SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent)evt;
            if (event.isSuccess() && !this.verify(ctx, this.engine)) {
                throw new SSLHandshakeException("Bad OCSP response");
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
}

