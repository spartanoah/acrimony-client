/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ITickable;

public interface ITickableSound
extends ISound,
ITickable {
    public boolean isDonePlaying();
}

