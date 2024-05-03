/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.optifine.DynamicLight;

public class DynamicLightsMap {
    private Map<Integer, DynamicLight> map = new HashMap<Integer, DynamicLight>();
    private List<DynamicLight> list = new ArrayList<DynamicLight>();
    private boolean dirty = false;

    public DynamicLight put(int id, DynamicLight dynamicLight) {
        DynamicLight dynamiclight = this.map.put(id, dynamicLight);
        this.setDirty();
        return dynamiclight;
    }

    public DynamicLight get(int id) {
        return this.map.get(id);
    }

    public int size() {
        return this.map.size();
    }

    public DynamicLight remove(int id) {
        DynamicLight dynamiclight = this.map.remove(id);
        if (dynamiclight != null) {
            this.setDirty();
        }
        return dynamiclight;
    }

    public void clear() {
        this.map.clear();
        this.list.clear();
        this.setDirty();
    }

    private void setDirty() {
        this.dirty = true;
    }

    public List<DynamicLight> valueList() {
        if (this.dirty) {
            this.list.clear();
            this.list.addAll(this.map.values());
            this.dirty = false;
        }
        return this.list;
    }
}

