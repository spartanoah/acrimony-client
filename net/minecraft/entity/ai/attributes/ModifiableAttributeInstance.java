/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class ModifiableAttributeInstance
implements IAttributeInstance {
    private final BaseAttributeMap attributeMap;
    private final IAttribute genericAttribute;
    private final Map<Integer, Set<AttributeModifier>> mapByOperation = Maps.newHashMap();
    private final Map<String, Set<AttributeModifier>> mapByName = Maps.newHashMap();
    private final Map<UUID, AttributeModifier> mapByUUID = Maps.newHashMap();
    private double baseValue;
    private boolean needsUpdate = true;
    private double cachedValue;

    public ModifiableAttributeInstance(BaseAttributeMap attributeMapIn, IAttribute genericAttributeIn) {
        this.attributeMap = attributeMapIn;
        this.genericAttribute = genericAttributeIn;
        this.baseValue = genericAttributeIn.getDefaultValue();
        for (int i = 0; i < 3; ++i) {
            this.mapByOperation.put(i, Sets.newHashSet());
        }
    }

    @Override
    public IAttribute getAttribute() {
        return this.genericAttribute;
    }

    @Override
    public double getBaseValue() {
        return this.baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        if (baseValue != this.getBaseValue()) {
            this.baseValue = baseValue;
            this.flagForUpdate();
        }
    }

    @Override
    public Collection<AttributeModifier> getModifiersByOperation(int operation) {
        return this.mapByOperation.get(operation);
    }

    @Override
    public Collection<AttributeModifier> func_111122_c() {
        HashSet<AttributeModifier> set = Sets.newHashSet();
        for (int i = 0; i < 3; ++i) {
            set.addAll(this.getModifiersByOperation(i));
        }
        return set;
    }

    @Override
    public AttributeModifier getModifier(UUID uuid) {
        return this.mapByUUID.get(uuid);
    }

    @Override
    public boolean hasModifier(AttributeModifier modifier) {
        return this.mapByUUID.get(modifier.getID()) != null;
    }

    @Override
    public void applyModifier(AttributeModifier modifier) {
        if (this.getModifier(modifier.getID()) != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        Set<AttributeModifier> set = this.mapByName.get(modifier.getName());
        if (set == null) {
            set = Sets.newHashSet();
            this.mapByName.put(modifier.getName(), set);
        }
        this.mapByOperation.get(modifier.getOperation()).add(modifier);
        set.add(modifier);
        this.mapByUUID.put(modifier.getID(), modifier);
        this.flagForUpdate();
    }

    protected void flagForUpdate() {
        this.needsUpdate = true;
        this.attributeMap.func_180794_a(this);
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        for (int i = 0; i < 3; ++i) {
            Set<AttributeModifier> set = this.mapByOperation.get(i);
            set.remove(modifier);
        }
        Set<AttributeModifier> set1 = this.mapByName.get(modifier.getName());
        if (set1 != null) {
            set1.remove(modifier);
            if (set1.isEmpty()) {
                this.mapByName.remove(modifier.getName());
            }
        }
        this.mapByUUID.remove(modifier.getID());
        this.flagForUpdate();
    }

    @Override
    public void removeAllModifiers() {
        Collection<AttributeModifier> collection = this.func_111122_c();
        if (collection != null) {
            for (AttributeModifier attributemodifier : Lists.newArrayList(collection)) {
                this.removeModifier(attributemodifier);
            }
        }
    }

    @Override
    public double getAttributeValue() {
        if (this.needsUpdate) {
            this.cachedValue = this.computeValue();
            this.needsUpdate = false;
        }
        return this.cachedValue;
    }

    private double computeValue() {
        double d0 = this.getBaseValue();
        for (AttributeModifier attributemodifier : this.func_180375_b(0)) {
            d0 += attributemodifier.getAmount();
        }
        double d1 = d0;
        for (AttributeModifier attributemodifier1 : this.func_180375_b(1)) {
            d1 += d0 * attributemodifier1.getAmount();
        }
        for (AttributeModifier attributemodifier2 : this.func_180375_b(2)) {
            d1 *= 1.0 + attributemodifier2.getAmount();
        }
        return this.genericAttribute.clampValue(d1);
    }

    private Collection<AttributeModifier> func_180375_b(int p_180375_1_) {
        HashSet<AttributeModifier> set = Sets.newHashSet(this.getModifiersByOperation(p_180375_1_));
        for (IAttribute iattribute = this.genericAttribute.func_180372_d(); iattribute != null; iattribute = iattribute.func_180372_d()) {
            IAttributeInstance iattributeinstance = this.attributeMap.getAttributeInstance(iattribute);
            if (iattributeinstance == null) continue;
            set.addAll(iattributeinstance.getModifiersByOperation(p_180375_1_));
        }
        return set;
    }
}

