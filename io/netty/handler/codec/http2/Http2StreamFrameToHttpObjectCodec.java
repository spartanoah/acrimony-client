/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.List;

@ChannelHandler.Sharable
public class Http2StreamFrameToHttpObjectCodec
extends MessageToMessageCodec<Http2StreamFrame, HttpObject> {
    private static final AttributeKey<HttpScheme> SCHEME_ATTR_KEY = AttributeKey.valueOf(HttpScheme.class, (String)"STREAMFRAMECODEC_SCHEME");
    private final boolean isServer;
    private final boolean validateHeaders;

    public Http2StreamFrameToHttpObjectCodec(boolean isServer, boolean validateHeaders) {
        this.isServer = isServer;
        this.validateHeaders = validateHeaders;
    }

    public Http2StreamFrameToHttpObjectCodec(boolean isServer) {
        this(isServer, true);
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return msg instanceof Http2HeadersFrame || msg instanceof Http2DataFrame;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Http2StreamFrame frame, List<Object> out) throws Exception {
        if (frame instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame)frame;
            Http2Headers headers = headersFrame.headers();
            Http2FrameStream stream = headersFrame.stream();
            int id = stream == null ? 0 : stream.id();
            CharSequence status = headers.status();
            if (null != status && HttpResponseStatus.CONTINUE.codeAsText().contentEquals(status)) {
                FullHttpMessage fullMsg = this.newFullMessage(id, headers, ctx.alloc());
                out.add(fullMsg);
                return;
            }
            if (headersFrame.isEndStream()) {
                if (headers.method() == null && status == null) {
                    DefaultLastHttpContent last = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER, this.validateHeaders);
                    HttpConversionUtil.addHttp2ToHttpHeaders(id, headers, last.trailingHeaders(), HttpVersion.HTTP_1_1, true, true);
                    out.add(last);
                } else {
                    FullHttpMessage full = this.newFullMessage(id, headers, ctx.alloc());
                    out.add(full);
                }
            } else {
                HttpMessage req = this.newMessage(id, headers);
                if (!HttpUtil.isContentLengthSet(req)) {
                    req.headers().add((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, (Object)HttpHeaderValues.CHUNKED);
                }
                out.add(req);
            }
        } else if (frame instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame)frame;
            if (dataFrame.isEndStream()) {
                out.add(new DefaultLastHttpContent(dataFrame.content().retain(), this.validateHeaders));
            } else {
                out.add(new DefaultHttpContent(dataFrame.content().retain()));
            }
        }
    }

    private void encodeLastContent(LastHttpContent last, List<Object> out) {
        boolean needFiller;
        boolean bl = needFiller = !(last instanceof FullHttpMessage) && last.trailingHeaders().isEmpty();
        if (last.content().isReadable() || needFiller) {
            out.add(new DefaultHttp2DataFrame(last.content().retain(), last.trailingHeaders().isEmpty()));
        }
        if (!last.trailingHeaders().isEmpty()) {
            Http2Headers headers = HttpConversionUtil.toHttp2Headers(last.trailingHeaders(), this.validateHeaders);
            out.add(new DefaultHttp2HeadersFrame(headers, true));
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpObject obj, List<Object> out) throws Exception {
        HttpResponse res;
        if (obj instanceof HttpResponse && (res = (HttpResponse)obj).status().equals(HttpResponseStatus.CONTINUE)) {
            if (res instanceof FullHttpResponse) {
                Http2Headers headers = this.toHttp2Headers(ctx, res);
                out.add(new DefaultHttp2HeadersFrame(headers, false));
                return;
            }
            throw new EncoderException(HttpResponseStatus.CONTINUE + " must be a FullHttpResponse");
        }
        if (obj instanceof HttpMessage) {
            Http2Headers headers = this.toHttp2Headers(ctx, (HttpMessage)obj);
            boolean noMoreFrames = false;
            if (obj instanceof FullHttpMessage) {
                FullHttpMessage full = (FullHttpMessage)obj;
                noMoreFrames = !full.content().isReadable() && full.trailingHeaders().isEmpty();
            }
            out.add(new DefaultHttp2HeadersFrame(headers, noMoreFrames));
        }
        if (obj instanceof LastHttpContent) {
            LastHttpContent last = (LastHttpContent)obj;
            this.encodeLastContent(last, out);
        } else if (obj instanceof HttpContent) {
            HttpContent cont = (HttpContent)obj;
            out.add(new DefaultHttp2DataFrame(cont.content().retain(), false));
        }
    }

    private Http2Headers toHttp2Headers(ChannelHandlerContext ctx, HttpMessage msg) {
        if (msg instanceof HttpRequest) {
            msg.headers().set((CharSequence)HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), (Object)Http2StreamFrameToHttpObjectCodec.connectionScheme(ctx));
        }
        return HttpConversionUtil.toHttp2Headers(msg, this.validateHeaders);
    }

    private HttpMessage newMessage(int id, Http2Headers headers) throws Http2Exception {
        return this.isServer ? HttpConversionUtil.toHttpRequest(id, headers, this.validateHeaders) : HttpConversionUtil.toHttpResponse(id, headers, this.validateHeaders);
    }

    private FullHttpMessage newFullMessage(int id, Http2Headers headers, ByteBufAllocator alloc) throws Http2Exception {
        return this.isServer ? HttpConversionUtil.toFullHttpRequest(id, headers, alloc, this.validateHeaders) : HttpConversionUtil.toFullHttpResponse(id, headers, alloc, this.validateHeaders);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        Attribute<HttpScheme> schemeAttribute = Http2StreamFrameToHttpObjectCodec.connectionSchemeAttribute(ctx);
        if (schemeAttribute.get() == null) {
            HttpScheme scheme = this.isSsl(ctx) ? HttpScheme.HTTPS : HttpScheme.HTTP;
            schemeAttribute.set(scheme);
        }
    }

    protected boolean isSsl(ChannelHandlerContext ctx) {
        Channel connChannel = Http2StreamFrameToHttpObjectCodec.connectionChannel(ctx);
        return null != connChannel.pipeline().get(SslHandler.class);
    }

    private static HttpScheme connectionScheme(ChannelHandlerContext ctx) {
        HttpScheme scheme = Http2StreamFrameToHttpObjectCodec.connectionSchemeAttribute(ctx).get();
        return scheme == null ? HttpScheme.HTTP : scheme;
    }

    private static Attribute<HttpScheme> connectionSchemeAttribute(ChannelHandlerContext ctx) {
        Channel ch = Http2StreamFrameToHttpObjectCodec.connectionChannel(ctx);
        return ch.attr(SCHEME_ATTR_KEY);
    }

    private static Channel connectionChannel(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        return ch instanceof Http2StreamChannel ? ch.parent() : ch;
    }
}

