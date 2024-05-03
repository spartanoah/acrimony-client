/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.data;

import com.viaversion.viaversion.api.data.Mappings;
import java.util.Arrays;

public class IntArrayMappings
implements Mappings {
    private final int[] mappings;
    private final int mappedIds;

    protected IntArrayMappings(int[] mappings, int mappedIds) {
        this.mappings = mappings;
        this.mappedIds = mappedIds;
    }

    public static IntArrayMappings of(int[] mappings, int mappedIds) {
        return new IntArrayMappings(mappings, mappedIds);
    }

    @Override
    public int getNewId(int id) {
        return id >= 0 && id < this.mappings.length ? this.mappings[id] : -1;
    }

    @Override
    public void setNewId(int id, int mappedId) {
        this.mappings[id] = mappedId;
    }

    @Override
    public int size() {
        return this.mappings.length;
    }

    @Override
    public int mappedSize() {
        return this.mappedIds;
    }

    @Override
    public Mappings inverse() {
        int[] inverse = new int[this.mappedIds];
        Arrays.fill(inverse, -1);
        for (int id = 0; id < this.mappings.length; ++id) {
            int mappedId = this.mappings[id];
            if (mappedId == -1 || inverse[mappedId] != -1) continue;
            inverse[mappedId] = id;
        }
        return IntArrayMappings.of(inverse, this.mappings.length);
    }

    public int[] raw() {
        return this.mappings;
    }
}

