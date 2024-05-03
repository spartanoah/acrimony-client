/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

public abstract class SimpleUserEventChannelHandler<I>
extends ChannelInboundHandlerAdapter {
    private final TypeParameterMatcher matcher;
    private final boolean autoRelease;

    protected SimpleUserEventChannelHandler() {
        this(true);
    }

    protected SimpleUserEventChannelHandler(boolean autoRelease) {
        this.matcher = TypeParameterMatcher.find(this, SimpleUserEventChannelHandler.class, "I");
        this.autoRelease = autoRelease;
    }

    protected SimpleUserEventChannelHandler(Class<? extends I> eventType) {
        this(eventType, true);
    }

    protected SimpleUserEventChannelHandler(Class<? extends I> eventType, boolean autoRelease) {
        this.matcher = TypeParameterMatcher.get(eventType);
        this.autoRelease = autoRelease;
    }

    protected boolean acceptEvent(Object evt) throws Exception {
        return this.matcher.match(evt);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean release = true;
        try {
            if (this.acceptEvent(evt)) {
                Object ievt = evt;
                this.eventReceived(ctx, ievt);
            } else {
                release = false;
                ctx.fireUserEventTriggered(evt);
            }
        } finally {
            if (this.autoRelease && release) {
                ReferenceCountUtil.release(evt);
            }
        }
    }

    protected abstract void eventReceived(ChannelHandlerContext var1, I var2) throws Exception;
}

