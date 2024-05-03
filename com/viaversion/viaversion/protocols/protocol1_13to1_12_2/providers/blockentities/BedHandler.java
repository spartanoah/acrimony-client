/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class BedHandler
implements BlockEntityProvider.BlockEntityHandler {
    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        Position position;
        BlockStorage storage = user.get(BlockStorage.class);
        if (!storage.contains(position = new Position(tag.getNumberTag("x").asInt(), tag.getNumberTag("y").asShort(), tag.getNumberTag("z").asInt()))) {
            Via.getPlatform().getLogger().warning("Received an bed color update packet, but there is no bed! O_o " + tag);
            return -1;
        }
        int blockId = storage.get(position).getOriginal() - 972 + 748;
        NumberTag color = tag.getNumberTag("color");
        if (color != null) {
            blockId += color.asInt() * 16;
        }
        return blockId;
    }
}

