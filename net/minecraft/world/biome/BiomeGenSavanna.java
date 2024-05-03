/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenMutated;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;

public class BiomeGenSavanna
extends BiomeGenBase {
    private static final WorldGenSavannaTree field_150627_aC = new WorldGenSavannaTree(false);

    protected BiomeGenSavanna(int p_i45383_1_) {
        super(p_i45383_1_);
        this.spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityHorse.class, 1, 2, 6));
        this.theBiomeDecorator.treesPerChunk = 1;
        this.theBiomeDecorator.flowersPerChunk = 4;
        this.theBiomeDecorator.grassPerChunk = 20;
    }

    @Override
    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return rand.nextInt(5) > 0 ? field_150627_aC : this.worldGeneratorTrees;
    }

    @Override
    protected BiomeGenBase createMutatedBiome(int p_180277_1_) {
        Mutated biomegenbase = new Mutated(p_180277_1_, this);
        biomegenbase.temperature = (this.temperature + 1.0f) * 0.5f;
        biomegenbase.minHeight = this.minHeight * 0.5f + 0.3f;
        biomegenbase.maxHeight = this.maxHeight * 0.5f + 1.2f;
        return biomegenbase;
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
        for (int i = 0; i < 7; ++i) {
            int j = rand.nextInt(16) + 8;
            int k = rand.nextInt(16) + 8;
            int l = rand.nextInt(worldIn.getHeight(pos.add(j, 0, k)).getY() + 32);
            DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, pos.add(j, l, k));
        }
        super.decorate(worldIn, rand, pos);
    }

    public static class Mutated
    extends BiomeGenMutated {
        public Mutated(int p_i45382_1_, BiomeGenBase p_i45382_2_) {
            super(p_i45382_1_, p_i45382_2_);
            this.theBiomeDecorator.treesPerChunk = 2;
            this.theBiomeDecorator.flowersPerChunk = 2;
            this.theBiomeDecorator.grassPerChunk = 5;
        }

        @Override
        public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int p_180622_4_, int p_180622_5_, double p_180622_6_) {
            this.topBlock = Blocks.grass.getDefaultState();
            this.fillerBlock = Blocks.dirt.getDefaultState();
            if (p_180622_6_ > 1.75) {
                this.topBlock = Blocks.stone.getDefaultState();
                this.fillerBlock = Blocks.stone.getDefaultState();
            } else if (p_180622_6_ > -0.5) {
                this.topBlock = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
            }
            this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, p_180622_4_, p_180622_5_, p_180622_6_);
        }

        @Override
        public void decorate(World worldIn, Random rand, BlockPos pos) {
            this.theBiomeDecorator.decorate(worldIn, rand, this, pos);
        }
    }
}

