/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InventoryPackets
extends ItemRewriter<ClientboundPackets1_9_3, ServerboundPackets1_12, Protocol1_12To1_11_1> {
    public InventoryPackets(Protocol1_12To1_11_1 protocol) {
        super(protocol, Type.ITEM1_8, Type.ITEM1_8_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        this.registerSetSlot(ClientboundPackets1_9_3.SET_SLOT);
        this.registerWindowItems(ClientboundPackets1_9_3.WINDOW_ITEMS);
        this.registerEntityEquipment(ClientboundPackets1_9_3.ENTITY_EQUIPMENT);
        ((Protocol1_12To1_11_1)this.protocol).registerClientbound(ClientboundPackets1_9_3.PLUGIN_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    if (wrapper.get(Type.STRING, 0).equalsIgnoreCase("MC|TrList")) {
                        wrapper.passthrough(Type.INT);
                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE).shortValue();
                        for (int i = 0; i < size; ++i) {
                            InventoryPackets.this.handleItemToClient(wrapper.passthrough(Type.ITEM1_8));
                            InventoryPackets.this.handleItemToClient(wrapper.passthrough(Type.ITEM1_8));
                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN);
                            if (secondItem) {
                                InventoryPackets.this.handleItemToClient(wrapper.passthrough(Type.ITEM1_8));
                            }
                            wrapper.passthrough(Type.BOOLEAN);
                            wrapper.passthrough(Type.INT);
                            wrapper.passthrough(Type.INT);
                        }
                    }
                });
            }
        });
        ((Protocol1_12To1_11_1)this.protocol).registerServerbound(ServerboundPackets1_12.CLICK_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map(Type.VAR_INT);
                this.map(Type.ITEM1_8);
                this.handler(wrapper -> {
                    Item item = wrapper.get(Type.ITEM1_8, 0);
                    if (!Via.getConfig().is1_12QuickMoveActionFix()) {
                        InventoryPackets.this.handleItemToServer(item);
                        return;
                    }
                    byte button = wrapper.get(Type.BYTE, 0);
                    int mode = wrapper.get(Type.VAR_INT, 0);
                    if (mode == 1 && button == 0 && item == null) {
                        short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        short slotId = wrapper.get(Type.SHORT, 0);
                        short actionId = wrapper.get(Type.SHORT, 1);
                        InventoryQuickMoveProvider provider = Via.getManager().getProviders().get(InventoryQuickMoveProvider.class);
                        boolean succeed = provider.registerQuickMoveAction(windowId, slotId, actionId, wrapper.user());
                        if (succeed) {
                            wrapper.cancel();
                        }
                    } else {
                        InventoryPackets.this.handleItemToServer(item);
                    }
                });
            }
        });
        this.registerCreativeInvAction(ServerboundPackets1_12.CREATIVE_INVENTORY_ACTION);
    }

    @Override
    public Item handleItemToServer(Item item) {
        if (item == null) {
            return null;
        }
        if (item.identifier() == 355) {
            item.setData((short)0);
        }
        boolean newItem = item.identifier() >= 235 && item.identifier() <= 252;
        if (newItem |= item.identifier() == 453) {
            item.setIdentifier(1);
            item.setData((short)0);
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToClient(@Nullable Item item) {
        if (item == null) {
            return null;
        }
        if (item.identifier() == 355) {
            item.setData((short)14);
        }
        return item;
    }
}

