/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SniCompletionEvent;
import io.netty.handler.ssl.SslClientHelloHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import java.util.Locale;

public abstract class AbstractSniHandler<T>
extends SslClientHelloHandler<T> {
    private String hostname;

    private static String extractSniHostname(ByteBuf in) {
        int offset = in.readerIndex();
        int endOffset = in.writerIndex();
        if (endOffset - (offset += 34) >= 6) {
            int extensionsLength;
            int extensionsLimit;
            short sessionIdLength = in.getUnsignedByte(offset);
            int cipherSuitesLength = in.getUnsignedShort(offset += sessionIdLength + 1);
            short compressionMethodLength = in.getUnsignedByte(offset += cipherSuitesLength + 2);
            offset += compressionMethodLength + 1;
            if ((extensionsLimit = (offset += 2) + (extensionsLength = in.getUnsignedShort(offset))) <= endOffset) {
                while (extensionsLimit - offset >= 4) {
                    int extensionLength;
                    int extensionType = in.getUnsignedShort(offset);
                    offset += 2;
                    if (extensionsLimit - (offset += 2) < (extensionLength = in.getUnsignedShort(offset))) break;
                    if (extensionType == 0) {
                        int serverNameLength;
                        if (extensionsLimit - (offset += 2) < 3) break;
                        short serverNameType = in.getUnsignedByte(offset);
                        ++offset;
                        if (serverNameType != 0 || extensionsLimit - (offset += 2) < (serverNameLength = in.getUnsignedShort(offset))) break;
                        String hostname = in.toString(offset, serverNameLength, CharsetUtil.US_ASCII);
                        return hostname.toLowerCase(Locale.US);
                    }
                    offset += extensionLength;
                }
            }
        }
        return null;
    }

    @Override
    protected Future<T> lookup(ChannelHandlerContext ctx, ByteBuf clientHello) throws Exception {
        this.hostname = clientHello == null ? null : AbstractSniHandler.extractSniHostname(clientHello);
        return this.lookup(ctx, this.hostname);
    }

    @Override
    protected void onLookupComplete(ChannelHandlerContext ctx, Future<T> future) throws Exception {
        try {
            this.onLookupComplete(ctx, this.hostname, future);
        } finally {
            AbstractSniHandler.fireSniCompletionEvent(ctx, this.hostname, future);
        }
    }

    protected abstract Future<T> lookup(ChannelHandlerContext var1, String var2) throws Exception;

    protected abstract void onLookupComplete(ChannelHandlerContext var1, String var2, Future<T> var3) throws Exception;

    private static void fireSniCompletionEvent(ChannelHandlerContext ctx, String hostname, Future<?> future) {
        Throwable cause = future.cause();
        if (cause == null) {
            ctx.fireUserEventTriggered(new SniCompletionEvent(hostname));
        } else {
            ctx.fireUserEventTriggered(new SniCompletionEvent(hostname, cause));
        }
    }
}

