/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text;

import com.viaversion.viaversion.libs.mcstructs.core.ICopyable;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public abstract class ATextComponent
implements ICopyable<ATextComponent> {
    private final List<ATextComponent> siblings = new ArrayList<ATextComponent>();
    private Style style = new Style();

    public ATextComponent append(String s) {
        this.append((ATextComponent)new StringComponent(s));
        return this;
    }

    public ATextComponent append(ATextComponent component) {
        this.siblings.add(component);
        return this;
    }

    public ATextComponent append(ATextComponent ... components) {
        this.siblings.addAll(Arrays.asList(components));
        return this;
    }

    public List<ATextComponent> getSiblings() {
        return this.siblings;
    }

    public ATextComponent forEach(Consumer<ATextComponent> consumer) {
        consumer.accept(this);
        for (ATextComponent sibling : this.siblings) {
            sibling.forEach(consumer);
        }
        return this;
    }

    @Nonnull
    public Style getStyle() {
        return this.style;
    }

    public ATextComponent setStyle(@Nonnull Style style) {
        this.style = style;
        return this;
    }

    public ATextComponent setParentStyle(@Nonnull Style style) {
        this.style.setParent(style);
        return this;
    }

    public ATextComponent copyParentStyle() {
        for (ATextComponent sibling : this.siblings) {
            sibling.getStyle().setParent(this.style);
            sibling.copyParentStyle();
        }
        return this;
    }

    public <C extends ATextComponent> C putMetaCopy(C component) {
        component.setStyle(this.style.copy());
        for (ATextComponent sibling : this.siblings) {
            component.append(sibling.copy());
        }
        return component;
    }

    public String asUnformattedString() {
        StringBuilder out = new StringBuilder(this.asSingleString());
        for (ATextComponent sibling : this.siblings) {
            out.append(sibling.asUnformattedString());
        }
        return out.toString();
    }

    public String asLegacyFormatString() {
        StringBuilder out = new StringBuilder();
        if (this.style.getColor() != null && this.style.getColor().isFormattingColor()) {
            out.append('\u00a7').append(this.style.getColor().getCode());
        }
        if (this.style.isObfuscated()) {
            out.append('\u00a7').append(TextFormatting.OBFUSCATED.getCode());
        }
        if (this.style.isBold()) {
            out.append('\u00a7').append(TextFormatting.BOLD.getCode());
        }
        if (this.style.isStrikethrough()) {
            out.append('\u00a7').append(TextFormatting.STRIKETHROUGH.getCode());
        }
        if (this.style.isUnderlined()) {
            out.append('\u00a7').append(TextFormatting.UNDERLINE.getCode());
        }
        if (this.style.isItalic()) {
            out.append('\u00a7').append(TextFormatting.ITALIC.getCode());
        }
        out.append(this.asSingleString());
        for (ATextComponent sibling : this.siblings) {
            ATextComponent copy = sibling.copy();
            copy.getStyle().setParent(this.style);
            out.append(copy.asLegacyFormatString());
        }
        return out.toString();
    }

    public abstract String asSingleString();

    @Override
    public abstract ATextComponent copy();

    public abstract boolean equals(Object var1);

    public abstract int hashCode();

    public abstract String toString();
}

