/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_9to1_8.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.InventoryTracker;

public class InventoryPackets {
    public static void register(Protocol1_9To1_8 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.WINDOW_PROPERTY, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.handler(wrapper -> {
                    short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    short property = wrapper.get(Type.SHORT, 0);
                    short value = wrapper.get(Type.SHORT, 1);
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equalsIgnoreCase("minecraft:enchanting_table") && property > 3 && property < 7) {
                        short level = (short)(value >> 8);
                        short enchantID = (short)(value & 0xFF);
                        wrapper.create(wrapper.getId(), propertyPacket -> {
                            propertyPacket.write(Type.UNSIGNED_BYTE, windowId);
                            propertyPacket.write(Type.SHORT, property);
                            propertyPacket.write(Type.SHORT, enchantID);
                        }).scheduleSend(Protocol1_9To1_8.class);
                        wrapper.set(Type.SHORT, 0, (short)(property + 3));
                        wrapper.set(Type.SHORT, 1, level);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.OPEN_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.STRING);
                this.map(Type.STRING, Protocol1_9To1_8.FIX_JSON);
                this.map(Type.UNSIGNED_BYTE);
                this.handler(wrapper -> {
                    String inventory = wrapper.get(Type.STRING, 0);
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(inventory);
                });
                this.handler(wrapper -> {
                    String inventory = wrapper.get(Type.STRING, 0);
                    if (inventory.equals("minecraft:brewing_stand")) {
                        wrapper.set(Type.UNSIGNED_BYTE, 1, (short)(wrapper.get(Type.UNSIGNED_BYTE, 1) + 1));
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.SET_SLOT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.ITEM1_8);
                this.handler(wrapper -> {
                    boolean showShieldWhenSwordInHand;
                    Item stack = wrapper.get(Type.ITEM1_8, 0);
                    boolean bl = showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand() && Via.getConfig().isShieldBlocking();
                    if (showShieldWhenSwordInHand) {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        EntityTracker1_9 entityTracker = (EntityTracker1_9)wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        short slotID = wrapper.get(Type.SHORT, 0);
                        byte windowId = wrapper.get(Type.UNSIGNED_BYTE, 0).byteValue();
                        inventoryTracker.setItemId(windowId, slotID, stack == null ? 0 : stack.identifier());
                        entityTracker.syncShieldWithSword();
                    }
                    ItemRewriter.toClient(stack);
                });
                this.handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    short slotID = wrapper.get(Type.SHORT, 0);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equals("minecraft:brewing_stand") && slotID >= 4) {
                        wrapper.set(Type.SHORT, 0, (short)(slotID + 1));
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.WINDOW_ITEMS, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.ITEM1_8_SHORT_ARRAY);
                this.handler(wrapper -> {
                    Item[] stacks = wrapper.get(Type.ITEM1_8_SHORT_ARRAY, 0);
                    Short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    EntityTracker1_9 entityTracker = (EntityTracker1_9)wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand() && Via.getConfig().isShieldBlocking();
                    for (short i = 0; i < stacks.length; i = (short)((short)(i + 1))) {
                        Item stack = stacks[i];
                        if (showShieldWhenSwordInHand) {
                            inventoryTracker.setItemId(windowId, i, stack == null ? 0 : stack.identifier());
                        }
                        ItemRewriter.toClient(stack);
                    }
                    if (showShieldWhenSwordInHand) {
                        entityTracker.syncShieldWithSword();
                    }
                });
                this.handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                        Item[] oldStack = wrapper.get(Type.ITEM1_8_SHORT_ARRAY, 0);
                        Item[] newStack = new Item[oldStack.length + 1];
                        for (int i = 0; i < newStack.length; ++i) {
                            if (i > 4) {
                                newStack[i] = oldStack[i - 1];
                                continue;
                            }
                            if (i == 4) continue;
                            newStack[i] = oldStack[i];
                        }
                        wrapper.set(Type.ITEM1_8_SHORT_ARRAY, 0, newStack);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CLOSE_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(null);
                    inventoryTracker.resetInventory(wrapper.get(Type.UNSIGNED_BYTE, 0));
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.MAP_DATA, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.BYTE);
                this.handler(wrapper -> wrapper.write(Type.BOOLEAN, true));
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.CREATIVE_INVENTORY_ACTION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.SHORT);
                this.map(Type.ITEM1_8);
                this.handler(wrapper -> {
                    boolean showShieldWhenSwordInHand;
                    Item stack = wrapper.get(Type.ITEM1_8, 0);
                    boolean bl = showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand() && Via.getConfig().isShieldBlocking();
                    if (showShieldWhenSwordInHand) {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        EntityTracker1_9 entityTracker = (EntityTracker1_9)wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        short slotID = wrapper.get(Type.SHORT, 0);
                        inventoryTracker.setItemId((short)0, slotID, stack == null ? 0 : stack.identifier());
                        entityTracker.syncShieldWithSword();
                    }
                    ItemRewriter.toServer(stack);
                });
                this.handler(wrapper -> {
                    boolean throwItem;
                    short slot = wrapper.get(Type.SHORT, 0);
                    boolean bl = throwItem = slot == 45;
                    if (throwItem) {
                        wrapper.create(ClientboundPackets1_9.SET_SLOT, w -> {
                            w.write(Type.UNSIGNED_BYTE, (short)0);
                            w.write(Type.SHORT, slot);
                            w.write(Type.ITEM1_8, null);
                        }).send(Protocol1_9To1_8.class);
                        wrapper.set(Type.SHORT, 0, (short)-999);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.CLICK_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map((Type)Type.VAR_INT, Type.BYTE);
                this.map(Type.ITEM1_8);
                this.handler(wrapper -> {
                    Item stack = wrapper.get(Type.ITEM1_8, 0);
                    if (Via.getConfig().isShowShieldWhenSwordInHand()) {
                        Short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        byte mode = wrapper.get(Type.BYTE, 1);
                        short hoverSlot = wrapper.get(Type.SHORT, 0);
                        byte button = wrapper.get(Type.BYTE, 0);
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.handleWindowClick(wrapper.user(), windowId, mode, hoverSlot, button);
                    }
                    ItemRewriter.toServer(stack);
                });
                this.handler(wrapper -> {
                    short windowID = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    short slot = wrapper.get(Type.SHORT, 0);
                    boolean throwItem = slot == 45 && windowID == 0;
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                        if (slot == 4) {
                            throwItem = true;
                        }
                        if (slot > 4) {
                            wrapper.set(Type.SHORT, 0, (short)(slot - 1));
                        }
                    }
                    if (throwItem) {
                        wrapper.create(ClientboundPackets1_9.SET_SLOT, w -> {
                            w.write(Type.UNSIGNED_BYTE, windowID);
                            w.write(Type.SHORT, slot);
                            w.write(Type.ITEM1_8, null);
                        }).scheduleSend(Protocol1_9To1_8.class);
                        wrapper.set(Type.BYTE, 0, (byte)0);
                        wrapper.set(Type.BYTE, 1, (byte)0);
                        wrapper.set(Type.SHORT, 0, (short)-999);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.CLOSE_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(null);
                    inventoryTracker.resetInventory(wrapper.get(Type.UNSIGNED_BYTE, 0));
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.HELD_ITEM_CHANGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.SHORT);
                this.handler(wrapper -> {
                    boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand() && Via.getConfig().isShieldBlocking();
                    EntityTracker1_9 entityTracker = (EntityTracker1_9)wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    if (entityTracker.isBlocking()) {
                        entityTracker.setBlocking(false);
                        if (!showShieldWhenSwordInHand) {
                            entityTracker.setSecondHand(null);
                        }
                    }
                    if (showShieldWhenSwordInHand) {
                        entityTracker.setHeldItemSlot(wrapper.get(Type.SHORT, 0).shortValue());
                        entityTracker.syncShieldWithSword();
                    }
                });
            }
        });
    }
}

