/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.packets;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_15;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_16;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.LongArrayTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.CompactArrayUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.Map;
import java.util.UUID;

public class WorldPackets {
    public static void register(Protocol1_16To1_15_2 protocol) {
        BlockRewriter<ClientboundPackets1_15> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_15.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_15.BLOCK_CHANGE);
        blockRewriter.registerMultiBlockChange(ClientboundPackets1_15.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_15.ACKNOWLEDGE_PLAYER_DIGGING);
        protocol.registerClientbound(ClientboundPackets1_15.UPDATE_LIGHT, new PacketHandlers(){

            @Override
            public void register() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.handler(wrapper -> wrapper.write(Type.BOOLEAN, true));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_15.CHUNK_DATA, wrapper -> {
            Chunk chunk = wrapper.read(ChunkType1_15.TYPE);
            wrapper.write(ChunkType1_16.TYPE, chunk);
            chunk.setIgnoreOldLightData(chunk.isFullChunk());
            for (int s = 0; s < chunk.getSections().length; ++s) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) continue;
                DataPalette palette = section.palette(PaletteType.BLOCKS);
                for (int i2 = 0; i2 < palette.size(); ++i2) {
                    int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(palette.idByIndex(i2));
                    palette.setIdByIndex(i2, mappedBlockStateId);
                }
            }
            CompoundTag heightMaps = chunk.getHeightMap();
            for (Tag heightMapTag : heightMaps.values()) {
                LongArrayTag heightMap = (LongArrayTag)heightMapTag;
                int[] heightMapData = new int[256];
                CompactArrayUtil.iterateCompactArray(9, heightMapData.length, heightMap.getValue(), (i, v) -> {
                    heightMapData[i] = v;
                });
                heightMap.setValue(CompactArrayUtil.createCompactArrayWithPadding(9, heightMapData.length, i -> heightMapData[i]));
            }
            if (chunk.getBlockEntities() == null) {
                return;
            }
            for (CompoundTag blockEntity : chunk.getBlockEntities()) {
                WorldPackets.handleBlockEntity(protocol, blockEntity);
            }
        });
        protocol.registerClientbound(ClientboundPackets1_15.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14);
            wrapper.passthrough(Type.UNSIGNED_BYTE);
            CompoundTag tag = wrapper.passthrough(Type.NAMED_COMPOUND_TAG);
            WorldPackets.handleBlockEntity(protocol, tag);
        });
        blockRewriter.registerEffect(ClientboundPackets1_15.EFFECT, 1010, 2001);
    }

    private static void handleBlockEntity(Protocol1_16To1_15_2 protocol, CompoundTag compoundTag) {
        StringTag spawnDataIdTag;
        CompoundTag spawnDataTag;
        StringTag idTag = compoundTag.getStringTag("id");
        if (idTag == null) {
            return;
        }
        String id = idTag.getValue();
        if (id.equals("minecraft:conduit")) {
            Object targetUuidTag = compoundTag.remove("target_uuid");
            if (!(targetUuidTag instanceof StringTag)) {
                return;
            }
            UUID targetUuid = UUID.fromString((String)((Tag)targetUuidTag).getValue());
            compoundTag.put("Target", new IntArrayTag(UUIDUtil.toIntArray(targetUuid)));
        } else if (id.equals("minecraft:skull") && compoundTag.getCompoundTag("Owner") != null) {
            CompoundTag ownerTag = (CompoundTag)compoundTag.remove("Owner");
            Object ownerUuidTag = ownerTag.remove("Id");
            if (ownerUuidTag instanceof StringTag) {
                UUID ownerUuid = UUID.fromString(((StringTag)ownerUuidTag).getValue());
                ownerTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(ownerUuid)));
            }
            CompoundTag skullOwnerTag = new CompoundTag();
            for (Map.Entry<String, Tag> entry : ownerTag.entrySet()) {
                skullOwnerTag.put(entry.getKey(), entry.getValue());
            }
            compoundTag.put("SkullOwner", skullOwnerTag);
        } else if (id.equals("minecraft:sign")) {
            for (int i = 1; i <= 4; ++i) {
                StringTag line = compoundTag.getStringTag("Text" + i);
                if (line == null) continue;
                JsonElement text = protocol.getComponentRewriter().processText(line.getValue());
                compoundTag.putString("Text" + i, text.toString());
            }
        } else if (id.equals("minecraft:mob_spawner") && (spawnDataTag = compoundTag.getCompoundTag("SpawnData")) != null && (spawnDataIdTag = spawnDataTag.getStringTag("id")) != null && spawnDataIdTag.getValue().equals("minecraft:zombie_pigman")) {
            spawnDataIdTag.setValue("minecraft:zombified_piglin");
        }
    }
}

