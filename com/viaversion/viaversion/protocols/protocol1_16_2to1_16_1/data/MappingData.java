/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.data;

import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.util.HashMap;
import java.util.Map;

public class MappingData
extends MappingDataBase {
    private final Map<String, CompoundTag> dimensionDataMap = new HashMap<String, CompoundTag>();
    private CompoundTag dimensionRegistry;

    public MappingData() {
        super("1.16", "1.16.2");
    }

    @Override
    public void loadExtras(CompoundTag data) {
        this.dimensionRegistry = MappingDataLoader.loadNBTFromFile("dimension-registry-1.16.2.nbt");
        ListTag dimensions = (ListTag)this.dimensionRegistry.getCompoundTag("minecraft:dimension_type").get("value");
        for (Tag dimension : dimensions) {
            CompoundTag dimensionCompound = (CompoundTag)dimension;
            CompoundTag dimensionData = dimensionCompound.getCompoundTag("element").copy();
            this.dimensionDataMap.put(dimensionCompound.getStringTag("name").getValue(), dimensionData);
        }
    }

    public Map<String, CompoundTag> getDimensionDataMap() {
        return this.dimensionDataMap;
    }

    public CompoundTag getDimensionRegistry() {
        return this.dimensionRegistry.copy();
    }
}

