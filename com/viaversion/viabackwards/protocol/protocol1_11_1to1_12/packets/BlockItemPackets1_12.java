/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_11_1to1_12.packets;

import com.viaversion.viabackwards.api.rewriters.LegacyBlockItemRewriter;
import com.viaversion.viabackwards.protocol.protocol1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viabackwards.protocol.protocol1_11_1to1_12.data.MapColorMapping;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ClientboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import java.util.Iterator;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockItemPackets1_12
extends LegacyBlockItemRewriter<ClientboundPackets1_12, ServerboundPackets1_9_3, Protocol1_11_1To1_12> {
    public BlockItemPackets1_12(Protocol1_11_1To1_12 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        ((Protocol1_11_1To1_12)this.protocol).registerClientbound(ClientboundPackets1_12.MAP_DATA, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.BYTE);
                this.map(Type.BOOLEAN);
                this.handler(wrapper -> {
                    int count = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < count * 3; ++i) {
                        wrapper.passthrough(Type.BYTE);
                    }
                });
                this.handler(wrapper -> {
                    short columns = wrapper.passthrough(Type.UNSIGNED_BYTE);
                    if (columns <= 0) {
                        return;
                    }
                    wrapper.passthrough(Type.UNSIGNED_BYTE);
                    wrapper.passthrough(Type.UNSIGNED_BYTE);
                    wrapper.passthrough(Type.UNSIGNED_BYTE);
                    byte[] data = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                    for (int i = 0; i < data.length; ++i) {
                        short color = (short)(data[i] & 0xFF);
                        if (color <= 143) continue;
                        color = (short)MapColorMapping.getNearestOldColor(color);
                        data[i] = (byte)color;
                    }
                    wrapper.write(Type.BYTE_ARRAY_PRIMITIVE, data);
                });
            }
        });
        this.registerSetSlot(ClientboundPackets1_12.SET_SLOT);
        this.registerWindowItems(ClientboundPackets1_12.WINDOW_ITEMS);
        this.registerEntityEquipment(ClientboundPackets1_12.ENTITY_EQUIPMENT);
        ((Protocol1_11_1To1_12)this.protocol).registerClientbound(ClientboundPackets1_12.PLUGIN_MESSAGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.STRING);
                this.handler(wrapper -> {
                    if (wrapper.get(Type.STRING, 0).equalsIgnoreCase("MC|TrList")) {
                        wrapper.passthrough(Type.INT);
                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE).shortValue();
                        for (int i = 0; i < size; ++i) {
                            wrapper.write(Type.ITEM1_8, BlockItemPackets1_12.this.handleItemToClient(wrapper.read(Type.ITEM1_8)));
                            wrapper.write(Type.ITEM1_8, BlockItemPackets1_12.this.handleItemToClient(wrapper.read(Type.ITEM1_8)));
                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN);
                            if (secondItem) {
                                wrapper.write(Type.ITEM1_8, BlockItemPackets1_12.this.handleItemToClient(wrapper.read(Type.ITEM1_8)));
                            }
                            wrapper.passthrough(Type.BOOLEAN);
                            wrapper.passthrough(Type.INT);
                            wrapper.passthrough(Type.INT);
                        }
                    }
                });
            }
        });
        ((Protocol1_11_1To1_12)this.protocol).registerServerbound(ServerboundPackets1_9_3.CLICK_WINDOW, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.SHORT);
                this.map(Type.BYTE);
                this.map(Type.SHORT);
                this.map(Type.VAR_INT);
                this.map(Type.ITEM);
                this.handler(wrapper -> {
                    Item item = wrapper.get(Type.ITEM, 0);
                    BlockItemPackets1_12.this.handleItemToServer(item);
                });
            }
        });
        this.registerCreativeInvAction(ServerboundPackets1_9_3.CREATIVE_INVENTORY_ACTION);
        ((Protocol1_11_1To1_12)this.protocol).registerClientbound(ClientboundPackets1_12.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            ChunkType1_9_3 type = ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment());
            Chunk chunk = wrapper.passthrough(type);
            this.handleChunk(chunk);
        });
        ((Protocol1_11_1To1_12)this.protocol).registerClientbound(ClientboundPackets1_12.BLOCK_CHANGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.POSITION1_8);
                this.map(Type.VAR_INT);
                this.handler(wrapper -> {
                    int idx = wrapper.get(Type.VAR_INT, 0);
                    wrapper.set(Type.VAR_INT, 0, BlockItemPackets1_12.this.handleBlockID(idx));
                });
            }
        });
        ((Protocol1_11_1To1_12)this.protocol).registerClientbound(ClientboundPackets1_12.MULTI_BLOCK_CHANGE, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.INT);
                this.map(Type.INT);
                this.map(Type.BLOCK_CHANGE_RECORD_ARRAY);
                this.handler(wrapper -> {
                    for (BlockChangeRecord record : wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                        record.setBlockId(BlockItemPackets1_12.this.handleBlockID(record.getBlockId()));
                    }
                });
            }
        });
        ((Protocol1_11_1To1_12)this.protocol).registerClientbound(ClientboundPackets1_12.BLOCK_ENTITY_DATA, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.POSITION1_8);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.NAMED_COMPOUND_TAG);
                this.handler(wrapper -> {
                    if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 11) {
                        wrapper.cancel();
                    }
                });
            }
        });
        ((Protocol1_11_1To1_12)this.protocol).getEntityRewriter().filter().handler((event, meta) -> {
            if (meta.metaType().type().equals(Type.ITEM1_8)) {
                meta.setValue(this.handleItemToClient((Item)meta.getValue()));
            }
        });
        ((Protocol1_11_1To1_12)this.protocol).registerServerbound(ServerboundPackets1_9_3.CLIENT_STATUS, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.handler(wrapper -> {
                    if (wrapper.get(Type.VAR_INT, 0) == 2) {
                        wrapper.cancel();
                    }
                });
            }
        });
    }

    @Override
    public @Nullable Item handleItemToClient(Item item) {
        if (item == null) {
            return null;
        }
        super.handleItemToClient(item);
        if (item.tag() != null) {
            CompoundTag backupTag = new CompoundTag();
            if (this.handleNbtToClient(item.tag(), backupTag)) {
                item.tag().put("Via|LongArrayTags", backupTag);
            }
        }
        return item;
    }

    private boolean handleNbtToClient(CompoundTag compoundTag, CompoundTag backupTag) {
        Iterator<Map.Entry<String, Tag>> iterator = compoundTag.iterator();
        boolean hasLongArrayTag = false;
        while (iterator.hasNext()) {
            Map.Entry<String, Tag> entry = iterator.next();
            if (entry.getValue() instanceof CompoundTag) {
                CompoundTag nestedBackupTag = new CompoundTag();
                backupTag.put(entry.getKey(), nestedBackupTag);
                hasLongArrayTag |= this.handleNbtToClient((CompoundTag)entry.getValue(), nestedBackupTag);
                continue;
            }
            if (!(entry.getValue() instanceof LongArrayTag)) continue;
            backupTag.put(entry.getKey(), this.fromLongArrayTag((LongArrayTag)entry.getValue()));
            iterator.remove();
            hasLongArrayTag = true;
        }
        return hasLongArrayTag;
    }

    @Override
    public @Nullable Item handleItemToServer(Item item) {
        Object tag;
        if (item == null) {
            return null;
        }
        super.handleItemToServer(item);
        if (item.tag() != null && (tag = item.tag().remove("Via|LongArrayTags")) instanceof CompoundTag) {
            this.handleNbtToServer(item.tag(), (CompoundTag)tag);
        }
        return item;
    }

    private void handleNbtToServer(CompoundTag compoundTag, CompoundTag backupTag) {
        for (Map.Entry<String, Tag> entry : backupTag) {
            if (entry.getValue() instanceof CompoundTag) {
                CompoundTag nestedTag = (CompoundTag)compoundTag.get(entry.getKey());
                this.handleNbtToServer(nestedTag, (CompoundTag)entry.getValue());
                continue;
            }
            compoundTag.put(entry.getKey(), this.fromIntArrayTag((IntArrayTag)entry.getValue()));
        }
    }

    private IntArrayTag fromLongArrayTag(LongArrayTag tag) {
        int[] intArray = new int[tag.length() * 2];
        long[] longArray = tag.getValue();
        int i = 0;
        for (long l : longArray) {
            intArray[i++] = (int)(l >> 32);
            intArray[i++] = (int)l;
        }
        return new IntArrayTag(intArray);
    }

    private LongArrayTag fromIntArrayTag(IntArrayTag tag) {
        long[] longArray = new long[tag.length() / 2];
        int[] intArray = tag.getValue();
        int i = 0;
        int j = 0;
        while (i < intArray.length) {
            longArray[j] = (long)intArray[i] << 32 | (long)intArray[i + 1] & 0xFFFFFFFFL;
            i += 2;
            ++j;
        }
        return new LongArrayTag(longArray);
    }
}

