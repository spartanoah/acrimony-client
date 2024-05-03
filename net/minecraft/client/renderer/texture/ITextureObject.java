/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer.texture;

import java.io.IOException;
import net.minecraft.client.resources.IResourceManager;
import net.optifine.shaders.MultiTexID;

public interface ITextureObject {
    public void setBlurMipmap(boolean var1, boolean var2);

    public void restoreLastBlurMipmap();

    public void loadTexture(IResourceManager var1) throws IOException;

    public int getGlTextureId();

    public MultiTexID getMultiTexID();
}

