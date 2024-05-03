/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.NumberTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.SoundSource;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.SpawnEggRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

public class InventoryPackets
extends ItemRewriter<ClientboundPackets1_12_1, ServerboundPackets1_13, Protocol1_13To1_12_2> {
    private static final String NBT_TAG_NAME = "ViaVersion|" + Protocol1_13To1_12_2.class.getSimpleName();

    public InventoryPackets(Protocol1_13To1_12_2 protocol) {
        super(protocol, null, null);
    }

    @Override
    public void registerPackets() {
        ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_12_1.SET_SLOT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.ITEM1_8, Type.ITEM1_13);
                this.handler(InventoryPackets.this.itemToClientHandler(Type.ITEM1_13));
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_12_1.WINDOW_ITEMS, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.ITEM1_8_SHORT_ARRAY, Type.ITEM1_13_SHORT_ARRAY);
                this.handler(InventoryPackets.this.itemArrayToClientHandler(Type.ITEM1_13_SHORT_ARRAY));
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_12_1.WINDOW_PROPERTY, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.SHORT);
                this.handler(wrapper -> {
                    short property = wrapper.get(Type.SHORT, 0);
                    if (property >= 4 && property <= 6) {
                        wrapper.set(Type.SHORT, 1, (short)((Protocol1_13To1_12_2)InventoryPackets.this.protocol).getMappingData().getEnchantmentMappings().getNewId(wrapper.get(Type.SHORT, 1).shortValue()));
                    }
                });
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    String channel = wrapper.get(Type.STRING, 0);
                    if (channel.equalsIgnoreCase("MC|StopSound")) {
                        String originalSource = wrapper.read(Type.STRING);
                        String originalSound = wrapper.read(Type.STRING);
                        wrapper.clearPacket();
                        wrapper.setPacketType(ClientboundPackets1_13.STOP_SOUND);
                        byte flags = 0;
                        wrapper.write(Type.BYTE, flags);
                        if (!originalSource.isEmpty()) {
                            flags = (byte)(flags | 1);
                            Optional<SoundSource> finalSource = SoundSource.findBySource(originalSource);
                            if (!finalSource.isPresent()) {
                                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                    Via.getPlatform().getLogger().info("Could not handle unknown sound source " + originalSource + " falling back to default: master");
                                }
                                finalSource = Optional.of(SoundSource.MASTER);
                            }
                            wrapper.write(Type.VAR_INT, finalSource.get().getId());
                        }
                        if (!originalSound.isEmpty()) {
                            flags = (byte)(flags | 2);
                            wrapper.write(Type.STRING, originalSound);
                        }
                        wrapper.set(Type.BYTE, 0, flags);
                        return;
                    }
                    if (channel.equalsIgnoreCase("MC|TrList")) {
                        channel = "minecraft:trader_list";
                        wrapper.passthrough(Type.INT);
                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE).shortValue();
                        for (int i = 0; i < size; ++i) {
                            Item input = wrapper.read(Type.ITEM1_8);
                            InventoryPackets.this.handleItemToClient(input);
                            wrapper.write(Type.ITEM1_13, input);
                            Item output = wrapper.read(Type.ITEM1_8);
                            InventoryPackets.this.handleItemToClient(output);
                            wrapper.write(Type.ITEM1_13, output);
                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN);
                            if (secondItem) {
                                Item second = wrapper.read(Type.ITEM1_8);
                                InventoryPackets.this.handleItemToClient(second);
                                wrapper.write(Type.ITEM1_13, second);
                            }
                            wrapper.passthrough(Type.BOOLEAN);
                            wrapper.passthrough(Type.INT);
                            wrapper.passthrough(Type.INT);
                        }
                    } else {
                        String old = channel;
                        if ((channel = InventoryPackets.getNewPluginChannelId(channel)) == null) {
                            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                                Via.getPlatform().getLogger().warning("Ignoring outgoing plugin message with channel: " + old);
                            }
                            wrapper.cancel();
                            return;
                        }
                        if (channel.equals("minecraft:register") || channel.equals("minecraft:unregister")) {
                            String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\u0000");
                            ArrayList<String> rewrittenChannels = new ArrayList<String>();
                            for (String s : channels) {
                                String rewritten = InventoryPackets.getNewPluginChannelId(s);
                                if (rewritten != null) {
                                    rewrittenChannels.add(rewritten);
                                    continue;
                                }
                                if (Via.getConfig().isSuppressConversionWarnings() && !Via.getManager().isDebug()) continue;
                                Via.getPlatform().getLogger().warning("Ignoring plugin channel in outgoing REGISTER: " + s);
                            }
                            if (!rewrittenChannels.isEmpty()) {
                                wrapper.write(Type.REMAINING_BYTES, Joiner.on('\u0000').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                            } else {
                                wrapper.cancel();
                                return;
                            }
                        }
                    }
                    wrapper.set(Type.STRING, 0, channel);
                });
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerClientbound(ClientboundPackets1_12_1.ENTITY_EQUIPMENT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.map(Type.ITEM1_8, Type.ITEM1_13);
                this.handler(InventoryPackets.this.itemToClientHandler(Type.ITEM1_13));
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerServerbound(ServerboundPackets1_13.CLICK_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map(Type.VAR_INT);
                this.map(Type.ITEM1_13, Type.ITEM1_8);
                this.handler(InventoryPackets.this.itemToServerHandler(Type.ITEM1_8));
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerServerbound(ServerboundPackets1_13.PLUGIN_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    String channel;
                    String old = channel = wrapper.get(Type.STRING, 0);
                    if ((channel = InventoryPackets.getOldPluginChannelId(channel)) == null) {
                        if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                            Via.getPlatform().getLogger().warning("Ignoring incoming plugin message with channel: " + old);
                        }
                        wrapper.cancel();
                        return;
                    }
                    if (channel.equals("REGISTER") || channel.equals("UNREGISTER")) {
                        String[] channels = new String(wrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8).split("\u0000");
                        ArrayList<String> rewrittenChannels = new ArrayList<String>();
                        for (String s : channels) {
                            String rewritten = InventoryPackets.getOldPluginChannelId(s);
                            if (rewritten != null) {
                                rewrittenChannels.add(rewritten);
                                continue;
                            }
                            if (Via.getConfig().isSuppressConversionWarnings() && !Via.getManager().isDebug()) continue;
                            Via.getPlatform().getLogger().warning("Ignoring plugin channel in incoming REGISTER: " + s);
                        }
                        wrapper.write(Type.REMAINING_BYTES, Joiner.on('\u0000').join(rewrittenChannels).getBytes(StandardCharsets.UTF_8));
                    }
                    wrapper.set(Type.STRING, 0, channel);
                });
            }
        });
        ((Protocol1_13To1_12_2)this.protocol).registerServerbound(ServerboundPackets1_13.CREATIVE_INVENTORY_ACTION, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.SHORT);
                this.map(Type.ITEM1_13, Type.ITEM1_8);
                this.handler(InventoryPackets.this.itemToServerHandler(Type.ITEM1_8));
            }
        });
    }

    @Override
    public Item handleItemToClient(Item item) {
        if (item == null) {
            return null;
        }
        CompoundTag tag = item.tag();
        int originalId = item.identifier() << 16 | item.data() & 0xFFFF;
        int rawId = item.identifier() << 4 | item.data() & 0xF;
        if (InventoryPackets.isDamageable(item.identifier())) {
            if (tag == null) {
                tag = new CompoundTag();
                item.setTag(tag);
            }
            tag.put("Damage", new IntTag(item.data()));
        }
        if (item.identifier() == 358) {
            if (tag == null) {
                tag = new CompoundTag();
                item.setTag(tag);
            }
            tag.put("map", new IntTag(item.data()));
        }
        if (tag != null) {
            ListTag canDestroyTag;
            ListTag canPlaceOnTag;
            ListTag storedEnch;
            Tag idTag;
            ListTag ench;
            StringTag name;
            CompoundTag display;
            CompoundTag blockEntityTag;
            boolean banner;
            boolean bl = banner = item.identifier() == 425;
            if ((banner || item.identifier() == 442) && (blockEntityTag = tag.getCompoundTag("BlockEntityTag")) != null) {
                ListTag patternsTag;
                NumberTag baseTag = blockEntityTag.getNumberTag("Base");
                if (baseTag != null) {
                    if (banner) {
                        rawId = 6800 + baseTag.asInt();
                    }
                    blockEntityTag.putInt("Base", 15 - baseTag.asInt());
                }
                if ((patternsTag = blockEntityTag.getListTag("Patterns")) != null) {
                    for (Tag pattern : patternsTag) {
                        CompoundTag patternTag;
                        NumberTag colorTag;
                        if (!(pattern instanceof CompoundTag) || (colorTag = (patternTag = (CompoundTag)pattern).getNumberTag("Color")) == null) continue;
                        patternTag.putInt("Color", 15 - colorTag.asInt());
                    }
                }
            }
            if ((display = tag.getCompoundTag("display")) != null && (name = display.getStringTag("Name")) != null) {
                display.putString(NBT_TAG_NAME + "|Name", name.getValue());
                name.setValue(ComponentUtil.legacyToJsonString(name.getValue(), true));
            }
            if ((ench = tag.getListTag("ench")) != null) {
                ListTag enchantments = new ListTag(CompoundTag.class);
                for (Object enchEntry : ench) {
                    CompoundTag entryTag;
                    if (!(enchEntry instanceof CompoundTag) || (idTag = (entryTag = (CompoundTag)enchEntry).getNumberTag("id")) == null) continue;
                    CompoundTag enchantmentEntry = new CompoundTag();
                    short oldId = ((NumberTag)idTag).asShort();
                    String newId = (String)Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                    if (newId == null) {
                        newId = "viaversion:legacy/" + oldId;
                    }
                    enchantmentEntry.putString("id", newId);
                    NumberTag levelTag = entryTag.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchantmentEntry.putShort("lvl", levelTag.asShort());
                    }
                    enchantments.add(enchantmentEntry);
                }
                tag.remove("ench");
                tag.put("Enchantments", enchantments);
            }
            if ((storedEnch = tag.getListTag("StoredEnchantments")) != null) {
                ListTag newStoredEnch = new ListTag(CompoundTag.class);
                for (Object enchEntry : storedEnch) {
                    CompoundTag entryTag;
                    NumberTag idTag2;
                    if (!(enchEntry instanceof CompoundTag) || (idTag2 = (entryTag = (CompoundTag)enchEntry).getNumberTag("id")) == null) continue;
                    CompoundTag enchantmentEntry = new CompoundTag();
                    short oldId = idTag2.asShort();
                    String newId = (String)Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().get(oldId);
                    if (newId == null) {
                        newId = "viaversion:legacy/" + oldId;
                    }
                    enchantmentEntry.putString("id", newId);
                    String[] levelTag = entryTag.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchantmentEntry.putShort("lvl", levelTag.asShort());
                    }
                    newStoredEnch.add(enchantmentEntry);
                }
                tag.put("StoredEnchantments", newStoredEnch);
            }
            if ((canPlaceOnTag = tag.getListTag("CanPlaceOn")) != null) {
                ListTag newCanPlaceOn = new ListTag(StringTag.class);
                tag.put(NBT_TAG_NAME + "|CanPlaceOn", canPlaceOnTag.copy());
                for (Object oldTag : canPlaceOnTag) {
                    String[] newValues;
                    Object value = ((Tag)oldTag).getValue();
                    String oldId = Key.stripMinecraftNamespace(value.toString());
                    String numberConverted = BlockIdData.numberIdToString.get(Ints.tryParse(oldId));
                    if (numberConverted != null) {
                        oldId = numberConverted;
                    }
                    if ((newValues = BlockIdData.blockIdMapping.get(oldId.toLowerCase(Locale.ROOT))) != null) {
                        for (String newValue : newValues) {
                            newCanPlaceOn.add(new StringTag(newValue));
                        }
                        continue;
                    }
                    newCanPlaceOn.add(new StringTag(oldId.toLowerCase(Locale.ROOT)));
                }
                tag.put("CanPlaceOn", newCanPlaceOn);
            }
            if ((canDestroyTag = tag.getListTag("CanDestroy")) != null) {
                ListTag newCanDestroy = new ListTag(StringTag.class);
                tag.put(NBT_TAG_NAME + "|CanDestroy", canDestroyTag.copy());
                for (Tag oldTag : canDestroyTag) {
                    String[] newValues;
                    Object value = oldTag.getValue();
                    String oldId = Key.stripMinecraftNamespace(value.toString());
                    String numberConverted = BlockIdData.numberIdToString.get(Ints.tryParse(oldId));
                    if (numberConverted != null) {
                        oldId = numberConverted;
                    }
                    if ((newValues = BlockIdData.blockIdMapping.get(oldId.toLowerCase(Locale.ROOT))) != null) {
                        for (String newValue : newValues) {
                            newCanDestroy.add(new StringTag(newValue));
                        }
                        continue;
                    }
                    newCanDestroy.add(new StringTag(oldId.toLowerCase(Locale.ROOT)));
                }
                tag.put("CanDestroy", newCanDestroy);
            }
            if (item.identifier() == 383) {
                CompoundTag entityTag = tag.getCompoundTag("EntityTag");
                if (entityTag != null) {
                    idTag = entityTag.getStringTag("id");
                    if (idTag != null) {
                        rawId = SpawnEggRewriter.getSpawnEggId(((StringTag)idTag).getValue());
                        if (rawId == -1) {
                            rawId = 25100288;
                        } else {
                            entityTag.remove("id");
                            if (entityTag.isEmpty()) {
                                tag.remove("EntityTag");
                            }
                        }
                    } else {
                        rawId = 25100288;
                    }
                } else {
                    rawId = 25100288;
                }
            }
            if (tag.isEmpty()) {
                tag = null;
                item.setTag(null);
            }
        }
        if (Protocol1_13To1_12_2.MAPPINGS.getItemMappings().getNewId(rawId) == -1) {
            if (!InventoryPackets.isDamageable(item.identifier()) && item.identifier() != 358) {
                if (tag == null) {
                    tag = new CompoundTag();
                    item.setTag(tag);
                }
                tag.put(NBT_TAG_NAME, new IntTag(originalId));
            }
            if (item.identifier() == 31 && item.data() == 0) {
                rawId = 512;
            } else if (Protocol1_13To1_12_2.MAPPINGS.getItemMappings().getNewId(rawId & 0xFFFFFFF0) != -1) {
                rawId &= 0xFFFFFFF0;
            } else {
                if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("Failed to get 1.13 item for " + item.identifier());
                }
                rawId = 16;
            }
        }
        item.setIdentifier(Protocol1_13To1_12_2.MAPPINGS.getItemMappings().getNewId(rawId));
        item.setData((short)0);
        return item;
    }

    public static String getNewPluginChannelId(String old) {
        switch (old) {
            case "MC|TrList": {
                return "minecraft:trader_list";
            }
            case "MC|Brand": {
                return "minecraft:brand";
            }
            case "MC|BOpen": {
                return "minecraft:book_open";
            }
            case "MC|DebugPath": {
                return "minecraft:debug/paths";
            }
            case "MC|DebugNeighborsUpdate": {
                return "minecraft:debug/neighbors_update";
            }
            case "REGISTER": {
                return "minecraft:register";
            }
            case "UNREGISTER": {
                return "minecraft:unregister";
            }
            case "BungeeCord": {
                return "bungeecord:main";
            }
            case "bungeecord:main": {
                return null;
            }
        }
        String mappedChannel = (String)Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().get(old);
        if (mappedChannel != null) {
            return mappedChannel;
        }
        return MappingData.validateNewChannel(old);
    }

    @Override
    public Item handleItemToServer(Item item) {
        int oldId;
        NumberTag viaTag;
        if (item == null) {
            return null;
        }
        Integer rawId = null;
        boolean gotRawIdFromTag = false;
        CompoundTag tag = item.tag();
        if (tag != null && (viaTag = tag.getNumberTag(NBT_TAG_NAME)) != null) {
            rawId = viaTag.asInt();
            tag.remove(NBT_TAG_NAME);
            gotRawIdFromTag = true;
        }
        if (rawId == null && (oldId = Protocol1_13To1_12_2.MAPPINGS.getItemMappings().inverse().getNewId(item.identifier())) != -1) {
            Optional<String> eggEntityId = SpawnEggRewriter.getEntityId(oldId);
            if (eggEntityId.isPresent()) {
                rawId = 25100288;
                if (tag == null) {
                    tag = new CompoundTag();
                    item.setTag(tag);
                }
                if (!tag.contains("EntityTag")) {
                    CompoundTag entityTag = new CompoundTag();
                    entityTag.put("id", new StringTag(eggEntityId.get()));
                    tag.put("EntityTag", entityTag);
                }
            } else {
                rawId = oldId >> 4 << 16 | oldId & 0xF;
            }
        }
        if (rawId == null) {
            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Failed to get 1.12 item for " + item.identifier());
            }
            rawId = 65536;
        }
        item.setIdentifier((short)(rawId >> 16));
        item.setData((short)(rawId & 0xFFFF));
        if (tag != null) {
            String[] newValues;
            Object value;
            ListTag old;
            ListTag storedEnch;
            ListTag enchantments;
            StringTag name;
            CompoundTag display;
            CompoundTag blockEntityTag;
            NumberTag mapTag;
            NumberTag damageTag;
            if (InventoryPackets.isDamageable(item.identifier()) && (damageTag = tag.getNumberTag("Damage")) != null) {
                if (!gotRawIdFromTag) {
                    item.setData(damageTag.asShort());
                }
                tag.remove("Damage");
            }
            if (item.identifier() == 358 && (mapTag = tag.getNumberTag("map")) != null) {
                if (!gotRawIdFromTag) {
                    item.setData(mapTag.asShort());
                }
                tag.remove("map");
            }
            if ((item.identifier() == 442 || item.identifier() == 425) && (blockEntityTag = tag.getCompoundTag("BlockEntityTag")) != null) {
                ListTag patternsTag;
                NumberTag baseTag = blockEntityTag.getNumberTag("Base");
                if (baseTag != null) {
                    blockEntityTag.putInt("Base", 15 - baseTag.asInt());
                }
                if ((patternsTag = blockEntityTag.getListTag("Patterns")) != null) {
                    for (Tag pattern : patternsTag) {
                        if (!(pattern instanceof CompoundTag)) continue;
                        CompoundTag patternTag = (CompoundTag)pattern;
                        NumberTag colorTag = patternTag.getNumberTag("Color");
                        patternTag.putInt("Color", 15 - colorTag.asInt());
                    }
                }
            }
            if ((display = tag.getCompoundTag("display")) != null && (name = display.getStringTag("Name")) != null) {
                Object via = display.remove(NBT_TAG_NAME + "|Name");
                name.setValue(via instanceof StringTag ? (String)((Tag)via).getValue() : ComponentUtil.jsonToLegacy(name.getValue()));
            }
            if ((enchantments = tag.getListTag("Enchantments")) != null) {
                ListTag ench = new ListTag(CompoundTag.class);
                for (Object enchantmentEntry : enchantments) {
                    CompoundTag entryTag;
                    StringTag idTag;
                    if (!(enchantmentEntry instanceof CompoundTag) || (idTag = (entryTag = (CompoundTag)enchantmentEntry).getStringTag("id")) == null) continue;
                    CompoundTag enchEntry = new CompoundTag();
                    String newId = idTag.getValue();
                    Short oldId2 = (Short)Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                    if (oldId2 == null && newId.startsWith("viaversion:legacy/")) {
                        oldId2 = Short.valueOf(newId.substring(18));
                    }
                    if (oldId2 == null) continue;
                    enchEntry.putShort("id", oldId2);
                    NumberTag levelTag = entryTag.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchEntry.putShort("lvl", levelTag.asShort());
                    }
                    ench.add(enchEntry);
                }
                tag.remove("Enchantments");
                tag.put("ench", ench);
            }
            if ((storedEnch = tag.getListTag("StoredEnchantments")) != null) {
                ListTag newStoredEnch = new ListTag(CompoundTag.class);
                for (Tag enchantmentEntry : storedEnch) {
                    CompoundTag entryTag;
                    StringTag idTag;
                    if (!(enchantmentEntry instanceof CompoundTag) || (idTag = (entryTag = (CompoundTag)enchantmentEntry).getStringTag("id")) == null) continue;
                    CompoundTag enchEntry = new CompoundTag();
                    String newId = idTag.getValue();
                    Short oldId3 = (Short)Protocol1_13To1_12_2.MAPPINGS.getOldEnchantmentsIds().inverse().get(newId);
                    if (oldId3 == null && newId.startsWith("viaversion:legacy/")) {
                        oldId3 = Short.valueOf(newId.substring(18));
                    }
                    if (oldId3 == null) continue;
                    enchEntry.putShort("id", oldId3);
                    NumberTag levelTag = entryTag.getNumberTag("lvl");
                    if (levelTag != null) {
                        enchEntry.putShort("lvl", levelTag.asShort());
                    }
                    newStoredEnch.add(enchEntry);
                }
                tag.put("StoredEnchantments", newStoredEnch);
            }
            if (tag.getListTag(NBT_TAG_NAME + "|CanPlaceOn") != null) {
                tag.put("CanPlaceOn", tag.remove(NBT_TAG_NAME + "|CanPlaceOn"));
            } else if (tag.getListTag("CanPlaceOn") != null) {
                old = tag.getListTag("CanPlaceOn");
                ListTag newCanPlaceOn = new ListTag(StringTag.class);
                for (Tag oldTag : old) {
                    value = oldTag.getValue();
                    newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String ? Key.stripMinecraftNamespace((String)value) : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanPlaceOn.add(new StringTag(newValue));
                        }
                        continue;
                    }
                    newCanPlaceOn.add(oldTag);
                }
                tag.put("CanPlaceOn", newCanPlaceOn);
            }
            if (tag.getListTag(NBT_TAG_NAME + "|CanDestroy") != null) {
                tag.put("CanDestroy", tag.remove(NBT_TAG_NAME + "|CanDestroy"));
            } else if (tag.getListTag("CanDestroy") != null) {
                old = tag.getListTag("CanDestroy");
                ListTag newCanDestroy = new ListTag(StringTag.class);
                for (Tag oldTag : old) {
                    value = oldTag.getValue();
                    newValues = BlockIdData.fallbackReverseMapping.get(value instanceof String ? Key.stripMinecraftNamespace((String)value) : null);
                    if (newValues != null) {
                        for (String newValue : newValues) {
                            newCanDestroy.add(new StringTag(newValue));
                        }
                        continue;
                    }
                    newCanDestroy.add(oldTag);
                }
                tag.put("CanDestroy", newCanDestroy);
            }
        }
        return item;
    }

    public static String getOldPluginChannelId(String newId) {
        if ((newId = MappingData.validateNewChannel(newId)) == null) {
            return null;
        }
        switch (newId) {
            case "minecraft:trader_list": {
                return "MC|TrList";
            }
            case "minecraft:book_open": {
                return "MC|BOpen";
            }
            case "minecraft:debug/paths": {
                return "MC|DebugPath";
            }
            case "minecraft:debug/neighbors_update": {
                return "MC|DebugNeighborsUpdate";
            }
            case "minecraft:register": {
                return "REGISTER";
            }
            case "minecraft:unregister": {
                return "UNREGISTER";
            }
            case "minecraft:brand": {
                return "MC|Brand";
            }
            case "bungeecord:main": {
                return "BungeeCord";
            }
        }
        String mappedChannel = (String)Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().inverse().get(newId);
        if (mappedChannel != null) {
            return mappedChannel;
        }
        return newId.length() > 20 ? newId.substring(0, 20) : newId;
    }

    public static boolean isDamageable(int id) {
        return id >= 256 && id <= 259 || id == 261 || id >= 267 && id <= 279 || id >= 283 && id <= 286 || id >= 290 && id <= 294 || id >= 298 && id <= 317 || id == 346 || id == 359 || id == 398 || id == 442 || id == 443;
    }
}

