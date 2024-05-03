/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_13to1_13_1.packets;

import com.viaversion.viabackwards.protocol.protocol1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public class InventoryPackets1_13_1
extends ItemRewriter<ClientboundPackets1_13, ServerboundPackets1_13, Protocol1_13To1_13_1> {
    public InventoryPackets1_13_1(Protocol1_13To1_13_1 protocol) {
        super(protocol, Type.ITEM1_13, Type.ITEM1_13_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        this.registerSetCooldown(ClientboundPackets1_13.COOLDOWN);
        this.registerWindowItems(ClientboundPackets1_13.WINDOW_ITEMS);
        this.registerSetSlot(ClientboundPackets1_13.SET_SLOT);
        ((Protocol1_13To1_13_1)this.protocol).registerClientbound(ClientboundPackets1_13.PLUGIN_MESSAGE, wrapper -> {
            String channel = wrapper.passthrough(Type.STRING);
            if (channel.equals("minecraft:trader_list")) {
                wrapper.passthrough(Type.INT);
                int size = wrapper.passthrough(Type.UNSIGNED_BYTE).shortValue();
                for (int i = 0; i < size; ++i) {
                    Item input = wrapper.passthrough(Type.ITEM1_13);
                    this.handleItemToClient(input);
                    Item output = wrapper.passthrough(Type.ITEM1_13);
                    this.handleItemToClient(output);
                    boolean secondItem = wrapper.passthrough(Type.BOOLEAN);
                    if (secondItem) {
                        Item second = wrapper.passthrough(Type.ITEM1_13);
                        this.handleItemToClient(second);
                    }
                    wrapper.passthrough(Type.BOOLEAN);
                    wrapper.passthrough(Type.INT);
                    wrapper.passthrough(Type.INT);
                }
            }
        });
        this.registerEntityEquipment(ClientboundPackets1_13.ENTITY_EQUIPMENT);
        this.registerClickWindow(ServerboundPackets1_13.CLICK_WINDOW);
        this.registerCreativeInvAction(ServerboundPackets1_13.CREATIVE_INVENTORY_ACTION);
        this.registerSpawnParticle(ClientboundPackets1_13.SPAWN_PARTICLE, Type.FLOAT);
    }
}

