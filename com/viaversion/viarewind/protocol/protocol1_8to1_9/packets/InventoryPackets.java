/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.Windows;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;

public class InventoryPackets {
    public static void register(final Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_9.CLOSE_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    packetWrapper.user().get(Windows.class).remove(windowsId);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.OPEN_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.STRING);
                this.map(Type.COMPONENT);
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    String type = packetWrapper.get(Type.STRING, 0);
                    if (type.equals("EntityHorse")) {
                        packetWrapper.passthrough(Type.INT);
                    }
                });
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    String windowType = packetWrapper.get(Type.STRING, 0);
                    packetWrapper.user().get(Windows.class).put(windowId, windowType);
                });
                this.handler(packetWrapper -> {
                    String name;
                    String type = packetWrapper.get(Type.STRING, 0);
                    if (type.equalsIgnoreCase("minecraft:shulker_box")) {
                        type = "minecraft:container";
                        packetWrapper.set(Type.STRING, 0, "minecraft:container");
                    }
                    if ((name = packetWrapper.get(Type.COMPONENT, 0).toString()).equalsIgnoreCase("{\"translate\":\"container.shulkerBox\"}")) {
                        packetWrapper.set(Type.COMPONENT, 0, JsonParser.parseString("{\"text\":\"Shulker Box\"}"));
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.WINDOW_ITEMS, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    Item[] items = packetWrapper.read(Type.ITEM1_8_SHORT_ARRAY);
                    for (int i = 0; i < items.length; ++i) {
                        items[i] = protocol.getItemRewriter().handleItemToClient(items[i]);
                    }
                    if (windowId == 0 && items.length == 46) {
                        Item[] old = items;
                        items = new Item[45];
                        System.arraycopy(old, 0, items, 0, 45);
                    } else {
                        String type = packetWrapper.user().get(Windows.class).get(windowId);
                        if (type != null && type.equalsIgnoreCase("minecraft:brewing_stand")) {
                            System.arraycopy(items, 0, packetWrapper.user().get(Windows.class).getBrewingItems(windowId), 0, 4);
                            Windows.updateBrewingStand(packetWrapper.user(), items[4], windowId);
                            Item[] old = items;
                            items = new Item[old.length - 1];
                            System.arraycopy(old, 0, items, 0, 4);
                            System.arraycopy(old, 5, items, 4, old.length - 5);
                        }
                    }
                    packetWrapper.write(Type.ITEM1_8_SHORT_ARRAY, items);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9.SET_SLOT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.ITEM1_8);
                this.handler(packetWrapper -> {
                    packetWrapper.set(Type.ITEM1_8, 0, protocol.getItemRewriter().handleItemToClient(packetWrapper.get(Type.ITEM1_8, 0)));
                    byte windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0).byteValue();
                    short slot = packetWrapper.get(Type.SHORT, 0);
                    if (windowId == 0 && slot == 45) {
                        packetWrapper.cancel();
                        return;
                    }
                    String type = packetWrapper.user().get(Windows.class).get(windowId);
                    if (type == null) {
                        return;
                    }
                    if (type.equalsIgnoreCase("minecraft:brewing_stand")) {
                        if (slot > 4) {
                            slot = (short)(slot - 1);
                            packetWrapper.set(Type.SHORT, 0, slot);
                        } else if (slot == 4) {
                            packetWrapper.cancel();
                            Windows.updateBrewingStand(packetWrapper.user(), packetWrapper.get(Type.ITEM1_8, 0), windowId);
                        } else {
                            packetWrapper.user().get(Windows.class).getBrewingItems((short)((short)windowId))[slot] = packetWrapper.get(Type.ITEM1_8, 0);
                        }
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.CLOSE_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.handler(packetWrapper -> {
                    short windowsId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    packetWrapper.user().get(Windows.class).remove(windowsId);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.CLICK_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map((Type)Type.BYTE, Type.VAR_INT);
                this.map(Type.ITEM1_8);
                this.handler(packetWrapper -> packetWrapper.set(Type.ITEM1_8, 0, protocol.getItemRewriter().handleItemToServer(packetWrapper.get(Type.ITEM1_8, 0))));
                this.handler(packetWrapper -> {
                    short slot;
                    short windowId = packetWrapper.get(Type.UNSIGNED_BYTE, 0);
                    Windows windows = packetWrapper.user().get(Windows.class);
                    String type = windows.get(windowId);
                    if (type == null) {
                        return;
                    }
                    if (type.equalsIgnoreCase("minecraft:brewing_stand") && (slot = packetWrapper.get(Type.SHORT, 0).shortValue()) > 3) {
                        slot = (short)(slot + 1);
                        packetWrapper.set(Type.SHORT, 0, slot);
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_8.CREATIVE_INVENTORY_ACTION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.SHORT);
                this.map(Type.ITEM1_8);
                this.handler(packetWrapper -> packetWrapper.set(Type.ITEM1_8, 0, protocol.getItemRewriter().handleItemToServer(packetWrapper.get(Type.ITEM1_8, 0))));
            }
        });
    }
}

