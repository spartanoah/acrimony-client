/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.renderer;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.src.Config;

public class VertexBufferUploader
extends WorldVertexBufferUploader {
    private VertexBuffer vertexBuffer = null;

    @Override
    public void func_181679_a(WorldRenderer p_181679_1_) {
        if (p_181679_1_.getDrawMode() == 7 && Config.isQuadsToTriangles()) {
            p_181679_1_.quadsToTriangles();
            this.vertexBuffer.setDrawMode(p_181679_1_.getDrawMode());
        }
        this.vertexBuffer.func_181722_a(p_181679_1_.getByteBuffer());
        p_181679_1_.reset();
    }

    public void setVertexBuffer(VertexBuffer vertexBufferIn) {
        this.vertexBuffer = vertexBufferIn;
    }
}

