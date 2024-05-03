/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;
import com.viaversion.viaversion.util.Pair;
import java.util.HashMap;
import java.util.Optional;

public class MetaIndex1_8to1_9 {
    private static final HashMap<Pair<EntityTypes1_10.EntityType, Integer>, MetaIndex> metadataRewrites = new HashMap();

    private static Optional<MetaIndex> getIndex(EntityTypes1_10.EntityType type, int index) {
        Pair<EntityTypes1_10.EntityType, Integer> pair = new Pair<EntityTypes1_10.EntityType, Integer>(type, index);
        if (metadataRewrites.containsKey(pair)) {
            return Optional.of(metadataRewrites.get(pair));
        }
        return Optional.empty();
    }

    public static MetaIndex searchIndex(EntityTypes1_10.EntityType type, int index) {
        EntityTypes1_10.EntityType currentType = type;
        do {
            Optional<MetaIndex> optMeta;
            if (!(optMeta = MetaIndex1_8to1_9.getIndex(currentType, index)).isPresent()) continue;
            return optMeta.get();
        } while ((currentType = currentType.getParent()) != null);
        return null;
    }

    static {
        for (MetaIndex index : MetaIndex.values()) {
            metadataRewrites.put(new Pair<EntityTypes1_10.EntityType, Integer>(index.getClazz(), index.getNewIndex()), index);
        }
    }
}

