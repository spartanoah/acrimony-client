/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;

public interface IEntityOwnable {
    public String getOwnerId();

    public Entity getOwner();
}

