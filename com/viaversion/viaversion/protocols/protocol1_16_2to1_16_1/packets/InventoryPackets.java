/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class InventoryPackets
extends ItemRewriter<ClientboundPackets1_16, ServerboundPackets1_16_2, Protocol1_16_2To1_16_1> {
    public InventoryPackets(Protocol1_16_2To1_16_1 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        this.registerSetCooldown(ClientboundPackets1_16.COOLDOWN);
        this.registerWindowItems(ClientboundPackets1_16.WINDOW_ITEMS);
        this.registerTradeList(ClientboundPackets1_16.TRADE_LIST);
        this.registerSetSlot(ClientboundPackets1_16.SET_SLOT);
        this.registerEntityEquipmentArray(ClientboundPackets1_16.ENTITY_EQUIPMENT);
        this.registerAdvancements(ClientboundPackets1_16.ADVANCEMENTS);
        ((Protocol1_16_2To1_16_1)this.protocol).registerClientbound(ClientboundPackets1_16.UNLOCK_RECIPES, wrapper -> {
            wrapper.passthrough(Type.VAR_INT);
            wrapper.passthrough(Type.BOOLEAN);
            wrapper.passthrough(Type.BOOLEAN);
            wrapper.passthrough(Type.BOOLEAN);
            wrapper.passthrough(Type.BOOLEAN);
            wrapper.write(Type.BOOLEAN, false);
            wrapper.write(Type.BOOLEAN, false);
            wrapper.write(Type.BOOLEAN, false);
            wrapper.write(Type.BOOLEAN, false);
        });
        new RecipeRewriter<ClientboundPackets1_16>(this.protocol).register(ClientboundPackets1_16.DECLARE_RECIPES);
        this.registerClickWindow(ServerboundPackets1_16_2.CLICK_WINDOW);
        this.registerCreativeInvAction(ServerboundPackets1_16_2.CREATIVE_INVENTORY_ACTION);
        ((Protocol1_16_2To1_16_1)this.protocol).registerServerbound(ServerboundPackets1_16_2.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.passthrough(Type.ITEM1_13_2)));
        this.registerSpawnParticle(ClientboundPackets1_16.SPAWN_PARTICLE, Type.DOUBLE);
    }
}

