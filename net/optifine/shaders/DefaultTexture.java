/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine.shaders;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;
import net.optifine.shaders.ShadersTex;

public class DefaultTexture
extends AbstractTexture {
    public DefaultTexture() {
        this.loadTexture(null);
    }

    @Override
    public void loadTexture(IResourceManager resourcemanager) {
        int[] aint = ShadersTex.createAIntImage(1, -1);
        ShadersTex.setupTexture(this.getMultiTexID(), aint, 1, 1, false, false);
    }
}

