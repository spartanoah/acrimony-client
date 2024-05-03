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
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class ChunkType1_13
extends Type<Chunk> {
    private static final ChunkType1_13 WITH_SKYLIGHT = new ChunkType1_13(true);
    private static final ChunkType1_13 WITHOUT_SKYLIGHT = new ChunkType1_13(false);
    private final boolean hasSkyLight;

    public ChunkType1_13(boolean hasSkyLight) {
        super(Chunk.class);
        this.hasSkyLight = hasSkyLight;
    }

    public static ChunkType1_13 forEnvironment(Environment environment) {
        return environment == Environment.NORMAL ? WITH_SKYLIGHT : WITHOUT_SKYLIGHT;
    }

    @Override
    public Chunk read(ByteBuf input) throws Exception {
        int[] biomeData;
        int chunkX = input.readInt();
        int chunkZ = input.readInt();
        boolean fullChunk = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.readPrimitive(input);
        ByteBuf data = input.readSlice(Type.VAR_INT.readPrimitive(input));
        ChunkSection[] sections = new ChunkSection[16];
        for (int i = 0; i < 16; ++i) {
            ChunkSection section;
            if ((primaryBitmask & 1 << i) == 0) continue;
            sections[i] = section = (ChunkSection)Types1_13.CHUNK_SECTION.read(data);
            section.getLight().readBlockLight(data);
            if (!this.hasSkyLight) continue;
            section.getLight().readSkyLight(data);
        }
        int[] nArray = biomeData = fullChunk ? new int[256] : null;
        if (fullChunk) {
            if (data.readableBytes() >= 1024) {
                for (int i = 0; i < 256; ++i) {
                    biomeData[i] = data.readInt();
                }
            } else {
                Via.getPlatform().getLogger().log(Level.WARNING, "Chunk x=" + chunkX + " z=" + chunkZ + " doesn't have biome data!");
            }
        }
        ArrayList<Object> nbtData = new ArrayList<Object>(Arrays.asList((Object[])Type.NAMED_COMPOUND_TAG_ARRAY.read(input)));
        return new BaseChunk(chunkX, chunkZ, fullChunk, false, primaryBitmask, sections, biomeData, nbtData);
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
                Types1_13.CHUNK_SECTION.write(buf, section);
                section.getLight().writeBlockLight(buf);
                if (!section.getLight().hasSkyLight()) continue;
                section.getLight().writeSkyLight(buf);
            }
            buf.readerIndex(0);
            Type.VAR_INT.writePrimitive(output, buf.readableBytes() + (chunk.isBiomeData() ? 1024 : 0));
            output.writeBytes(buf);
        } finally {
            buf.release();
        }
        if (chunk.isBiomeData()) {
            for (int value : chunk.getBiomeData()) {
                output.writeInt(value);
            }
        }
        Type.NAMED_COMPOUND_TAG_ARRAY.write(output, chunk.getBlockEntities().toArray(new CompoundTag[0]));
    }
}

