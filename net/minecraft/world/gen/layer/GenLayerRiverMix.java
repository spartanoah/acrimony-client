/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.gen.layer;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerRiverMix
extends GenLayer {
    private GenLayer biomePatternGeneratorChain;
    private GenLayer riverPatternGeneratorChain;

    public GenLayerRiverMix(long p_i2129_1_, GenLayer p_i2129_3_, GenLayer p_i2129_4_) {
        super(p_i2129_1_);
        this.biomePatternGeneratorChain = p_i2129_3_;
        this.riverPatternGeneratorChain = p_i2129_4_;
    }

    @Override
    public void initWorldGenSeed(long seed) {
        this.biomePatternGeneratorChain.initWorldGenSeed(seed);
        this.riverPatternGeneratorChain.initWorldGenSeed(seed);
        super.initWorldGenSeed(seed);
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] aint = this.biomePatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] aint1 = this.riverPatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
        int[] aint2 = IntCache.getIntCache(areaWidth * areaHeight);
        for (int i = 0; i < areaWidth * areaHeight; ++i) {
            if (aint[i] != BiomeGenBase.ocean.biomeID && aint[i] != BiomeGenBase.deepOcean.biomeID) {
                if (aint1[i] == BiomeGenBase.river.biomeID) {
                    if (aint[i] == BiomeGenBase.icePlains.biomeID) {
                        aint2[i] = BiomeGenBase.frozenRiver.biomeID;
                        continue;
                    }
                    if (aint[i] != BiomeGenBase.mushroomIsland.biomeID && aint[i] != BiomeGenBase.mushroomIslandShore.biomeID) {
                        aint2[i] = aint1[i] & 0xFF;
                        continue;
                    }
                    aint2[i] = BiomeGenBase.mushroomIslandShore.biomeID;
                    continue;
                }
                aint2[i] = aint[i];
                continue;
            }
            aint2[i] = aint[i];
        }
        return aint2;
    }
}

