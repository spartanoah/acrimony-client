/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_14_2to1_14_3;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public class Protocol1_14_2To1_14_3
extends BackwardsProtocol<ClientboundPackets1_14, ClientboundPackets1_14, ServerboundPackets1_14, ServerboundPackets1_14> {
    public Protocol1_14_2To1_14_3() {
        super(ClientboundPackets1_14.class, ClientboundPackets1_14.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        this.registerClientbound(ClientboundPackets1_14.TRADE_LIST, (PacketWrapper wrapper) -> {
            wrapper.passthrough(Type.VAR_INT);
            int size = wrapper.passthrough(Type.UNSIGNED_BYTE).shortValue();
            for (int i = 0; i < size; ++i) {
                wrapper.passthrough(Type.ITEM1_13_2);
                wrapper.passthrough(Type.ITEM1_13_2);
                if (wrapper.passthrough(Type.BOOLEAN).booleanValue()) {
                    wrapper.passthrough(Type.ITEM1_13_2);
                }
                wrapper.passthrough(Type.BOOLEAN);
                wrapper.passthrough(Type.INT);
                wrapper.passthrough(Type.INT);
                wrapper.passthrough(Type.INT);
                wrapper.passthrough(Type.INT);
                wrapper.passthrough(Type.FLOAT);
            }
            wrapper.passthrough(Type.VAR_INT);
            wrapper.passthrough(Type.VAR_INT);
            wrapper.passthrough(Type.BOOLEAN);
            wrapper.read(Type.BOOLEAN);
        });
        RecipeRewriter<ClientboundPackets1_14> recipeHandler = new RecipeRewriter<ClientboundPackets1_14>(this);
        this.registerClientbound(ClientboundPackets1_14.DECLARE_RECIPES, (PacketWrapper wrapper) -> {
            int size = wrapper.passthrough(Type.VAR_INT);
            int deleted = 0;
            for (int i = 0; i < size; ++i) {
                String fullType = wrapper.read(Type.STRING);
                String type = Key.stripMinecraftNamespace(fullType);
                String id = wrapper.read(Type.STRING);
                if (type.equals("crafting_special_repairitem")) {
                    ++deleted;
                    continue;
                }
                wrapper.write(Type.STRING, fullType);
                wrapper.write(Type.STRING, id);
                recipeHandler.handleRecipeType(wrapper, type);
            }
            wrapper.set(Type.VAR_INT, 0, size - deleted);
        });
    }
}

