/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.components.nbt;

import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.NbtComponent;
import java.util.Objects;

public class EntityNbtComponent
extends NbtComponent {
    private final String selector;

    public EntityNbtComponent(String component, boolean resolve, String selector) {
        super(component, resolve);
        this.selector = selector;
    }

    public EntityNbtComponent(String component, boolean resolve, ATextComponent separator, String selector) {
        super(component, resolve, separator);
        this.selector = selector;
    }

    public String getSelector() {
        return this.selector;
    }

    @Override
    public ATextComponent copy() {
        if (this.getSeparator() == null) {
            return this.putMetaCopy(new EntityNbtComponent(this.getComponent(), this.isResolve(), null, this.selector));
        }
        return this.putMetaCopy(new EntityNbtComponent(this.getComponent(), this.isResolve(), this.getSeparator(), this.selector));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        EntityNbtComponent that = (EntityNbtComponent)o;
        return Objects.equals(this.getSiblings(), that.getSiblings()) && Objects.equals(this.getStyle(), that.getStyle()) && Objects.equals(this.selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSiblings(), this.getStyle(), this.selector);
    }

    @Override
    public String toString() {
        return "EntityNbtComponent{siblings=" + this.getSiblings() + ", style=" + this.getStyle() + ", selector='" + this.selector + '\'' + '}';
    }
}

