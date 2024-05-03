/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http.websocketx.extensions;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionData;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionDecoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionEncoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionUtil;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandshaker;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WebSocketServerExtensionHandler
extends ChannelDuplexHandler {
    private final List<WebSocketServerExtensionHandshaker> extensionHandshakers;
    private List<WebSocketServerExtension> validExtensions;

    public WebSocketServerExtensionHandler(WebSocketServerExtensionHandshaker ... extensionHandshakers) {
        this.extensionHandshakers = Arrays.asList(ObjectUtil.checkNonEmpty(extensionHandshakers, "extensionHandshakers"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String extensionsHeader;
        HttpRequest request;
        if (msg instanceof HttpRequest && WebSocketExtensionUtil.isWebsocketUpgrade((request = (HttpRequest)msg).headers()) && (extensionsHeader = request.headers().getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS)) != null) {
            List<WebSocketExtensionData> extensions = WebSocketExtensionUtil.extractExtensions(extensionsHeader);
            int rsv = 0;
            for (WebSocketExtensionData extensionData : extensions) {
                Iterator<WebSocketServerExtensionHandshaker> extensionHandshakersIterator = this.extensionHandshakers.iterator();
                WebSocketExtension validExtension = null;
                while (validExtension == null && extensionHandshakersIterator.hasNext()) {
                    WebSocketServerExtensionHandshaker extensionHandshaker = extensionHandshakersIterator.next();
                    validExtension = extensionHandshaker.handshakeExtension(extensionData);
                }
                if (validExtension == null || (validExtension.rsv() & rsv) != 0) continue;
                if (this.validExtensions == null) {
                    this.validExtensions = new ArrayList<WebSocketServerExtension>(1);
                }
                rsv |= validExtension.rsv();
                this.validExtensions.add((WebSocketServerExtension)validExtension);
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        HttpResponse httpResponse;
        if (msg instanceof HttpResponse && HttpResponseStatus.SWITCHING_PROTOCOLS.equals((httpResponse = (HttpResponse)msg).status())) {
            this.handlePotentialUpgrade(ctx, promise, httpResponse);
        }
        super.write(ctx, msg, promise);
    }

    private void handlePotentialUpgrade(final ChannelHandlerContext ctx, ChannelPromise promise, HttpResponse httpResponse) {
        HttpHeaders headers = httpResponse.headers();
        if (WebSocketExtensionUtil.isWebsocketUpgrade(headers)) {
            if (this.validExtensions != null) {
                String headerValue = headers.getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);
                ArrayList<WebSocketExtensionData> extraExtensions = new ArrayList<WebSocketExtensionData>(this.extensionHandshakers.size());
                for (WebSocketServerExtension extension : this.validExtensions) {
                    extraExtensions.add(extension.newReponseData());
                }
                String newHeaderValue = WebSocketExtensionUtil.computeMergeExtensionsHeaderValue(headerValue, extraExtensions);
                promise.addListener(new ChannelFutureListener(){

                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            for (WebSocketServerExtension extension : WebSocketServerExtensionHandler.this.validExtensions) {
                                WebSocketExtensionDecoder decoder = extension.newExtensionDecoder();
                                WebSocketExtensionEncoder encoder = extension.newExtensionEncoder();
                                String name = ctx.name();
                                ctx.pipeline().addAfter(name, decoder.getClass().getName(), decoder).addAfter(name, encoder.getClass().getName(), encoder);
                            }
                        }
                    }
                });
                if (newHeaderValue != null) {
                    headers.set((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS, (Object)newHeaderValue);
                }
            }
            promise.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.pipeline().remove(WebSocketServerExtensionHandler.this);
                    }
                }
            });
        }
    }
}

