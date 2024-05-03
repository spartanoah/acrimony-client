/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.ssl.NotSslRecordException;
import io.netty.handler.ssl.SniCompletionEvent;
import io.netty.handler.ssl.SslUtils;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.List;

public abstract class SslClientHelloHandler<T>
extends ByteToMessageDecoder
implements ChannelOutboundHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslClientHelloHandler.class);
    private boolean handshakeFailed;
    private boolean suppressRead;
    private boolean readPending;
    private ByteBuf handshakeBuffer;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!this.suppressRead && !this.handshakeFailed) {
            try {
                int packetLength;
                int readerIndex = in.readerIndex();
                int handshakeLength = -1;
                block7: for (int readableBytes = in.readableBytes(); readableBytes >= 5; readableBytes -= packetLength) {
                    short contentType = in.getUnsignedByte(readerIndex);
                    switch (contentType) {
                        case 20: 
                        case 21: {
                            int len = SslUtils.getEncryptedPacketLength(in, readerIndex);
                            if (len == -2) {
                                this.handshakeFailed = true;
                                NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
                                in.skipBytes(in.readableBytes());
                                ctx.fireUserEventTriggered(new SniCompletionEvent(e));
                                SslUtils.handleHandshakeFailure(ctx, e, true);
                                throw e;
                            }
                            if (len == -1) {
                                return;
                            }
                            this.select(ctx, null);
                            return;
                        }
                        case 22: {
                            short majorVersion = in.getUnsignedByte(readerIndex + 1);
                            if (majorVersion != 3) break;
                            packetLength = in.getUnsignedShort(readerIndex + 3) + 5;
                            if (readableBytes < packetLength) {
                                return;
                            }
                            if (packetLength == 5) {
                                this.select(ctx, null);
                                return;
                            }
                            int endOffset = readerIndex + packetLength;
                            if (handshakeLength == -1) {
                                if (readerIndex + 4 > endOffset) {
                                    return;
                                }
                                short handshakeType = in.getUnsignedByte(readerIndex + 5);
                                if (handshakeType != 1) {
                                    this.select(ctx, null);
                                    return;
                                }
                                handshakeLength = in.getUnsignedMedium(readerIndex + 5 + 1);
                                readerIndex += 4;
                                if (handshakeLength + 4 + 5 <= (packetLength -= 4)) {
                                    this.select(ctx, in.retainedSlice(readerIndex += 5, handshakeLength));
                                    return;
                                }
                                if (this.handshakeBuffer == null) {
                                    this.handshakeBuffer = ctx.alloc().buffer(handshakeLength);
                                } else {
                                    this.handshakeBuffer.clear();
                                }
                            }
                            this.handshakeBuffer.writeBytes(in, readerIndex + 5, packetLength - 5);
                            readerIndex += packetLength;
                            if (handshakeLength > this.handshakeBuffer.readableBytes()) continue block7;
                            ByteBuf clientHello = this.handshakeBuffer.setIndex(0, handshakeLength);
                            this.handshakeBuffer = null;
                            this.select(ctx, clientHello);
                            return;
                        }
                    }
                    this.select(ctx, null);
                    return;
                }
            } catch (NotSslRecordException e) {
                throw e;
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unexpected client hello packet: " + ByteBufUtil.hexDump(in), e);
                }
                this.select(ctx, null);
            }
        }
    }

    private void releaseHandshakeBuffer() {
        SslClientHelloHandler.releaseIfNotNull(this.handshakeBuffer);
        this.handshakeBuffer = null;
    }

    private static void releaseIfNotNull(ByteBuf buffer) {
        if (buffer != null) {
            buffer.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void select(final ChannelHandlerContext ctx, ByteBuf clientHello) throws Exception {
        try {
            Future<T> future = this.lookup(ctx, clientHello);
            if (future.isDone()) {
                this.onLookupComplete(ctx, future);
            } else {
                this.suppressRead = true;
                final ByteBuf finalClientHello = clientHello;
                future.addListener(new FutureListener<T>(){

                    @Override
                    public void operationComplete(Future<T> future) {
                        SslClientHelloHandler.releaseIfNotNull(finalClientHello);
                        try {
                            SslClientHelloHandler.this.suppressRead = false;
                            try {
                                SslClientHelloHandler.this.onLookupComplete(ctx, future);
                            } catch (DecoderException err) {
                                ctx.fireExceptionCaught(err);
                            } catch (Exception cause) {
                                ctx.fireExceptionCaught(new DecoderException(cause));
                            } catch (Throwable cause) {
                                ctx.fireExceptionCaught(cause);
                            }
                        } finally {
                            if (SslClientHelloHandler.this.readPending) {
                                SslClientHelloHandler.this.readPending = false;
                                ctx.read();
                            }
                        }
                    }
                });
                clientHello = null;
            }
        } catch (Throwable cause) {
            PlatformDependent.throwException(cause);
        } finally {
            SslClientHelloHandler.releaseIfNotNull(clientHello);
        }
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        this.releaseHandshakeBuffer();
        super.handlerRemoved0(ctx);
    }

    protected abstract Future<T> lookup(ChannelHandlerContext var1, ByteBuf var2) throws Exception;

    protected abstract void onLookupComplete(ChannelHandlerContext var1, Future<T> var2) throws Exception;

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        if (this.suppressRead) {
            this.readPending = true;
        } else {
            ctx.read();
        }
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}

