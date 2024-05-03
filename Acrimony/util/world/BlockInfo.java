/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.world;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class BlockInfo {
    public final BlockPos pos;
    public final EnumFacing facing;

    public BlockInfo(BlockPos pos, EnumFacing facing) {
        this.pos = pos;
        this.facing = facing;
    }
}

