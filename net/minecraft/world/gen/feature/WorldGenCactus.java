/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenCactus
extends WorldGenerator {
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            if (!worldIn.isAirBlock(blockpos)) continue;
            int j = 1 + rand.nextInt(rand.nextInt(3) + 1);
            for (int k = 0; k < j; ++k) {
                if (!Blocks.cactus.canBlockStay(worldIn, blockpos)) continue;
                worldIn.setBlockState(blockpos.up(k), Blocks.cactus.getDefaultState(), 2);
            }
        }
        return true;
    }
}

