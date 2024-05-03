/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.ai.attributes;

public interface IAttribute {
    public String getAttributeUnlocalizedName();

    public double clampValue(double var1);

    public double getDefaultValue();

    public boolean getShouldWatch();

    public IAttribute func_180372_d();
}

