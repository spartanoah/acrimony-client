/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata;

import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;
import java.util.UUID;

public class MetadataRewriter1_9To1_8
extends EntityRewriter<ClientboundPackets1_8, Protocol1_9To1_8> {
    public MetadataRewriter1_9To1_8(Protocol1_9To1_8 protocol) {
        super(protocol);
    }

    @Override
    protected void registerRewrites() {
        this.filter().handler(this::handleMetadata);
    }

    private void handleMetadata(MetaHandlerEvent event, Metadata metadata) {
        EntityType type = event.entityType();
        MetaIndex metaIndex = MetaIndex.searchIndex(type, metadata.id());
        if (metaIndex == null) {
            event.cancel();
            return;
        }
        if (metaIndex.getNewType() == null) {
            event.cancel();
            return;
        }
        metadata.setId(metaIndex.getNewIndex());
        metadata.setMetaTypeUnsafe(metaIndex.getNewType());
        Object value = metadata.getValue();
        switch (metaIndex.getNewType()) {
            case Byte: {
                if (metaIndex.getOldType() == MetaType1_8.Byte) {
                    metadata.setValue(value);
                }
                if (metaIndex.getOldType() == MetaType1_8.Int) {
                    metadata.setValue(((Integer)value).byteValue());
                }
                if (metaIndex != MetaIndex.ENTITY_STATUS || type != EntityTypes1_10.EntityType.PLAYER) break;
                byte val2 = 0;
                if (((Byte)value & 0x10) == 16) {
                    val2 = 1;
                }
                int newIndex = MetaIndex.PLAYER_HAND.getNewIndex();
                MetaType1_9 metaType = MetaIndex.PLAYER_HAND.getNewType();
                event.createExtraMeta(new Metadata(newIndex, metaType, val2));
                break;
            }
            case OptUUID: {
                String owner = (String)value;
                UUID toWrite = null;
                if (!owner.isEmpty()) {
                    try {
                        toWrite = UUID.fromString(owner);
                    } catch (Exception metaType) {
                        // empty catch block
                    }
                }
                metadata.setValue(toWrite);
                break;
            }
            case VarInt: {
                if (metaIndex.getOldType() == MetaType1_8.Byte) {
                    metadata.setValue(((Byte)value).intValue());
                }
                if (metaIndex.getOldType() == MetaType1_8.Short) {
                    metadata.setValue(((Short)value).intValue());
                }
                if (metaIndex.getOldType() != MetaType1_8.Int) break;
                metadata.setValue(value);
                break;
            }
            case Float: 
            case String: {
                metadata.setValue(value);
                break;
            }
            case Boolean: {
                if (metaIndex == MetaIndex.AGEABLE_AGE) {
                    metadata.setValue((Byte)value < 0);
                    break;
                }
                metadata.setValue((Byte)value != 0);
                break;
            }
            case Slot: {
                metadata.setValue(value);
                ItemRewriter.toClient((Item)metadata.getValue());
                break;
            }
            case Position: {
                Vector vector = (Vector)value;
                metadata.setValue(vector);
                break;
            }
            case Vector3F: {
                EulerAngle angle = (EulerAngle)value;
                metadata.setValue(angle);
                break;
            }
            case Chat: {
                value = Protocol1_9To1_8.fixJson(value.toString());
                metadata.setValue(value);
                break;
            }
            case BlockID: {
                metadata.setValue(((Number)value).intValue());
                break;
            }
            default: {
                throw new RuntimeException("Unhandled MetaDataType: " + metaIndex.getNewType());
            }
        }
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_10.getTypeFromId(type, false);
    }

    @Override
    public EntityType objectTypeFromId(int type) {
        return EntityTypes1_10.getTypeFromId(type, true);
    }
}

