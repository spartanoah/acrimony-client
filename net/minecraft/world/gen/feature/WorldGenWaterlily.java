/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenWaterlily
extends WorldGenerator {
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int i = 0; i < 10; ++i) {
            int l;
            int k;
            int j = position.getX() + rand.nextInt(8) - rand.nextInt(8);
            if (!worldIn.isAirBlock(new BlockPos(j, k = position.getY() + rand.nextInt(4) - rand.nextInt(4), l = position.getZ() + rand.nextInt(8) - rand.nextInt(8))) || !Blocks.waterlily.canPlaceBlockAt(worldIn, new BlockPos(j, k, l))) continue;
            worldIn.setBlockState(new BlockPos(j, k, l), Blocks.waterlily.getDefaultState(), 2);
        }
        return true;
    }
}

