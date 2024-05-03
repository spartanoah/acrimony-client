/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_8;
import io.netty.buffer.ByteBuf;

public class BulkChunkType1_8
extends Type<Chunk[]> {
    public static final Type<Chunk[]> TYPE = new BulkChunkType1_8();
    private static final int BLOCKS_PER_SECTION = 4096;
    private static final int BLOCKS_BYTES = 8192;
    private static final int LIGHT_BYTES = 2048;
    private static final int BIOME_BYTES = 256;

    public BulkChunkType1_8() {
        super(Chunk[].class);
    }

    @Override
    public Chunk[] read(ByteBuf input) throws Exception {
        int i;
        boolean skyLight = input.readBoolean();
        int count = Type.VAR_INT.readPrimitive(input);
        Chunk[] chunks = new Chunk[count];
        ChunkBulkSection[] chunkInfo = new ChunkBulkSection[count];
        for (i = 0; i < chunkInfo.length; ++i) {
            chunkInfo[i] = new ChunkBulkSection(input, skyLight);
        }
        for (i = 0; i < chunks.length; ++i) {
            ChunkBulkSection chunkBulkSection = chunkInfo[i];
            chunkBulkSection.readData(input);
            chunks[i] = ChunkType1_8.deserialize(chunkBulkSection.chunkX, chunkBulkSection.chunkZ, true, skyLight, chunkBulkSection.bitmask, chunkBulkSection.data());
        }
        return chunks;
    }

    @Override
    public void write(ByteBuf output, Chunk[] chunks) throws Exception {
        boolean skyLight = false;
        block0: for (Chunk chunk : chunks) {
            for (ChunkSection section : chunk.getSections()) {
                if (section == null || !section.getLight().hasSkyLight()) continue;
                skyLight = true;
                break block0;
            }
        }
        output.writeBoolean(skyLight);
        Type.VAR_INT.writePrimitive(output, chunks.length);
        for (Chunk chunk : chunks) {
            output.writeInt(chunk.getX());
            output.writeInt(chunk.getZ());
            output.writeShort(chunk.getBitmask());
        }
        for (Chunk chunk : chunks) {
            output.writeBytes(ChunkType1_8.serialize(chunk));
        }
    }

    public static final class ChunkBulkSection {
        private final int chunkX;
        private final int chunkZ;
        private final int bitmask;
        private final byte[] data;

        public ChunkBulkSection(ByteBuf input, boolean skyLight) {
            this.chunkX = input.readInt();
            this.chunkZ = input.readInt();
            this.bitmask = input.readUnsignedShort();
            int setSections = Integer.bitCount(this.bitmask);
            this.data = new byte[setSections * (8192 + (skyLight ? 4096 : 2048)) + 256];
        }

        public void readData(ByteBuf input) {
            input.readBytes(this.data);
        }

        public int chunkX() {
            return this.chunkX;
        }

        public int chunkZ() {
            return this.chunkZ;
        }

        public int bitmask() {
            return this.bitmask;
        }

        public byte[] data() {
            return this.data;
        }
    }
}

