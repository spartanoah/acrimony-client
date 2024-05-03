/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text;

import com.viaversion.viaversion.libs.mcstructs.core.ICopyable;
import com.viaversion.viaversion.libs.mcstructs.core.Identifier;
import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import java.util.Objects;

public class Style
implements ICopyable<Style> {
    private Style parent;
    private TextFormatting color;
    private Boolean obfuscated;
    private Boolean bold;
    private Boolean strikethrough;
    private Boolean underlined;
    private Boolean italic;
    private ClickEvent clickEvent;
    private AHoverEvent hoverEvent;
    private String insertion;
    private Identifier font;

    public Style setParent(Style style) {
        this.parent = style;
        return this;
    }

    public Style getParent() {
        return this.parent;
    }

    public Style setFormatting(TextFormatting formatting) {
        if (formatting == null) {
            return this;
        }
        if (formatting.isColor()) {
            this.color = formatting;
        } else if (TextFormatting.OBFUSCATED.equals(formatting)) {
            this.obfuscated = true;
        } else if (TextFormatting.BOLD.equals(formatting)) {
            this.bold = true;
        } else if (TextFormatting.STRIKETHROUGH.equals(formatting)) {
            this.strikethrough = true;
        } else if (TextFormatting.UNDERLINE.equals(formatting)) {
            this.underlined = true;
        } else if (TextFormatting.ITALIC.equals(formatting)) {
            this.italic = true;
        } else if (TextFormatting.RESET.equals(formatting)) {
            this.color = null;
            this.obfuscated = null;
            this.bold = null;
            this.strikethrough = null;
            this.underlined = null;
            this.italic = null;
            this.clickEvent = null;
            this.hoverEvent = null;
            this.insertion = null;
            this.font = null;
        } else {
            throw new IllegalArgumentException("Invalid TextFormatting " + formatting);
        }
        return this;
    }

    public Style setFormatting(TextFormatting ... formattings) {
        for (TextFormatting formatting : formattings) {
            this.setFormatting(formatting);
        }
        return this;
    }

    public Style setColor(int rgb) {
        this.color = new TextFormatting(rgb);
        return this;
    }

    public TextFormatting getColor() {
        if (this.color == null && this.parent != null) {
            return this.parent.getColor();
        }
        return this.color;
    }

    public Style setBold(Boolean bold) {
        this.bold = bold;
        return this;
    }

    public Boolean getBold() {
        if (this.bold == null && this.parent != null) {
            return this.parent.getBold();
        }
        return this.bold;
    }

    public boolean isBold() {
        Boolean bold = this.getBold();
        return bold != null && bold != false;
    }

    public Style setItalic(Boolean italic) {
        this.italic = italic;
        return this;
    }

    public Boolean getItalic() {
        if (this.italic == null && this.parent != null) {
            return this.parent.getItalic();
        }
        return this.italic;
    }

    public boolean isItalic() {
        Boolean italic = this.getItalic();
        return italic != null && italic != false;
    }

    public Style setUnderlined(Boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    public Boolean getUnderlined() {
        if (this.underlined == null && this.parent != null) {
            return this.parent.getUnderlined();
        }
        return this.underlined;
    }

    public boolean isUnderlined() {
        Boolean underlined = this.getUnderlined();
        return underlined != null && underlined != false;
    }

    public Style setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public Boolean getStrikethrough() {
        if (this.strikethrough == null && this.parent != null) {
            return this.parent.getStrikethrough();
        }
        return this.strikethrough;
    }

    public boolean isStrikethrough() {
        Boolean strikethrough = this.getStrikethrough();
        return strikethrough != null && strikethrough != false;
    }

    public Style setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }

    public Boolean getObfuscated() {
        if (this.obfuscated == null && this.parent != null) {
            return this.parent.getObfuscated();
        }
        return this.obfuscated;
    }

    public boolean isObfuscated() {
        Boolean obfuscated = this.getObfuscated();
        return obfuscated != null && obfuscated != false;
    }

    public Style setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public ClickEvent getClickEvent() {
        if (this.clickEvent == null && this.parent != null) {
            return this.parent.getClickEvent();
        }
        return this.clickEvent;
    }

    public Style setHoverEvent(AHoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public AHoverEvent getHoverEvent() {
        if (this.hoverEvent == null && this.parent != null) {
            return this.parent.getHoverEvent();
        }
        return this.hoverEvent;
    }

    public Style setInsertion(String insertion) {
        this.insertion = insertion;
        return this;
    }

    public String getInsertion() {
        if (this.insertion == null && this.parent != null) {
            return this.parent.getInsertion();
        }
        return this.insertion;
    }

    public Style setFont(Identifier font) {
        this.font = font;
        return this;
    }

    public Identifier getFont() {
        if (this.font == null && this.parent != null) {
            return this.parent.getFont();
        }
        return this.font;
    }

    public boolean isEmpty() {
        return this.getColor() == null && this.getBold() == null && this.getItalic() == null && this.getUnderlined() == null && this.getStrikethrough() == null && this.getObfuscated() == null && this.getClickEvent() == null && this.getHoverEvent() == null && this.getInsertion() == null && this.getFont() == null;
    }

    @Override
    public Style copy() {
        Style style = new Style();
        style.parent = this.parent;
        style.color = this.color;
        style.bold = this.bold;
        style.italic = this.italic;
        style.underlined = this.underlined;
        style.strikethrough = this.strikethrough;
        style.obfuscated = this.obfuscated;
        style.clickEvent = this.clickEvent;
        style.hoverEvent = this.hoverEvent;
        style.insertion = this.insertion;
        style.font = this.font;
        return style;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Style style = (Style)o;
        return Objects.equals(this.parent, style.parent) && Objects.equals(this.color, style.color) && Objects.equals(this.obfuscated, style.obfuscated) && Objects.equals(this.bold, style.bold) && Objects.equals(this.strikethrough, style.strikethrough) && Objects.equals(this.underlined, style.underlined) && Objects.equals(this.italic, style.italic) && Objects.equals(this.clickEvent, style.clickEvent) && Objects.equals(this.hoverEvent, style.hoverEvent) && Objects.equals(this.insertion, style.insertion) && Objects.equals(this.font, style.font);
    }

    public int hashCode() {
        return Objects.hash(this.parent, this.color, this.obfuscated, this.bold, this.strikethrough, this.underlined, this.italic, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public String toString() {
        return "Style{parent=" + this.parent + ", color=" + this.color + ", obfuscated=" + this.obfuscated + ", bold=" + this.bold + ", strikethrough=" + this.strikethrough + ", underlined=" + this.underlined + ", italic=" + this.italic + ", clickEvent=" + this.clickEvent + ", hoverEvent=" + this.hoverEvent + ", insertion='" + this.insertion + '\'' + ", font=" + this.font + '}';
    }
}

