/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.client.stream;

import net.minecraft.client.stream.Metadata;
import net.minecraft.entity.EntityLivingBase;

public class MetadataPlayerDeath
extends Metadata {
    public MetadataPlayerDeath(EntityLivingBase p_i46066_1_, EntityLivingBase p_i46066_2_) {
        super("player_death");
        if (p_i46066_1_ != null) {
            this.func_152808_a("player", p_i46066_1_.getCommandSenderName());
        }
        if (p_i46066_2_ != null) {
            this.func_152808_a("killer", p_i46066_2_.getCommandSenderName());
        }
    }
}

