/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IRenderChunkFactory {
    public RenderChunk makeRenderChunk(World var1, RenderGlobal var2, BlockPos var3, int var4);
}

