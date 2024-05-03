/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.collection.CharObjectMap;
import io.netty.util.internal.ObjectUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Http2ClientUpgradeCodec
implements HttpClientUpgradeHandler.UpgradeCodec {
    private static final List<CharSequence> UPGRADE_HEADERS = Collections.singletonList(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER);
    private final String handlerName;
    private final Http2ConnectionHandler connectionHandler;
    private final ChannelHandler upgradeToHandler;
    private final ChannelHandler http2MultiplexHandler;

    public Http2ClientUpgradeCodec(Http2FrameCodec frameCodec, ChannelHandler upgradeToHandler) {
        this(null, frameCodec, upgradeToHandler);
    }

    public Http2ClientUpgradeCodec(String handlerName, Http2FrameCodec frameCodec, ChannelHandler upgradeToHandler) {
        this(handlerName, frameCodec, upgradeToHandler, null);
    }

    public Http2ClientUpgradeCodec(Http2ConnectionHandler connectionHandler) {
        this((String)null, connectionHandler);
    }

    public Http2ClientUpgradeCodec(Http2ConnectionHandler connectionHandler, Http2MultiplexHandler http2MultiplexHandler) {
        this((String)null, connectionHandler, http2MultiplexHandler);
    }

    public Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler) {
        this(handlerName, connectionHandler, connectionHandler, null);
    }

    public Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler, Http2MultiplexHandler http2MultiplexHandler) {
        this(handlerName, connectionHandler, connectionHandler, http2MultiplexHandler);
    }

    private Http2ClientUpgradeCodec(String handlerName, Http2ConnectionHandler connectionHandler, ChannelHandler upgradeToHandler, Http2MultiplexHandler http2MultiplexHandler) {
        this.handlerName = handlerName;
        this.connectionHandler = ObjectUtil.checkNotNull(connectionHandler, "connectionHandler");
        this.upgradeToHandler = ObjectUtil.checkNotNull(upgradeToHandler, "upgradeToHandler");
        this.http2MultiplexHandler = http2MultiplexHandler;
    }

    @Override
    public CharSequence protocol() {
        return Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME;
    }

    @Override
    public Collection<CharSequence> setUpgradeHeaders(ChannelHandlerContext ctx, HttpRequest upgradeRequest) {
        CharSequence settingsValue = this.getSettingsHeaderValue(ctx);
        upgradeRequest.headers().set(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER, (Object)settingsValue);
        return UPGRADE_HEADERS;
    }

    @Override
    public void upgradeTo(ChannelHandlerContext ctx, FullHttpResponse upgradeResponse) throws Exception {
        try {
            ctx.pipeline().addAfter(ctx.name(), this.handlerName, this.upgradeToHandler);
            if (this.http2MultiplexHandler != null) {
                String name = ctx.pipeline().context(this.connectionHandler).name();
                ctx.pipeline().addAfter(name, null, this.http2MultiplexHandler);
            }
            this.connectionHandler.onHttpClientUpgrade();
        } catch (Http2Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private CharSequence getSettingsHeaderValue(ChannelHandlerContext ctx) {
        String string;
        ByteBuf buf = null;
        ByteBuf encodedBuf = null;
        try {
            Http2Settings settings = this.connectionHandler.decoder().localSettings();
            int payloadLength = 6 * settings.size();
            buf = ctx.alloc().buffer(payloadLength);
            for (CharObjectMap.PrimitiveEntry entry : settings.entries()) {
                buf.writeChar(entry.key());
                buf.writeInt(((Long)entry.value()).intValue());
            }
            encodedBuf = Base64.encode(buf, Base64Dialect.URL_SAFE);
            string = encodedBuf.toString(CharsetUtil.UTF_8);
        } catch (Throwable throwable) {
            ReferenceCountUtil.release(buf);
            ReferenceCountUtil.release(encodedBuf);
            throw throwable;
        }
        ReferenceCountUtil.release(buf);
        ReferenceCountUtil.release(encodedBuf);
        return string;
    }
}

