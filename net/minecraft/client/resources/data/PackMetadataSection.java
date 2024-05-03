/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.resources.data;

import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.IChatComponent;

public class PackMetadataSection
implements IMetadataSection {
    private final IChatComponent packDescription;
    private final int packFormat;

    public PackMetadataSection(IChatComponent p_i1034_1_, int p_i1034_2_) {
        this.packDescription = p_i1034_1_;
        this.packFormat = p_i1034_2_;
    }

    public IChatComponent getPackDescription() {
        return this.packDescription;
    }

    public int getPackFormat() {
        return this.packFormat;
    }
}

