/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Environment;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.util.ChunkUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Level;

public class ChunkType1_9_1
extends Type<Chunk> {
    private static final ChunkType1_9_1 WITH_SKYLIGHT = new ChunkType1_9_1(true);
    private static final ChunkType1_9_1 WITHOUT_SKYLIGHT = new ChunkType1_9_1(false);
    private final boolean hasSkyLight;

    public ChunkType1_9_1(boolean hasSkyLight) {
        super(Chunk.class);
        this.hasSkyLight = hasSkyLight;
    }

    public static ChunkType1_9_1 forEnvironment(Environment environment) {
        return environment == Environment.NORMAL ? WITH_SKYLIGHT : WITHOUT_SKYLIGHT;
    }

    @Override
    public Chunk read(ByteBuf input) throws Exception {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        boolean groundUp = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.readPrimitive(input);
        ByteBuf data = input.readSlice(Type.VAR_INT.readPrimitive(input));
        ChunkSection[] sections = new ChunkSection[16];
        int[] biomeData = groundUp ? new int[256] : null;
        try {
            int i;
            BitSet usedSections = new BitSet(16);
            for (i = 0; i < 16; ++i) {
                if ((primaryBitmask & 1 << i) == 0) continue;
                usedSections.set(i);
            }
            for (i = 0; i < 16; ++i) {
                ChunkSection section;
                if (!usedSections.get(i)) continue;
                sections[i] = section = (ChunkSection)Types1_9.CHUNK_SECTION.read(data);
                section.getLight().readBlockLight(data);
                if (!this.hasSkyLight) continue;
                section.getLight().readSkyLight(data);
            }
            if (groundUp) {
                for (i = 0; i < 256; ++i) {
                    biomeData[i] = data.readByte() & 0xFF;
                }
            }
        } catch (Throwable e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "The server sent an invalid chunk data packet, returning an empty chunk instead", e);
            return ChunkUtil.createEmptyChunk(chunkX, chunkZ);
        }
        return new BaseChunk(chunkX, chunkZ, groundUp, false, primaryBitmask, sections, biomeData, new ArrayList<CompoundTag>());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(ByteBuf output, Chunk chunk) throws Exception {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());
        output.writeBoolean(chunk.isFullChunk());
        Type.VAR_INT.writePrimitive(output, chunk.getBitmask());
        ByteBuf buf = output.alloc().buffer();
        try {
            for (int i = 0; i < 16; ++i) {
                ChunkSection section = chunk.getSections()[i];
                if (section == null) continue;
                Types1_9.CHUNK_SECTION.write(buf, section);
                section.getLight().writeBlockLight(buf);
                if (!section.getLight().hasSkyLight()) continue;
                section.getLight().writeSkyLight(buf);
            }
            buf.readerIndex(0);
            Type.VAR_INT.writePrimitive(output, buf.readableBytes() + (chunk.isBiomeData() ? 256 : 0));
            output.writeBytes(buf);
        } finally {
            buf.release();
        }
        if (chunk.isBiomeData()) {
            for (int biome : chunk.getBiomeData()) {
                output.writeByte((byte)biome);
            }
        }
    }
}

