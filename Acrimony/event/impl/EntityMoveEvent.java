/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.event.impl;

import Acrimony.event.Event;
import net.minecraft.entity.Entity;

public class EntityMoveEvent
extends Event {
    private Entity entity;

    public Entity getEntity() {
        return this.entity;
    }

    public EntityMoveEvent(Entity entity) {
        this.entity = entity;
    }
}

