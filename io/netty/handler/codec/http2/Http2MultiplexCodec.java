/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.AbstractHttp2StreamChannel;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Frame;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2FrameStreamException;
import io.netty.handler.codec.http2.Http2FrameStreamVisitor;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.handler.codec.http2.MaxCapacityQueue;
import java.util.ArrayDeque;
import java.util.Queue;

@Deprecated
public class Http2MultiplexCodec
extends Http2FrameCodec {
    private final ChannelHandler inboundStreamHandler;
    private final ChannelHandler upgradeStreamHandler;
    private final Queue<AbstractHttp2StreamChannel> readCompletePendingQueue = new MaxCapacityQueue<AbstractHttp2StreamChannel>(new ArrayDeque(8), 100);
    private boolean parentReadInProgress;
    private int idCount;
    volatile ChannelHandlerContext ctx;

    Http2MultiplexCodec(Http2ConnectionEncoder encoder, Http2ConnectionDecoder decoder, Http2Settings initialSettings, ChannelHandler inboundStreamHandler, ChannelHandler upgradeStreamHandler, boolean decoupleCloseAndGoAway) {
        super(encoder, decoder, initialSettings, decoupleCloseAndGoAway);
        this.inboundStreamHandler = inboundStreamHandler;
        this.upgradeStreamHandler = upgradeStreamHandler;
    }

    @Override
    public void onHttpClientUpgrade() throws Http2Exception {
        if (this.upgradeStreamHandler == null) {
            throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Client is misconfigured for upgrade requests", new Object[0]);
        }
        super.onHttpClientUpgrade();
    }

    @Override
    public final void handlerAdded0(ChannelHandlerContext ctx) throws Exception {
        if (ctx.executor() != ctx.channel().eventLoop()) {
            throw new IllegalStateException("EventExecutor must be EventLoop of Channel");
        }
        this.ctx = ctx;
    }

    @Override
    public final void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved0(ctx);
        this.readCompletePendingQueue.clear();
    }

    @Override
    final void onHttp2Frame(ChannelHandlerContext ctx, Http2Frame frame) {
        if (frame instanceof Http2StreamFrame) {
            Http2StreamFrame streamFrame = (Http2StreamFrame)frame;
            AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel)((Http2FrameCodec.DefaultHttp2FrameStream)streamFrame.stream()).attachment;
            channel.fireChildRead(streamFrame);
            return;
        }
        if (frame instanceof Http2GoAwayFrame) {
            this.onHttp2GoAwayFrame(ctx, (Http2GoAwayFrame)frame);
        }
        ctx.fireChannelRead(frame);
    }

    @Override
    final void onHttp2StreamStateChanged(ChannelHandlerContext ctx, Http2FrameCodec.DefaultHttp2FrameStream stream) {
        switch (stream.state()) {
            case HALF_CLOSED_LOCAL: {
                if (stream.id() != 1) break;
            }
            case HALF_CLOSED_REMOTE: 
            case OPEN: {
                Http2MultiplexCodecStreamChannel streamChannel;
                if (stream.attachment != null) break;
                if (stream.id() == 1 && !this.connection().isServer()) {
                    assert (this.upgradeStreamHandler != null);
                    streamChannel = new Http2MultiplexCodecStreamChannel(stream, this.upgradeStreamHandler);
                    streamChannel.closeOutbound();
                } else {
                    streamChannel = new Http2MultiplexCodecStreamChannel(stream, this.inboundStreamHandler);
                }
                ChannelFuture future = ctx.channel().eventLoop().register(streamChannel);
                if (future.isDone()) {
                    Http2MultiplexHandler.registerDone(future);
                    break;
                }
                future.addListener(Http2MultiplexHandler.CHILD_CHANNEL_REGISTRATION_LISTENER);
                break;
            }
            case CLOSED: {
                AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel)stream.attachment;
                if (channel == null) break;
                channel.streamClosed();
                break;
            }
        }
    }

    final Http2StreamChannel newOutboundStream() {
        return new Http2MultiplexCodecStreamChannel(this.newStream(), null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    final void onHttp2FrameStreamException(ChannelHandlerContext ctx, Http2FrameStreamException cause) {
        Http2FrameStream stream = cause.stream();
        AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel)((Http2FrameCodec.DefaultHttp2FrameStream)stream).attachment;
        try {
            channel.pipeline().fireExceptionCaught(cause.getCause());
        } finally {
            channel.unsafe().closeForcibly();
        }
    }

    private void onHttp2GoAwayFrame(ChannelHandlerContext ctx, final Http2GoAwayFrame goAwayFrame) {
        if (goAwayFrame.lastStreamId() == Integer.MAX_VALUE) {
            return;
        }
        try {
            this.forEachActiveStream(new Http2FrameStreamVisitor(){

                @Override
                public boolean visit(Http2FrameStream stream) {
                    int streamId = stream.id();
                    AbstractHttp2StreamChannel channel = (AbstractHttp2StreamChannel)((Http2FrameCodec.DefaultHttp2FrameStream)stream).attachment;
                    if (streamId > goAwayFrame.lastStreamId() && Http2MultiplexCodec.this.connection().local().isValidStreamId(streamId)) {
                        channel.pipeline().fireUserEventTriggered(goAwayFrame.retainedDuplicate());
                    }
                    return true;
                }
            });
        } catch (Http2Exception e) {
            ctx.fireExceptionCaught(e);
            ctx.close();
        }
    }

    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.processPendingReadCompleteQueue();
        this.channelReadComplete0(ctx);
    }

    private void processPendingReadCompleteQueue() {
        this.parentReadInProgress = true;
        try {
            AbstractHttp2StreamChannel childChannel;
            while ((childChannel = this.readCompletePendingQueue.poll()) != null) {
                childChannel.fireChildReadComplete();
            }
        } finally {
            this.parentReadInProgress = false;
            this.readCompletePendingQueue.clear();
            this.flush0(this.ctx);
        }
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.parentReadInProgress = true;
        super.channelRead(ctx, msg);
    }

    @Override
    public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            this.forEachActiveStream(AbstractHttp2StreamChannel.WRITABLE_VISITOR);
        }
        super.channelWritabilityChanged(ctx);
    }

    final void flush0(ChannelHandlerContext ctx) {
        this.flush(ctx);
    }

    private final class Http2MultiplexCodecStreamChannel
    extends AbstractHttp2StreamChannel {
        Http2MultiplexCodecStreamChannel(Http2FrameCodec.DefaultHttp2FrameStream stream, ChannelHandler inboundHandler) {
            super(stream, ++Http2MultiplexCodec.this.idCount, inboundHandler);
        }

        @Override
        protected boolean isParentReadInProgress() {
            return Http2MultiplexCodec.this.parentReadInProgress;
        }

        @Override
        protected void addChannelToReadCompletePendingQueue() {
            while (!Http2MultiplexCodec.this.readCompletePendingQueue.offer(this)) {
                Http2MultiplexCodec.this.processPendingReadCompleteQueue();
            }
        }

        @Override
        protected ChannelHandlerContext parentContext() {
            return Http2MultiplexCodec.this.ctx;
        }

        @Override
        protected ChannelFuture write0(ChannelHandlerContext ctx, Object msg) {
            ChannelPromise promise = ctx.newPromise();
            Http2MultiplexCodec.this.write(ctx, msg, promise);
            return promise;
        }

        @Override
        protected void flush0(ChannelHandlerContext ctx) {
            Http2MultiplexCodec.this.flush0(ctx);
        }
    }
}

