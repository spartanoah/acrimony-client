/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.entity.living.player.Player
 */
package com.viaversion.viaversion.sponge.platform;

import com.viaversion.viaversion.ViaAPIBase;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.entity.living.player.Player;

public class SpongeViaAPI
extends ViaAPIBase<Player> {
    @Override
    public int getPlayerVersion(Player player) {
        return this.getPlayerVersion(player.uniqueId());
    }

    @Override
    public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
        this.sendRawPacket(player.uniqueId(), packet);
    }
}

