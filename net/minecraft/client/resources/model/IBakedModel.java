/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.resources.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public interface IBakedModel {
    public List<BakedQuad> getFaceQuads(EnumFacing var1);

    public List<BakedQuad> getGeneralQuads();

    public boolean isAmbientOcclusion();

    public boolean isGui3d();

    public boolean isBuiltInRenderer();

    public TextureAtlasSprite getTexture();

    public ItemCameraTransforms getItemCameraTransforms();
}

