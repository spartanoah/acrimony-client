/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import java.util.Random;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeGenMutated
extends BiomeGenBase {
    protected BiomeGenBase baseBiome;

    public BiomeGenMutated(int id, BiomeGenBase biome) {
        super(id);
        this.baseBiome = biome;
        this.func_150557_a(biome.color, true);
        this.biomeName = biome.biomeName + " M";
        this.topBlock = biome.topBlock;
        this.fillerBlock = biome.fillerBlock;
        this.fillerBlockMetadata = biome.fillerBlockMetadata;
        this.minHeight = biome.minHeight;
        this.maxHeight = biome.maxHeight;
        this.temperature = biome.temperature;
        this.rainfall = biome.rainfall;
        this.waterColorMultiplier = biome.waterColorMultiplier;
        this.enableSnow = biome.enableSnow;
        this.enableRain = biome.enableRain;
        this.spawnableCreatureList = Lists.newArrayList(biome.spawnableCreatureList);
        this.spawnableMonsterList = Lists.newArrayList(biome.spawnableMonsterList);
        this.spawnableCaveCreatureList = Lists.newArrayList(biome.spawnableCaveCreatureList);
        this.spawnableWaterCreatureList = Lists.newArrayList(biome.spawnableWaterCreatureList);
        this.temperature = biome.temperature;
        this.rainfall = biome.rainfall;
        this.minHeight = biome.minHeight + 0.1f;
        this.maxHeight = biome.maxHeight + 0.2f;
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        this.baseBiome.theBiomeDecorator.decorate(worldIn, rand, this, pos);
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int p_180622_4_, int p_180622_5_, double p_180622_6_) {
        this.baseBiome.genTerrainBlocks(worldIn, rand, chunkPrimerIn, p_180622_4_, p_180622_5_, p_180622_6_);
    }

    @Override
    public float getSpawningChance() {
        return this.baseBiome.getSpawningChance();
    }

    @Override
    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return this.baseBiome.genBigTreeChance(rand);
    }

    @Override
    public int getFoliageColorAtPos(BlockPos pos) {
        return this.baseBiome.getFoliageColorAtPos(pos);
    }

    @Override
    public int getGrassColorAtPos(BlockPos pos) {
        return this.baseBiome.getGrassColorAtPos(pos);
    }

    @Override
    public Class<? extends BiomeGenBase> getBiomeClass() {
        return this.baseBiome.getBiomeClass();
    }

    @Override
    public boolean isEqualTo(BiomeGenBase biome) {
        return this.baseBiome.isEqualTo(biome);
    }

    @Override
    public BiomeGenBase.TempCategory getTempCategory() {
        return this.baseBiome.getTempCategory();
    }
}

