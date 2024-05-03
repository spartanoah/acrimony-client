/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.github.luben.zstd.Zstd
 */
package io.netty.handler.codec.compression;

import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.compression.CompressionException;
import io.netty.util.internal.ObjectUtil;
import java.nio.ByteBuffer;

public final class ZstdEncoder
extends MessageToByteEncoder<ByteBuf> {
    private final int blockSize;
    private final int compressionLevel;
    private final int maxEncodeSize;
    private ByteBuf buffer;

    public ZstdEncoder() {
        this(3, 65536, 0x2000000);
    }

    public ZstdEncoder(int compressionLevel) {
        this(compressionLevel, 65536, 0x2000000);
    }

    public ZstdEncoder(int blockSize, int maxEncodeSize) {
        this(3, blockSize, maxEncodeSize);
    }

    public ZstdEncoder(int compressionLevel, int blockSize, int maxEncodeSize) {
        super(true);
        this.compressionLevel = ObjectUtil.checkInRange(compressionLevel, 0, 22, "compressionLevel");
        this.blockSize = ObjectUtil.checkPositive(blockSize, "blockSize");
        this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        if (this.buffer == null) {
            throw new IllegalStateException("not added to a pipeline,or has been removed,buffer is null");
        }
        int remaining = msg.readableBytes() + this.buffer.readableBytes();
        if (remaining < 0) {
            throw new EncoderException("too much data to allocate a buffer for compression");
        }
        long bufferSize = 0L;
        while (remaining > 0) {
            int curSize = Math.min(this.blockSize, remaining);
            remaining -= curSize;
            bufferSize += Zstd.compressBound((long)curSize);
        }
        if (bufferSize > (long)this.maxEncodeSize || 0L > bufferSize) {
            throw new EncoderException("requested encode buffer size (" + bufferSize + " bytes) exceeds the maximum allowable size (" + this.maxEncodeSize + " bytes)");
        }
        return ctx.alloc().directBuffer((int)bufferSize);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        int length;
        if (this.buffer == null) {
            throw new IllegalStateException("not added to a pipeline,or has been removed,buffer is null");
        }
        ByteBuf buffer = this.buffer;
        while ((length = in.readableBytes()) > 0) {
            int nextChunkSize = Math.min(length, buffer.writableBytes());
            in.readBytes(buffer, nextChunkSize);
            if (buffer.isWritable()) continue;
            this.flushBufferedData(out);
        }
    }

    private void flushBufferedData(ByteBuf out) {
        int compressedLength;
        int flushableBytes = this.buffer.readableBytes();
        if (flushableBytes == 0) {
            return;
        }
        int bufSize = (int)Zstd.compressBound((long)flushableBytes);
        out.ensureWritable(bufSize);
        int idx = out.writerIndex();
        try {
            ByteBuffer outNioBuffer = out.internalNioBuffer(idx, out.writableBytes());
            compressedLength = Zstd.compress((ByteBuffer)outNioBuffer, (ByteBuffer)this.buffer.internalNioBuffer(this.buffer.readerIndex(), flushableBytes), (int)this.compressionLevel);
        } catch (Exception e) {
            throw new CompressionException(e);
        }
        out.writerIndex(idx + compressedLength);
        this.buffer.clear();
    }

    @Override
    public void flush(ChannelHandlerContext ctx) {
        if (this.buffer != null && this.buffer.isReadable()) {
            ByteBuf buf = this.allocateBuffer(ctx, Unpooled.EMPTY_BUFFER, this.isPreferDirect());
            this.flushBufferedData(buf);
            ctx.write(buf);
        }
        ctx.flush();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.buffer = ctx.alloc().directBuffer(this.blockSize);
        this.buffer.clear();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        if (this.buffer != null) {
            this.buffer.release();
            this.buffer = null;
        }
    }
}

