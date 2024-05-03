/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.world;

import net.minecraft.util.IChatComponent;

public interface IWorldNameable {
    public String getCommandSenderName();

    public boolean hasCustomName();

    public IChatComponent getDisplayName();
}

