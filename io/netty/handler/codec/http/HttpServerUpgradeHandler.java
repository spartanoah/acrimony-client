/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HttpServerUpgradeHandler
extends HttpObjectAggregator {
    private final SourceCodec sourceCodec;
    private final UpgradeCodecFactory upgradeCodecFactory;
    private final boolean validateHeaders;
    private boolean handlingUpgrade;

    public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory) {
        this(sourceCodec, upgradeCodecFactory, 0);
    }

    public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory, int maxContentLength) {
        this(sourceCodec, upgradeCodecFactory, maxContentLength, true);
    }

    public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory, int maxContentLength, boolean validateHeaders) {
        super(maxContentLength);
        this.sourceCodec = ObjectUtil.checkNotNull(sourceCodec, "sourceCodec");
        this.upgradeCodecFactory = ObjectUtil.checkNotNull(upgradeCodecFactory, "upgradeCodecFactory");
        this.validateHeaders = validateHeaders;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
        FullHttpRequest fullRequest;
        if (!this.handlingUpgrade) {
            if (!(msg instanceof HttpRequest)) {
                ReferenceCountUtil.retain(msg);
                ctx.fireChannelRead(msg);
                return;
            }
            HttpRequest req = (HttpRequest)msg;
            if (req.headers().contains(HttpHeaderNames.UPGRADE) && this.shouldHandleUpgradeRequest(req)) {
                this.handlingUpgrade = true;
            } else {
                ReferenceCountUtil.retain(msg);
                ctx.fireChannelRead(msg);
                return;
            }
        }
        if (msg instanceof FullHttpRequest) {
            fullRequest = (FullHttpRequest)msg;
            ReferenceCountUtil.retain(msg);
            out.add(msg);
        } else {
            super.decode(ctx, (Object)msg, (List)out);
            if (out.isEmpty()) {
                return;
            }
            assert (out.size() == 1);
            this.handlingUpgrade = false;
            fullRequest = (FullHttpRequest)out.get(0);
        }
        if (this.upgrade(ctx, fullRequest)) {
            out.clear();
        }
    }

    protected boolean shouldHandleUpgradeRequest(HttpRequest req) {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean upgrade(ChannelHandlerContext ctx, FullHttpRequest request) {
        List<CharSequence> requestedProtocols = HttpServerUpgradeHandler.splitHeader(request.headers().get(HttpHeaderNames.UPGRADE));
        int numRequestedProtocols = requestedProtocols.size();
        UpgradeCodec upgradeCodec = null;
        CharSequence upgradeProtocol = null;
        for (int i = 0; i < numRequestedProtocols; ++i) {
            CharSequence p = requestedProtocols.get(i);
            UpgradeCodec c = this.upgradeCodecFactory.newUpgradeCodec(p);
            if (c == null) continue;
            upgradeProtocol = p;
            upgradeCodec = c;
            break;
        }
        if (upgradeCodec == null) {
            return false;
        }
        List<String> connectionHeaderValues = request.headers().getAll(HttpHeaderNames.CONNECTION);
        if (connectionHeaderValues == null || connectionHeaderValues.isEmpty()) {
            return false;
        }
        StringBuilder concatenatedConnectionValue = new StringBuilder(connectionHeaderValues.size() * 10);
        for (CharSequence charSequence : connectionHeaderValues) {
            concatenatedConnectionValue.append(charSequence).append(',');
        }
        concatenatedConnectionValue.setLength(concatenatedConnectionValue.length() - 1);
        Collection<CharSequence> requiredHeaders = upgradeCodec.requiredUpgradeHeaders();
        List<CharSequence> list = HttpServerUpgradeHandler.splitHeader(concatenatedConnectionValue);
        if (!AsciiString.containsContentEqualsIgnoreCase(list, HttpHeaderNames.UPGRADE) || !AsciiString.containsAllContentEqualsIgnoreCase(list, requiredHeaders)) {
            return false;
        }
        for (CharSequence requiredHeader : requiredHeaders) {
            if (request.headers().contains(requiredHeader)) continue;
            return false;
        }
        FullHttpResponse upgradeResponse = this.createUpgradeResponse(upgradeProtocol);
        if (!upgradeCodec.prepareUpgradeResponse(ctx, request, upgradeResponse.headers())) {
            return false;
        }
        UpgradeEvent event = new UpgradeEvent(upgradeProtocol, request);
        try {
            ChannelFuture writeComplete = ctx.writeAndFlush(upgradeResponse);
            this.sourceCodec.upgradeFrom(ctx);
            upgradeCodec.upgradeTo(ctx, request);
            ctx.pipeline().remove(this);
            ctx.fireUserEventTriggered(event.retain());
            writeComplete.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } finally {
            event.release();
        }
        return true;
    }

    private FullHttpResponse createUpgradeResponse(CharSequence upgradeProtocol) {
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS, Unpooled.EMPTY_BUFFER, this.validateHeaders);
        res.headers().add((CharSequence)HttpHeaderNames.CONNECTION, (Object)HttpHeaderValues.UPGRADE);
        res.headers().add((CharSequence)HttpHeaderNames.UPGRADE, (Object)upgradeProtocol);
        return res;
    }

    private static List<CharSequence> splitHeader(CharSequence header) {
        StringBuilder builder = new StringBuilder(header.length());
        ArrayList<CharSequence> protocols = new ArrayList<CharSequence>(4);
        for (int i = 0; i < header.length(); ++i) {
            char c = header.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (c == ',') {
                protocols.add(builder.toString());
                builder.setLength(0);
                continue;
            }
            builder.append(c);
        }
        if (builder.length() > 0) {
            protocols.add(builder.toString());
        }
        return protocols;
    }

    public static final class UpgradeEvent
    implements ReferenceCounted {
        private final CharSequence protocol;
        private final FullHttpRequest upgradeRequest;

        UpgradeEvent(CharSequence protocol, FullHttpRequest upgradeRequest) {
            this.protocol = protocol;
            this.upgradeRequest = upgradeRequest;
        }

        public CharSequence protocol() {
            return this.protocol;
        }

        public FullHttpRequest upgradeRequest() {
            return this.upgradeRequest;
        }

        @Override
        public int refCnt() {
            return this.upgradeRequest.refCnt();
        }

        @Override
        public UpgradeEvent retain() {
            this.upgradeRequest.retain();
            return this;
        }

        @Override
        public UpgradeEvent retain(int increment) {
            this.upgradeRequest.retain(increment);
            return this;
        }

        public UpgradeEvent touch() {
            this.upgradeRequest.touch();
            return this;
        }

        public UpgradeEvent touch(Object hint) {
            this.upgradeRequest.touch(hint);
            return this;
        }

        @Override
        public boolean release() {
            return this.upgradeRequest.release();
        }

        @Override
        public boolean release(int decrement) {
            return this.upgradeRequest.release(decrement);
        }

        public String toString() {
            return "UpgradeEvent [protocol=" + this.protocol + ", upgradeRequest=" + this.upgradeRequest + ']';
        }
    }

    public static interface UpgradeCodecFactory {
        public UpgradeCodec newUpgradeCodec(CharSequence var1);
    }

    public static interface UpgradeCodec {
        public Collection<CharSequence> requiredUpgradeHeaders();

        public boolean prepareUpgradeResponse(ChannelHandlerContext var1, FullHttpRequest var2, HttpHeaders var3);

        public void upgradeTo(ChannelHandlerContext var1, FullHttpRequest var2);
    }

    public static interface SourceCodec {
        public void upgradeFrom(ChannelHandlerContext var1);
    }
}

