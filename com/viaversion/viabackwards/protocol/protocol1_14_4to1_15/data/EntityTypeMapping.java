/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viabackwards.protocol.protocol1_14_4to1_15.data;

import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;

public class EntityTypeMapping {
    public static int getOldEntityId(int entityId) {
        if (entityId == 4) {
            return EntityTypes1_14.PUFFERFISH.getId();
        }
        return entityId >= 5 ? entityId - 1 : entityId;
    }
}

