/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.compression;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

public class CompressionEncoder
extends MessageToByteEncoder<ByteBuf> {
    private final Deflater deflater = new Deflater();
    private final int threshold;

    public CompressionEncoder(int threshold) {
        this.threshold = threshold;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        int frameLength = in.readableBytes();
        if (frameLength < this.threshold) {
            out.writeByte(0);
            out.writeBytes(in);
            return;
        }
        Type.VAR_INT.writePrimitive(out, frameLength);
        ByteBuf temp = in;
        if (!in.hasArray()) {
            temp = ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(in);
        } else {
            in.retain();
        }
        ByteBuf output = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            this.deflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
            this.deflater.finish();
            while (!this.deflater.finished()) {
                output.ensureWritable(4096);
                output.writerIndex(output.writerIndex() + this.deflater.deflate(output.array(), output.arrayOffset() + output.writerIndex(), output.writableBytes()));
            }
            out.writeBytes(output);
        } finally {
            output.release();
            temp.release();
            this.deflater.reset();
        }
    }
}

