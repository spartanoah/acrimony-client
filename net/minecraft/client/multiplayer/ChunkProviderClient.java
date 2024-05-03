/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderClient
implements IChunkProvider {
    private static final Logger logger = LogManager.getLogger();
    private Chunk blankChunk;
    private LongHashMap<Chunk> chunkMapping = new LongHashMap();
    private List<Chunk> chunkListing = Lists.newArrayList();
    private World worldObj;

    public ChunkProviderClient(World worldIn) {
        this.blankChunk = new EmptyChunk(worldIn, 0, 0);
        this.worldObj = worldIn;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

    public void unloadChunk(int p_73234_1_, int p_73234_2_) {
        Chunk chunk = this.provideChunk(p_73234_1_, p_73234_2_);
        if (!chunk.isEmpty()) {
            chunk.onChunkUnload();
        }
        this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(p_73234_1_, p_73234_2_));
        this.chunkListing.remove(chunk);
    }

    public Chunk loadChunk(int p_73158_1_, int p_73158_2_) {
        Chunk chunk = new Chunk(this.worldObj, p_73158_1_, p_73158_2_);
        this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(p_73158_1_, p_73158_2_), chunk);
        this.chunkListing.add(chunk);
        chunk.setChunkLoaded(true);
        return chunk;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        Chunk chunk = this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        return chunk == null ? this.blankChunk : chunk;
    }

    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate progressCallback) {
        return true;
    }

    @Override
    public void saveExtraData() {
    }

    @Override
    public boolean unloadQueuedChunks() {
        long i = System.currentTimeMillis();
        for (Chunk chunk : this.chunkListing) {
            chunk.func_150804_b(System.currentTimeMillis() - i > 5L);
        }
        if (System.currentTimeMillis() - i > 100L) {
            logger.info("Warning: Clientside chunk ticking took {} ms", new Object[]{System.currentTimeMillis() - i});
        }
        return false;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
    }

    @Override
    public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
        return false;
    }

    @Override
    public String makeString() {
        return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements() + ", " + this.chunkListing.size();
    }

    @Override
    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return null;
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return this.chunkListing.size();
    }

    @Override
    public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_) {
    }

    @Override
    public Chunk provideChunk(BlockPos blockPosIn) {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}

