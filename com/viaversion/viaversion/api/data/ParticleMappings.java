/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.data;

import com.viaversion.viaversion.api.data.FullMappingsBase;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import java.util.List;

public class ParticleMappings
extends FullMappingsBase {
    private final IntList itemParticleIds = new IntArrayList(4);
    private final IntList blockParticleIds = new IntArrayList(4);

    public ParticleMappings(List<String> unmappedIdentifiers, List<String> mappedIdentifiers, Mappings mappings) {
        super(unmappedIdentifiers, mappedIdentifiers, mappings);
        this.addBlockParticle("block");
        this.addBlockParticle("falling_dust");
        this.addBlockParticle("block_marker");
        this.addItemParticle("item");
    }

    public boolean addItemParticle(String identifier) {
        int id = this.id(identifier);
        return id != -1 && this.itemParticleIds.add(id);
    }

    public boolean addBlockParticle(String identifier) {
        int id = this.id(identifier);
        return id != -1 && this.blockParticleIds.add(id);
    }

    public boolean isBlockParticle(int id) {
        return this.blockParticleIds.contains(id);
    }

    public boolean isItemParticle(int id) {
        return this.itemParticleIds.contains(id);
    }
}

