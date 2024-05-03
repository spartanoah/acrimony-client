/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;

public abstract class PositionedSound
implements ISound {
    protected final ResourceLocation positionedSoundLocation;
    protected float volume = 1.0f;
    protected float pitch = 1.0f;
    protected float xPosF;
    protected float yPosF;
    protected float zPosF;
    protected boolean repeat = false;
    protected int repeatDelay = 0;
    protected ISound.AttenuationType attenuationType = ISound.AttenuationType.LINEAR;

    protected PositionedSound(ResourceLocation soundResource) {
        this.positionedSoundLocation = soundResource;
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return this.positionedSoundLocation;
    }

    @Override
    public boolean canRepeat() {
        return this.repeat;
    }

    @Override
    public int getRepeatDelay() {
        return this.repeatDelay;
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public float getXPosF() {
        return this.xPosF;
    }

    @Override
    public float getYPosF() {
        return this.yPosF;
    }

    @Override
    public float getZPosF() {
        return this.zPosF;
    }

    @Override
    public ISound.AttenuationType getAttenuationType() {
        return this.attenuationType;
    }
}

