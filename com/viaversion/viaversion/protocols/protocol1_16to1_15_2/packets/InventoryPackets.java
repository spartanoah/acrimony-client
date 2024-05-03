/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.packets;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.storage.InventoryTracker1_16;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.UUID;

public class InventoryPackets
extends ItemRewriter<ClientboundPackets1_15, ServerboundPackets1_16, Protocol1_16To1_15_2> {
    public InventoryPackets(Protocol1_16To1_15_2 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        final PacketHandler cursorRemapper = wrapper -> {
            PacketWrapper clearPacket = wrapper.create(ClientboundPackets1_16.SET_SLOT);
            clearPacket.write(Type.UNSIGNED_BYTE, (short)-1);
            clearPacket.write(Type.SHORT, (short)-1);
            clearPacket.write(Type.ITEM1_13_2, null);
            clearPacket.send(Protocol1_16To1_15_2.class);
        };
        ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_15.OPEN_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.map(Type.COMPONENT);
                this.handler(cursorRemapper);
                this.handler(wrapper -> {
                    InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
                    int windowType = wrapper.get(Type.VAR_INT, 1);
                    if (windowType >= 20) {
                        wrapper.set(Type.VAR_INT, 1, ++windowType);
                    }
                    inventoryTracker.setInventoryOpen(true);
                });
            }
        });
        ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_15.CLOSE_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.handler(cursorRemapper);
                this.handler(wrapper -> {
                    InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
                    inventoryTracker.setInventoryOpen(false);
                });
            }
        });
        ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_15.WINDOW_PROPERTY, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.handler(wrapper -> {
                    short enchantmentId;
                    short property = wrapper.get(Type.SHORT, 0);
                    if (property >= 4 && property <= 6 && (enchantmentId = wrapper.get(Type.SHORT, 1).shortValue()) >= 11) {
                        enchantmentId = (short)(enchantmentId + 1);
                        wrapper.set(Type.SHORT, 1, enchantmentId);
                    }
                });
            }
        });
        this.registerSetCooldown(ClientboundPackets1_15.COOLDOWN);
        this.registerWindowItems(ClientboundPackets1_15.WINDOW_ITEMS);
        this.registerTradeList(ClientboundPackets1_15.TRADE_LIST);
        this.registerSetSlot(ClientboundPackets1_15.SET_SLOT);
        this.registerAdvancements(ClientboundPackets1_15.ADVANCEMENTS);
        ((Protocol1_16To1_15_2)this.protocol).registerClientbound(ClientboundPackets1_15.ENTITY_EQUIPMENT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.handler(wrapper -> {
                    int slot = wrapper.read(Type.VAR_INT);
                    wrapper.write(Type.BYTE, (byte)slot);
                    InventoryPackets.this.handleItemToClient(wrapper.passthrough(Type.ITEM1_13_2));
                });
            }
        });
        new RecipeRewriter<ClientboundPackets1_15>(this.protocol).register(ClientboundPackets1_15.DECLARE_RECIPES);
        this.registerClickWindow(ServerboundPackets1_16.CLICK_WINDOW);
        this.registerCreativeInvAction(ServerboundPackets1_16.CREATIVE_INVENTORY_ACTION);
        ((Protocol1_16To1_15_2)this.protocol).registerServerbound(ServerboundPackets1_16.CLOSE_WINDOW, wrapper -> {
            InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
            inventoryTracker.setInventoryOpen(false);
        });
        ((Protocol1_16To1_15_2)this.protocol).registerServerbound(ServerboundPackets1_16.EDIT_BOOK, wrapper -> this.handleItemToServer(wrapper.passthrough(Type.ITEM1_13_2)));
        this.registerSpawnParticle(ClientboundPackets1_15.SPAWN_PARTICLE, Type.DOUBLE);
    }

    @Override
    public Item handleItemToClient(Item item) {
        ListTag pages;
        if (item == null) {
            return null;
        }
        CompoundTag tag = item.tag();
        if (item.identifier() == 771 && tag != null) {
            StringTag idTag;
            CompoundTag ownerTag = tag.getCompoundTag("SkullOwner");
            if (ownerTag != null && (idTag = ownerTag.getStringTag("Id")) != null) {
                UUID id = UUID.fromString(idTag.getValue());
                ownerTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(id)));
            }
        } else if (item.identifier() == 759 && tag != null && (pages = tag.getListTag("pages")) != null) {
            for (Tag pageTag : pages) {
                if (!(pageTag instanceof StringTag)) continue;
                StringTag page = (StringTag)pageTag;
                page.setValue(((Protocol1_16To1_15_2)this.protocol).getComponentRewriter().processText(page.getValue()).toString());
            }
        }
        InventoryPackets.oldToNewAttributes(item);
        item.setIdentifier(Protocol1_16To1_15_2.MAPPINGS.getNewItemId(item.identifier()));
        return item;
    }

    @Override
    public Item handleItemToServer(Item item) {
        IntArrayTag idTag;
        CompoundTag tag;
        CompoundTag ownerTag;
        if (item == null) {
            return null;
        }
        item.setIdentifier(Protocol1_16To1_15_2.MAPPINGS.getOldItemId(item.identifier()));
        if (item.identifier() == 771 && item.tag() != null && (ownerTag = (tag = item.tag()).getCompoundTag("SkullOwner")) != null && (idTag = ownerTag.getIntArrayTag("Id")) != null) {
            UUID id = UUIDUtil.fromIntArray(idTag.getValue());
            ownerTag.putString("Id", id.toString());
        }
        InventoryPackets.newToOldAttributes(item);
        return item;
    }

    public static void oldToNewAttributes(Item item) {
        if (item.tag() == null) {
            return;
        }
        ListTag attributes = item.tag().getListTag("AttributeModifiers");
        if (attributes == null) {
            return;
        }
        for (Tag tag : attributes) {
            if (!(tag instanceof CompoundTag)) continue;
            CompoundTag attribute = (CompoundTag)tag;
            InventoryPackets.rewriteAttributeName(attribute, "AttributeName", false);
            InventoryPackets.rewriteAttributeName(attribute, "Name", false);
            NumberTag leastTag = attribute.getNumberTag("UUIDLeast");
            NumberTag mostTag = attribute.getNumberTag("UUIDMost");
            if (leastTag == null || mostTag == null) continue;
            int[] uuidIntArray = UUIDUtil.toIntArray(leastTag.asLong(), mostTag.asLong());
            attribute.put("UUID", new IntArrayTag(uuidIntArray));
        }
    }

    public static void newToOldAttributes(Item item) {
        if (item.tag() == null) {
            return;
        }
        ListTag attributes = item.tag().getListTag("AttributeModifiers");
        if (attributes == null) {
            return;
        }
        for (Tag tag : attributes) {
            if (!(tag instanceof CompoundTag)) continue;
            CompoundTag attribute = (CompoundTag)tag;
            InventoryPackets.rewriteAttributeName(attribute, "AttributeName", true);
            InventoryPackets.rewriteAttributeName(attribute, "Name", true);
            IntArrayTag uuidTag = attribute.getIntArrayTag("UUID");
            if (uuidTag == null || uuidTag.getValue().length != 4) continue;
            UUID uuid = UUIDUtil.fromIntArray(uuidTag.getValue());
            attribute.putLong("UUIDLeast", uuid.getLeastSignificantBits());
            attribute.putLong("UUIDMost", uuid.getMostSignificantBits());
        }
    }

    public static void rewriteAttributeName(CompoundTag compoundTag, String entryName, boolean inverse) {
        String mappedAttribute;
        StringTag attributeNameTag = compoundTag.getStringTag(entryName);
        if (attributeNameTag == null) {
            return;
        }
        String attributeName = attributeNameTag.getValue();
        if (inverse) {
            attributeName = Key.namespaced(attributeName);
        }
        if ((mappedAttribute = (String)(inverse ? Protocol1_16To1_15_2.MAPPINGS.getAttributeMappings().inverse() : Protocol1_16To1_15_2.MAPPINGS.getAttributeMappings()).get(attributeName)) == null) {
            return;
        }
        attributeNameTag.setValue(mappedAttribute);
    }
}

