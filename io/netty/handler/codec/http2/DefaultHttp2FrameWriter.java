/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.DefaultHttp2HeadersEncoder;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameSizePolicy;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersEncoder;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.ReferenceCounted;
import io.netty.util.collection.CharObjectMap;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;

public class DefaultHttp2FrameWriter
implements Http2FrameWriter,
Http2FrameSizePolicy,
Http2FrameWriter.Configuration {
    private static final String STREAM_ID = "Stream ID";
    private static final String STREAM_DEPENDENCY = "Stream Dependency";
    private static final ByteBuf ZERO_BUFFER = Unpooled.unreleasableBuffer(Unpooled.directBuffer(255).writeZero(255)).asReadOnly();
    private final Http2HeadersEncoder headersEncoder;
    private int maxFrameSize;

    public DefaultHttp2FrameWriter() {
        this(new DefaultHttp2HeadersEncoder());
    }

    public DefaultHttp2FrameWriter(Http2HeadersEncoder.SensitivityDetector headersSensitivityDetector) {
        this(new DefaultHttp2HeadersEncoder(headersSensitivityDetector));
    }

    public DefaultHttp2FrameWriter(Http2HeadersEncoder.SensitivityDetector headersSensitivityDetector, boolean ignoreMaxHeaderListSize) {
        this(new DefaultHttp2HeadersEncoder(headersSensitivityDetector, ignoreMaxHeaderListSize));
    }

    public DefaultHttp2FrameWriter(Http2HeadersEncoder headersEncoder) {
        this.headersEncoder = headersEncoder;
        this.maxFrameSize = 16384;
    }

    @Override
    public Http2FrameWriter.Configuration configuration() {
        return this;
    }

    @Override
    public Http2HeadersEncoder.Configuration headersConfiguration() {
        return this.headersEncoder.configuration();
    }

    @Override
    public Http2FrameSizePolicy frameSizePolicy() {
        return this;
    }

    @Override
    public void maxFrameSize(int max) throws Http2Exception {
        if (!Http2CodecUtil.isMaxFrameSizeValid(max)) {
            throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Invalid MAX_FRAME_SIZE specified in sent settings: %d", max);
        }
        this.maxFrameSize = max;
    }

    @Override
    public int maxFrameSize() {
        return this.maxFrameSize;
    }

    @Override
    public void close() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endStream, ChannelPromise promise) {
        Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator = new Http2CodecUtil.SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
        ReferenceCounted frameHeader = null;
        try {
            DefaultHttp2FrameWriter.verifyStreamId(streamId, STREAM_ID);
            Http2CodecUtil.verifyPadding(padding);
            int remainingData = data.readableBytes();
            Http2Flags flags = new Http2Flags();
            flags.endOfStream(false);
            flags.paddingPresent(false);
            if (remainingData > this.maxFrameSize) {
                frameHeader = ctx.alloc().buffer(9);
                Http2CodecUtil.writeFrameHeaderInternal((ByteBuf)frameHeader, this.maxFrameSize, (byte)0, flags, streamId);
                do {
                    ctx.write(frameHeader.retainedSlice(), promiseAggregator.newPromise());
                    ctx.write(data.readRetainedSlice(this.maxFrameSize), promiseAggregator.newPromise());
                } while ((remainingData -= this.maxFrameSize) > this.maxFrameSize);
            }
            if (padding == 0) {
                if (frameHeader != null) {
                    frameHeader.release();
                    frameHeader = null;
                }
                ByteBuf frameHeader2 = ctx.alloc().buffer(9);
                flags.endOfStream(endStream);
                Http2CodecUtil.writeFrameHeaderInternal(frameHeader2, remainingData, (byte)0, flags, streamId);
                ctx.write(frameHeader2, promiseAggregator.newPromise());
                ByteBuf lastFrame = data.readSlice(remainingData);
                data = null;
                ctx.write(lastFrame, promiseAggregator.newPromise());
            } else {
                if (remainingData != this.maxFrameSize) {
                    if (frameHeader != null) {
                        frameHeader.release();
                        frameHeader = null;
                    }
                } else {
                    ByteBuf lastFrame;
                    remainingData -= this.maxFrameSize;
                    if (frameHeader == null) {
                        lastFrame = ctx.alloc().buffer(9);
                        Http2CodecUtil.writeFrameHeaderInternal(lastFrame, this.maxFrameSize, (byte)0, flags, streamId);
                    } else {
                        lastFrame = ((ByteBuf)frameHeader).slice();
                        frameHeader = null;
                    }
                    ctx.write(lastFrame, promiseAggregator.newPromise());
                    lastFrame = data.readableBytes() != this.maxFrameSize ? data.readSlice(this.maxFrameSize) : data;
                    data = null;
                    ctx.write(lastFrame, promiseAggregator.newPromise());
                }
                do {
                    int frameDataBytes = Math.min(remainingData, this.maxFrameSize);
                    int framePaddingBytes = Math.min(padding, Math.max(0, this.maxFrameSize - 1 - frameDataBytes));
                    ByteBuf frameHeader2 = ctx.alloc().buffer(10);
                    flags.endOfStream(endStream && (remainingData -= frameDataBytes) == 0 && (padding -= framePaddingBytes) == 0);
                    flags.paddingPresent(framePaddingBytes > 0);
                    Http2CodecUtil.writeFrameHeaderInternal(frameHeader2, framePaddingBytes + frameDataBytes, (byte)0, flags, streamId);
                    DefaultHttp2FrameWriter.writePaddingLength(frameHeader2, framePaddingBytes);
                    ctx.write(frameHeader2, promiseAggregator.newPromise());
                    if (frameDataBytes != 0 && data != null) {
                        if (remainingData == 0) {
                            ByteBuf lastFrame = data.readSlice(frameDataBytes);
                            data = null;
                            ctx.write(lastFrame, promiseAggregator.newPromise());
                        } else {
                            ctx.write(data.readRetainedSlice(frameDataBytes), promiseAggregator.newPromise());
                        }
                    }
                    if (DefaultHttp2FrameWriter.paddingBytes(framePaddingBytes) <= 0) continue;
                    ctx.write(ZERO_BUFFER.slice(0, DefaultHttp2FrameWriter.paddingBytes(framePaddingBytes)), promiseAggregator.newPromise());
                } while (remainingData != 0 || padding != 0);
            }
        } catch (Throwable cause) {
            if (frameHeader != null) {
                frameHeader.release();
            }
            try {
                if (data != null) {
                    data.release();
                }
            } finally {
                promiseAggregator.setFailure(cause);
                promiseAggregator.doneAllocatingPromises();
            }
            return promiseAggregator;
        }
        return promiseAggregator.doneAllocatingPromises();
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, ChannelPromise promise) {
        return this.writeHeadersInternal(ctx, streamId, headers, padding, endStream, false, 0, (short)0, false, promise);
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream, ChannelPromise promise) {
        return this.writeHeadersInternal(ctx, streamId, headers, padding, endStream, true, streamDependency, weight, exclusive, promise);
    }

    @Override
    public ChannelFuture writePriority(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive, ChannelPromise promise) {
        try {
            DefaultHttp2FrameWriter.verifyStreamId(streamId, STREAM_ID);
            DefaultHttp2FrameWriter.verifyStreamOrConnectionId(streamDependency, STREAM_DEPENDENCY);
            DefaultHttp2FrameWriter.verifyWeight(weight);
            ByteBuf buf = ctx.alloc().buffer(14);
            Http2CodecUtil.writeFrameHeaderInternal(buf, 5, (byte)2, new Http2Flags(), streamId);
            buf.writeInt(exclusive ? (int)(0x80000000L | (long)streamDependency) : streamDependency);
            buf.writeByte(weight - 1);
            return ctx.write(buf, promise);
        } catch (Throwable t) {
            return promise.setFailure(t);
        }
    }

    @Override
    public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
        try {
            DefaultHttp2FrameWriter.verifyStreamId(streamId, STREAM_ID);
            DefaultHttp2FrameWriter.verifyErrorCode(errorCode);
            ByteBuf buf = ctx.alloc().buffer(13);
            Http2CodecUtil.writeFrameHeaderInternal(buf, 4, (byte)3, new Http2Flags(), streamId);
            buf.writeInt((int)errorCode);
            return ctx.write(buf, promise);
        } catch (Throwable t) {
            return promise.setFailure(t);
        }
    }

    @Override
    public ChannelFuture writeSettings(ChannelHandlerContext ctx, Http2Settings settings, ChannelPromise promise) {
        try {
            ObjectUtil.checkNotNull(settings, "settings");
            int payloadLength = 6 * settings.size();
            ByteBuf buf = ctx.alloc().buffer(9 + payloadLength);
            Http2CodecUtil.writeFrameHeaderInternal(buf, payloadLength, (byte)4, new Http2Flags(), 0);
            for (CharObjectMap.PrimitiveEntry entry : settings.entries()) {
                buf.writeChar(entry.key());
                buf.writeInt(((Long)entry.value()).intValue());
            }
            return ctx.write(buf, promise);
        } catch (Throwable t) {
            return promise.setFailure(t);
        }
    }

    @Override
    public ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise) {
        try {
            ByteBuf buf = ctx.alloc().buffer(9);
            Http2CodecUtil.writeFrameHeaderInternal(buf, 0, (byte)4, new Http2Flags().ack(true), 0);
            return ctx.write(buf, promise);
        } catch (Throwable t) {
            return promise.setFailure(t);
        }
    }

    @Override
    public ChannelFuture writePing(ChannelHandlerContext ctx, boolean ack, long data, ChannelPromise promise) {
        Http2Flags flags = ack ? new Http2Flags().ack(true) : new Http2Flags();
        ByteBuf buf = ctx.alloc().buffer(17);
        Http2CodecUtil.writeFrameHeaderInternal(buf, 8, (byte)6, flags, 0);
        buf.writeLong(data);
        return ctx.write(buf, promise);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ChannelFuture writePushPromise(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding, ChannelPromise promise) {
        ReferenceCounted headerBlock = null;
        Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator = new Http2CodecUtil.SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
        try {
            DefaultHttp2FrameWriter.verifyStreamId(streamId, STREAM_ID);
            DefaultHttp2FrameWriter.verifyStreamId(promisedStreamId, "Promised Stream ID");
            Http2CodecUtil.verifyPadding(padding);
            headerBlock = ctx.alloc().buffer();
            this.headersEncoder.encodeHeaders(streamId, headers, (ByteBuf)headerBlock);
            Http2Flags flags = new Http2Flags().paddingPresent(padding > 0);
            int nonFragmentLength = 4 + padding;
            int maxFragmentLength = this.maxFrameSize - nonFragmentLength;
            ByteBuf fragment = headerBlock.readRetainedSlice(Math.min(((ByteBuf)headerBlock).readableBytes(), maxFragmentLength));
            flags.endOfHeaders(!((ByteBuf)headerBlock).isReadable());
            int payloadLength = fragment.readableBytes() + nonFragmentLength;
            ByteBuf buf = ctx.alloc().buffer(14);
            Http2CodecUtil.writeFrameHeaderInternal(buf, payloadLength, (byte)5, flags, streamId);
            DefaultHttp2FrameWriter.writePaddingLength(buf, padding);
            buf.writeInt(promisedStreamId);
            ctx.write(buf, promiseAggregator.newPromise());
            ctx.write(fragment, promiseAggregator.newPromise());
            if (DefaultHttp2FrameWriter.paddingBytes(padding) > 0) {
                ctx.write(ZERO_BUFFER.slice(0, DefaultHttp2FrameWriter.paddingBytes(padding)), promiseAggregator.newPromise());
            }
            if (!flags.endOfHeaders()) {
                this.writeContinuationFrames(ctx, streamId, (ByteBuf)headerBlock, promiseAggregator);
            }
        } catch (Http2Exception e) {
            promiseAggregator.setFailure(e);
        } catch (Throwable t) {
            promiseAggregator.setFailure(t);
            promiseAggregator.doneAllocatingPromises();
            PlatformDependent.throwException(t);
        } finally {
            if (headerBlock != null) {
                headerBlock.release();
            }
        }
        return promiseAggregator.doneAllocatingPromises();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ChannelFuture writeGoAway(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData, ChannelPromise promise) {
        Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator = new Http2CodecUtil.SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
        try {
            DefaultHttp2FrameWriter.verifyStreamOrConnectionId(lastStreamId, "Last Stream ID");
            DefaultHttp2FrameWriter.verifyErrorCode(errorCode);
            int payloadLength = 8 + debugData.readableBytes();
            ByteBuf buf = ctx.alloc().buffer(17);
            Http2CodecUtil.writeFrameHeaderInternal(buf, payloadLength, (byte)7, new Http2Flags(), 0);
            buf.writeInt(lastStreamId);
            buf.writeInt((int)errorCode);
            ctx.write(buf, promiseAggregator.newPromise());
        } catch (Throwable t) {
            try {
                debugData.release();
            } finally {
                promiseAggregator.setFailure(t);
                promiseAggregator.doneAllocatingPromises();
            }
            return promiseAggregator;
        }
        try {
            ctx.write(debugData, promiseAggregator.newPromise());
        } catch (Throwable t) {
            promiseAggregator.setFailure(t);
        }
        return promiseAggregator.doneAllocatingPromises();
    }

    @Override
    public ChannelFuture writeWindowUpdate(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement, ChannelPromise promise) {
        try {
            DefaultHttp2FrameWriter.verifyStreamOrConnectionId(streamId, STREAM_ID);
            DefaultHttp2FrameWriter.verifyWindowSizeIncrement(windowSizeIncrement);
            ByteBuf buf = ctx.alloc().buffer(13);
            Http2CodecUtil.writeFrameHeaderInternal(buf, 4, (byte)8, new Http2Flags(), streamId);
            buf.writeInt(windowSizeIncrement);
            return ctx.write(buf, promise);
        } catch (Throwable t) {
            return promise.setFailure(t);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ChannelFuture writeFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload, ChannelPromise promise) {
        Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator = new Http2CodecUtil.SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
        try {
            DefaultHttp2FrameWriter.verifyStreamOrConnectionId(streamId, STREAM_ID);
            ByteBuf buf = ctx.alloc().buffer(9);
            Http2CodecUtil.writeFrameHeaderInternal(buf, payload.readableBytes(), frameType, flags, streamId);
            ctx.write(buf, promiseAggregator.newPromise());
        } catch (Throwable t) {
            try {
                payload.release();
            } finally {
                promiseAggregator.setFailure(t);
                promiseAggregator.doneAllocatingPromises();
            }
            return promiseAggregator;
        }
        try {
            ctx.write(payload, promiseAggregator.newPromise());
        } catch (Throwable t) {
            promiseAggregator.setFailure(t);
        }
        return promiseAggregator.doneAllocatingPromises();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ChannelFuture writeHeadersInternal(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, boolean hasPriority, int streamDependency, short weight, boolean exclusive, ChannelPromise promise) {
        ReferenceCounted headerBlock = null;
        Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator = new Http2CodecUtil.SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
        try {
            DefaultHttp2FrameWriter.verifyStreamId(streamId, STREAM_ID);
            if (hasPriority) {
                DefaultHttp2FrameWriter.verifyStreamOrConnectionId(streamDependency, STREAM_DEPENDENCY);
                Http2CodecUtil.verifyPadding(padding);
                DefaultHttp2FrameWriter.verifyWeight(weight);
            }
            headerBlock = ctx.alloc().buffer();
            this.headersEncoder.encodeHeaders(streamId, headers, (ByteBuf)headerBlock);
            Http2Flags flags = new Http2Flags().endOfStream(endStream).priorityPresent(hasPriority).paddingPresent(padding > 0);
            int nonFragmentBytes = padding + flags.getNumPriorityBytes();
            int maxFragmentLength = this.maxFrameSize - nonFragmentBytes;
            ByteBuf fragment = headerBlock.readRetainedSlice(Math.min(((ByteBuf)headerBlock).readableBytes(), maxFragmentLength));
            flags.endOfHeaders(!((ByteBuf)headerBlock).isReadable());
            int payloadLength = fragment.readableBytes() + nonFragmentBytes;
            ByteBuf buf = ctx.alloc().buffer(15);
            Http2CodecUtil.writeFrameHeaderInternal(buf, payloadLength, (byte)1, flags, streamId);
            DefaultHttp2FrameWriter.writePaddingLength(buf, padding);
            if (hasPriority) {
                buf.writeInt(exclusive ? (int)(0x80000000L | (long)streamDependency) : streamDependency);
                buf.writeByte(weight - 1);
            }
            ctx.write(buf, promiseAggregator.newPromise());
            ctx.write(fragment, promiseAggregator.newPromise());
            if (DefaultHttp2FrameWriter.paddingBytes(padding) > 0) {
                ctx.write(ZERO_BUFFER.slice(0, DefaultHttp2FrameWriter.paddingBytes(padding)), promiseAggregator.newPromise());
            }
            if (!flags.endOfHeaders()) {
                this.writeContinuationFrames(ctx, streamId, (ByteBuf)headerBlock, promiseAggregator);
            }
        } catch (Http2Exception e) {
            promiseAggregator.setFailure(e);
        } catch (Throwable t) {
            promiseAggregator.setFailure(t);
            promiseAggregator.doneAllocatingPromises();
            PlatformDependent.throwException(t);
        } finally {
            if (headerBlock != null) {
                headerBlock.release();
            }
        }
        return promiseAggregator.doneAllocatingPromises();
    }

    private ChannelFuture writeContinuationFrames(ChannelHandlerContext ctx, int streamId, ByteBuf headerBlock, Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator) {
        Http2Flags flags = new Http2Flags();
        if (headerBlock.isReadable()) {
            int fragmentReadableBytes = Math.min(headerBlock.readableBytes(), this.maxFrameSize);
            ByteBuf buf = ctx.alloc().buffer(10);
            Http2CodecUtil.writeFrameHeaderInternal(buf, fragmentReadableBytes, (byte)9, flags, streamId);
            do {
                fragmentReadableBytes = Math.min(headerBlock.readableBytes(), this.maxFrameSize);
                ByteBuf fragment = headerBlock.readRetainedSlice(fragmentReadableBytes);
                if (headerBlock.isReadable()) {
                    ctx.write(buf.retain(), promiseAggregator.newPromise());
                } else {
                    flags = flags.endOfHeaders(true);
                    buf.release();
                    buf = ctx.alloc().buffer(10);
                    Http2CodecUtil.writeFrameHeaderInternal(buf, fragmentReadableBytes, (byte)9, flags, streamId);
                    ctx.write(buf, promiseAggregator.newPromise());
                }
                ctx.write(fragment, promiseAggregator.newPromise());
            } while (headerBlock.isReadable());
        }
        return promiseAggregator;
    }

    private static int paddingBytes(int padding) {
        return padding - 1;
    }

    private static void writePaddingLength(ByteBuf buf, int padding) {
        if (padding > 0) {
            buf.writeByte(padding - 1);
        }
    }

    private static void verifyStreamId(int streamId, String argumentName) {
        ObjectUtil.checkPositive(streamId, argumentName);
    }

    private static void verifyStreamOrConnectionId(int streamId, String argumentName) {
        ObjectUtil.checkPositiveOrZero(streamId, argumentName);
    }

    private static void verifyWeight(short weight) {
        if (weight < 1 || weight > 256) {
            throw new IllegalArgumentException("Invalid weight: " + weight);
        }
    }

    private static void verifyErrorCode(long errorCode) {
        if (errorCode < 0L || errorCode > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Invalid errorCode: " + errorCode);
        }
    }

    private static void verifyWindowSizeIncrement(int windowSizeIncrement) {
        ObjectUtil.checkPositiveOrZero(windowSizeIncrement, "windowSizeIncrement");
    }

    private static void verifyPingPayload(ByteBuf data) {
        if (data == null || data.readableBytes() != 8) {
            throw new IllegalArgumentException("Opaque data must be 8 bytes");
        }
    }
}

