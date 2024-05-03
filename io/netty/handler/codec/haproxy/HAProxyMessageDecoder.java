/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ProtocolDetectionResult;
import io.netty.handler.codec.haproxy.HAProxyConstants;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyProtocolException;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.util.CharsetUtil;
import java.util.List;

public class HAProxyMessageDecoder
extends ByteToMessageDecoder {
    private static final int V1_MAX_LENGTH = 108;
    private static final int V2_MAX_LENGTH = 65551;
    private static final int V2_MIN_LENGTH = 232;
    private static final int V2_MAX_TLV = 65319;
    private static final int BINARY_PREFIX_LENGTH = HAProxyConstants.BINARY_PREFIX.length;
    private static final ProtocolDetectionResult<HAProxyProtocolVersion> DETECTION_RESULT_V1 = ProtocolDetectionResult.detected(HAProxyProtocolVersion.V1);
    private static final ProtocolDetectionResult<HAProxyProtocolVersion> DETECTION_RESULT_V2 = ProtocolDetectionResult.detected(HAProxyProtocolVersion.V2);
    private HeaderExtractor headerExtractor;
    private boolean discarding;
    private int discardedBytes;
    private final boolean failFast;
    private boolean finished;
    private int version = -1;
    private final int v2MaxHeaderSize;

    public HAProxyMessageDecoder() {
        this(true);
    }

    public HAProxyMessageDecoder(boolean failFast) {
        this.v2MaxHeaderSize = 65551;
        this.failFast = failFast;
    }

    public HAProxyMessageDecoder(int maxTlvSize) {
        this(maxTlvSize, true);
    }

    public HAProxyMessageDecoder(int maxTlvSize, boolean failFast) {
        int calcMax;
        this.v2MaxHeaderSize = maxTlvSize < 1 ? 232 : (maxTlvSize > 65319 ? 65551 : ((calcMax = maxTlvSize + 232) > 65551 ? 65551 : calcMax));
        this.failFast = failFast;
    }

    private static int findVersion(ByteBuf buffer) {
        int n = buffer.readableBytes();
        if (n < 13) {
            return -1;
        }
        int idx = buffer.readerIndex();
        return HAProxyMessageDecoder.match(HAProxyConstants.BINARY_PREFIX, buffer, idx) ? (int)buffer.getByte(idx + BINARY_PREFIX_LENGTH) : 1;
    }

    private static int findEndOfHeader(ByteBuf buffer) {
        int n = buffer.readableBytes();
        if (n < 16) {
            return -1;
        }
        int offset = buffer.readerIndex() + 14;
        int totalHeaderBytes = 16 + buffer.getUnsignedShort(offset);
        if (n >= totalHeaderBytes) {
            return totalHeaderBytes;
        }
        return -1;
    }

    private static int findEndOfLine(ByteBuf buffer) {
        int n = buffer.writerIndex();
        for (int i = buffer.readerIndex(); i < n; ++i) {
            byte b = buffer.getByte(i);
            if (b != 13 || i >= n - 1 || buffer.getByte(i + 1) != 10) continue;
            return i;
        }
        return -1;
    }

    @Override
    public boolean isSingleDecode() {
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (this.finished) {
            ctx.pipeline().remove(this);
        }
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (this.version == -1 && (this.version = HAProxyMessageDecoder.findVersion(in)) == -1) {
            return;
        }
        ByteBuf decoded = this.version == 1 ? this.decodeLine(ctx, in) : this.decodeStruct(ctx, in);
        if (decoded != null) {
            this.finished = true;
            try {
                if (this.version == 1) {
                    out.add(HAProxyMessage.decodeHeader(decoded.toString(CharsetUtil.US_ASCII)));
                } else {
                    out.add(HAProxyMessage.decodeHeader(decoded));
                }
            } catch (HAProxyProtocolException e) {
                this.fail(ctx, null, e);
            }
        }
    }

    private ByteBuf decodeStruct(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        if (this.headerExtractor == null) {
            this.headerExtractor = new StructHeaderExtractor(this.v2MaxHeaderSize);
        }
        return this.headerExtractor.extract(ctx, buffer);
    }

    private ByteBuf decodeLine(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        if (this.headerExtractor == null) {
            this.headerExtractor = new LineHeaderExtractor(108);
        }
        return this.headerExtractor.extract(ctx, buffer);
    }

    private void failOverLimit(ChannelHandlerContext ctx, int length) {
        this.failOverLimit(ctx, String.valueOf(length));
    }

    private void failOverLimit(ChannelHandlerContext ctx, String length) {
        int maxLength = this.version == 1 ? 108 : this.v2MaxHeaderSize;
        this.fail(ctx, "header length (" + length + ") exceeds the allowed maximum (" + maxLength + ')', null);
    }

    private void fail(ChannelHandlerContext ctx, String errMsg, Exception e) {
        this.finished = true;
        ctx.close();
        HAProxyProtocolException ppex = errMsg != null && e != null ? new HAProxyProtocolException(errMsg, e) : (errMsg != null ? new HAProxyProtocolException(errMsg) : (e != null ? new HAProxyProtocolException(e) : new HAProxyProtocolException()));
        throw ppex;
    }

    public static ProtocolDetectionResult<HAProxyProtocolVersion> detectProtocol(ByteBuf buffer) {
        if (buffer.readableBytes() < 12) {
            return ProtocolDetectionResult.needsMoreData();
        }
        int idx = buffer.readerIndex();
        if (HAProxyMessageDecoder.match(HAProxyConstants.BINARY_PREFIX, buffer, idx)) {
            return DETECTION_RESULT_V2;
        }
        if (HAProxyMessageDecoder.match(HAProxyConstants.TEXT_PREFIX, buffer, idx)) {
            return DETECTION_RESULT_V1;
        }
        return ProtocolDetectionResult.invalid();
    }

    private static boolean match(byte[] prefix, ByteBuf buffer, int idx) {
        for (int i = 0; i < prefix.length; ++i) {
            byte b = buffer.getByte(idx + i);
            if (b == prefix[i]) continue;
            return false;
        }
        return true;
    }

    private final class StructHeaderExtractor
    extends HeaderExtractor {
        StructHeaderExtractor(int maxHeaderSize) {
            super(maxHeaderSize);
        }

        @Override
        protected int findEndOfHeader(ByteBuf buffer) {
            return HAProxyMessageDecoder.findEndOfHeader(buffer);
        }

        @Override
        protected int delimiterLength(ByteBuf buffer, int eoh) {
            return 0;
        }
    }

    private final class LineHeaderExtractor
    extends HeaderExtractor {
        LineHeaderExtractor(int maxHeaderSize) {
            super(maxHeaderSize);
        }

        @Override
        protected int findEndOfHeader(ByteBuf buffer) {
            return HAProxyMessageDecoder.findEndOfLine(buffer);
        }

        @Override
        protected int delimiterLength(ByteBuf buffer, int eoh) {
            return buffer.getByte(eoh) == 13 ? 2 : 1;
        }
    }

    private abstract class HeaderExtractor {
        private final int maxHeaderSize;

        protected HeaderExtractor(int maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
        }

        public ByteBuf extract(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
            int eoh = this.findEndOfHeader(buffer);
            if (!HAProxyMessageDecoder.this.discarding) {
                if (eoh >= 0) {
                    int length = eoh - buffer.readerIndex();
                    if (length > this.maxHeaderSize) {
                        buffer.readerIndex(eoh + this.delimiterLength(buffer, eoh));
                        HAProxyMessageDecoder.this.failOverLimit(ctx, length);
                        return null;
                    }
                    ByteBuf frame = buffer.readSlice(length);
                    buffer.skipBytes(this.delimiterLength(buffer, eoh));
                    return frame;
                }
                int length = buffer.readableBytes();
                if (length > this.maxHeaderSize) {
                    HAProxyMessageDecoder.this.discardedBytes = length;
                    buffer.skipBytes(length);
                    HAProxyMessageDecoder.this.discarding = true;
                    if (HAProxyMessageDecoder.this.failFast) {
                        HAProxyMessageDecoder.this.failOverLimit(ctx, "over " + HAProxyMessageDecoder.this.discardedBytes);
                    }
                }
                return null;
            }
            if (eoh >= 0) {
                int length = HAProxyMessageDecoder.this.discardedBytes + eoh - buffer.readerIndex();
                buffer.readerIndex(eoh + this.delimiterLength(buffer, eoh));
                HAProxyMessageDecoder.this.discardedBytes = 0;
                HAProxyMessageDecoder.this.discarding = false;
                if (!HAProxyMessageDecoder.this.failFast) {
                    HAProxyMessageDecoder.this.failOverLimit(ctx, "over " + length);
                }
            } else {
                HAProxyMessageDecoder.this.discardedBytes = HAProxyMessageDecoder.this.discardedBytes + buffer.readableBytes();
                buffer.skipBytes(buffer.readableBytes());
            }
            return null;
        }

        protected abstract int findEndOfHeader(ByteBuf var1);

        protected abstract int delimiterLength(ByteBuf var1, int var2);
    }
}

