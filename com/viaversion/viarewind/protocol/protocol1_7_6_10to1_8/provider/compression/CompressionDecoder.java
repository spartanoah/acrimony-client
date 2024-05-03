/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.provider.compression;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import java.util.zip.Inflater;

public class CompressionDecoder
extends MessageToMessageDecoder<ByteBuf> {
    private final Inflater inflater = new Inflater();
    private final int threshold;

    public CompressionDecoder(int threshold) {
        this.threshold = threshold;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }
        int outLength = Type.VAR_INT.readPrimitive(in);
        if (outLength == 0) {
            out.add(in.readBytes(in.readableBytes()));
            return;
        }
        if (outLength < this.threshold) {
            throw new DecoderException("Badly compressed packet - size of " + outLength + " is below server threshold of " + this.threshold);
        }
        if (outLength > 0x200000) {
            throw new DecoderException("Badly compressed packet - size of " + outLength + " is larger than protocol maximum of " + 0x200000);
        }
        ByteBuf temp = in;
        if (!in.hasArray()) {
            temp = ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(in);
        } else {
            in.retain();
        }
        ByteBuf output = ByteBufAllocator.DEFAULT.heapBuffer(outLength, outLength);
        try {
            this.inflater.setInput(temp.array(), temp.arrayOffset() + temp.readerIndex(), temp.readableBytes());
            output.writerIndex(output.writerIndex() + this.inflater.inflate(output.array(), output.arrayOffset(), outLength));
            out.add(output.retain());
        } finally {
            output.release();
            temp.release();
            this.inflater.reset();
        }
    }
}

