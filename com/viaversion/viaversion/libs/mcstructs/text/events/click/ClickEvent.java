/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.events.click;

import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import java.util.Objects;

public class ClickEvent {
    private final ClickEventAction action;
    private final String value;

    public ClickEvent(ClickEventAction action, String value) {
        this.action = action;
        this.value = value;
    }

    public ClickEventAction getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ClickEvent that = (ClickEvent)o;
        return this.action == that.action && Objects.equals(this.value, that.value);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.action, this.value});
    }

    public String toString() {
        return "ClickEvent{action=" + (Object)((Object)this.action) + ", value='" + this.value + "'}";
    }
}

