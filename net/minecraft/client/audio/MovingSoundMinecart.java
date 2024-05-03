/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class MovingSoundMinecart
extends MovingSound {
    private final EntityMinecart minecart;
    private float distance = 0.0f;

    public MovingSoundMinecart(EntityMinecart minecartIn) {
        super(new ResourceLocation("minecraft:minecart.base"));
        this.minecart = minecartIn;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    @Override
    public void update() {
        if (this.minecart.isDead) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float)this.minecart.posX;
            this.yPosF = (float)this.minecart.posY;
            this.zPosF = (float)this.minecart.posZ;
            float f = MathHelper.sqrt_double(this.minecart.motionX * this.minecart.motionX + this.minecart.motionZ * this.minecart.motionZ);
            if ((double)f >= 0.01) {
                this.distance = MathHelper.clamp_float(this.distance + 0.0025f, 0.0f, 1.0f);
                this.volume = 0.0f + MathHelper.clamp_float(f, 0.0f, 0.5f) * 0.7f;
            } else {
                this.distance = 0.0f;
                this.volume = 0.0f;
            }
        }
    }
}

