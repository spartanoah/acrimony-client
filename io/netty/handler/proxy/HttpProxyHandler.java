/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class HttpProxyHandler
extends ProxyHandler {
    private static final String PROTOCOL = "http";
    private static final String AUTH_BASIC = "basic";
    private final HttpClientCodecWrapper codecWrapper = new HttpClientCodecWrapper();
    private final String username;
    private final String password;
    private final CharSequence authorization;
    private final HttpHeaders outboundHeaders;
    private final boolean ignoreDefaultPortsInConnectHostHeader;
    private HttpResponseStatus status;
    private HttpHeaders inboundHeaders;

    public HttpProxyHandler(SocketAddress proxyAddress) {
        this(proxyAddress, null);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, HttpHeaders headers) {
        this(proxyAddress, headers, false);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, HttpHeaders headers, boolean ignoreDefaultPortsInConnectHostHeader) {
        super(proxyAddress);
        this.username = null;
        this.password = null;
        this.authorization = null;
        this.outboundHeaders = headers;
        this.ignoreDefaultPortsInConnectHostHeader = ignoreDefaultPortsInConnectHostHeader;
    }

    public HttpProxyHandler(SocketAddress proxyAddress, String username, String password) {
        this(proxyAddress, username, password, null);
    }

    public HttpProxyHandler(SocketAddress proxyAddress, String username, String password, HttpHeaders headers) {
        this(proxyAddress, username, password, headers, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public HttpProxyHandler(SocketAddress proxyAddress, String username, String password, HttpHeaders headers, boolean ignoreDefaultPortsInConnectHostHeader) {
        super(proxyAddress);
        ByteBuf authzBase64;
        this.username = ObjectUtil.checkNotNull(username, "username");
        this.password = ObjectUtil.checkNotNull(password, "password");
        ByteBuf authz = Unpooled.copiedBuffer(username + ':' + password, CharsetUtil.UTF_8);
        try {
            authzBase64 = Base64.encode(authz, false);
        } finally {
            authz.release();
        }
        try {
            this.authorization = new AsciiString("Basic " + authzBase64.toString(CharsetUtil.US_ASCII));
        } finally {
            authzBase64.release();
        }
        this.outboundHeaders = headers;
        this.ignoreDefaultPortsInConnectHostHeader = ignoreDefaultPortsInConnectHostHeader;
    }

    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public String authScheme() {
        return this.authorization != null ? AUTH_BASIC : "none";
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();
        p.addBefore(name, null, this.codecWrapper);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
        this.codecWrapper.codec.removeOutboundHandler();
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
        this.codecWrapper.codec.removeInboundHandler();
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress raddr = (InetSocketAddress)this.destinationAddress();
        String hostString = HttpUtil.formatHostnameForHttp(raddr);
        int port = raddr.getPort();
        String url = hostString + ":" + port;
        String hostHeader = this.ignoreDefaultPortsInConnectHostHeader && (port == 80 || port == 443) ? hostString : url;
        DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url, Unpooled.EMPTY_BUFFER, false);
        req.headers().set((CharSequence)HttpHeaderNames.HOST, (Object)hostHeader);
        if (this.authorization != null) {
            req.headers().set((CharSequence)HttpHeaderNames.PROXY_AUTHORIZATION, (Object)this.authorization);
        }
        if (this.outboundHeaders != null) {
            req.headers().add(this.outboundHeaders);
        }
        return req;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        boolean finished;
        if (response instanceof HttpResponse) {
            if (this.status != null) {
                throw new HttpProxyConnectException(this.exceptionMessage("too many responses"), null);
            }
            HttpResponse res = (HttpResponse)response;
            this.status = res.status();
            this.inboundHeaders = res.headers();
        }
        if (finished = response instanceof LastHttpContent) {
            if (this.status == null) {
                throw new HttpProxyConnectException(this.exceptionMessage("missing response"), this.inboundHeaders);
            }
            if (this.status.code() != 200) {
                throw new HttpProxyConnectException(this.exceptionMessage("status: " + this.status), this.inboundHeaders);
            }
        }
        return finished;
    }

    private static final class HttpClientCodecWrapper
    implements ChannelInboundHandler,
    ChannelOutboundHandler {
        final HttpClientCodec codec = new HttpClientCodec();

        private HttpClientCodecWrapper() {
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            this.codec.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            this.codec.handlerRemoved(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            this.codec.exceptionCaught(ctx, cause);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            this.codec.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelReadComplete(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            this.codec.userEventTriggered(ctx, evt);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            this.codec.channelWritabilityChanged(ctx);
        }

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.codec.bind(ctx, localAddress, promise);
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.codec.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.codec.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.codec.close(ctx, promise);
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.codec.deregister(ctx, promise);
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            this.codec.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            this.codec.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            this.codec.flush(ctx);
        }
    }

    public static final class HttpProxyConnectException
    extends ProxyConnectException {
        private static final long serialVersionUID = -8824334609292146066L;
        private final HttpHeaders headers;

        public HttpProxyConnectException(String message, HttpHeaders headers) {
            super(message);
            this.headers = headers;
        }

        public HttpHeaders headers() {
            return this.headers;
        }
    }
}

