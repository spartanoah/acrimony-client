/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.chunk;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.chunk.ChunkType1_7_6;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Pair;
import io.netty.buffer.ByteBuf;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

public class BulkChunkType1_7_6
extends Type<Chunk[]> {
    public static final BulkChunkType1_7_6 TYPE = new BulkChunkType1_7_6();

    public BulkChunkType1_7_6() {
        super(Chunk[].class);
    }

    @Override
    public Chunk[] read(ByteBuf byteBuf) throws Exception {
        throw new UnsupportedOperationException();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(ByteBuf byteBuf, Chunk[] chunks) throws Exception {
        int compressedSize;
        byte[] compressedData;
        int chunkCount = chunks.length;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int[] chunkX = new int[chunkCount];
        int[] chunkZ = new int[chunkCount];
        short[] primaryBitMask = new short[chunkCount];
        short[] additionalBitMask = new short[chunkCount];
        for (int i = 0; i < chunkCount; ++i) {
            Chunk chunk = chunks[i];
            Pair<byte[], Short> chunkData = ChunkType1_7_6.serialize(chunk);
            output.write(chunkData.key());
            chunkX[i] = chunk.getX();
            chunkZ[i] = chunk.getZ();
            primaryBitMask[i] = (short)chunk.getBitmask();
            additionalBitMask[i] = chunkData.value();
        }
        byte[] data = output.toByteArray();
        Deflater deflater = new Deflater();
        try {
            deflater.setInput(data, 0, data.length);
            deflater.finish();
            compressedData = new byte[data.length];
            compressedSize = deflater.deflate(compressedData);
        } finally {
            deflater.end();
        }
        byteBuf.writeShort(chunkCount);
        byteBuf.writeInt(compressedSize);
        boolean skyLight = false;
        block4: for (Chunk chunk : chunks) {
            for (ChunkSection section : chunk.getSections()) {
                if (section == null || !section.getLight().hasSkyLight()) continue;
                skyLight = true;
                continue block4;
            }
        }
        byteBuf.writeBoolean(skyLight);
        byteBuf.writeBytes(compressedData, 0, compressedSize);
        for (int i = 0; i < chunkCount; ++i) {
            byteBuf.writeInt(chunkX[i]);
            byteBuf.writeInt(chunkZ[i]);
            byteBuf.writeShort(primaryBitMask[i]);
            byteBuf.writeShort(additionalBitMask[i]);
        }
    }
}

