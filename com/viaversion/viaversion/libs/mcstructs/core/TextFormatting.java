/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public class TextFormatting {
    public static final Map<String, TextFormatting> ALL = new LinkedHashMap<String, TextFormatting>();
    public static final Map<String, TextFormatting> COLORS = new LinkedHashMap<String, TextFormatting>();
    public static final Map<String, TextFormatting> FORMATTINGS = new LinkedHashMap<String, TextFormatting>();
    public static final char COLOR_CHAR = '\u00a7';
    public static final TextFormatting BLACK = new TextFormatting("black", '0', 0);
    public static final TextFormatting DARK_BLUE = new TextFormatting("dark_blue", '1', 170);
    public static final TextFormatting DARK_GREEN = new TextFormatting("dark_green", '2', 43520);
    public static final TextFormatting DARK_AQUA = new TextFormatting("dark_aqua", '3', 43690);
    public static final TextFormatting DARK_RED = new TextFormatting("dark_red", '4', 0xAA0000);
    public static final TextFormatting DARK_PURPLE = new TextFormatting("dark_purple", '5', 0xAA00AA);
    public static final TextFormatting GOLD = new TextFormatting("gold", '6', 0xFFAA00);
    public static final TextFormatting GRAY = new TextFormatting("gray", '7', 0xAAAAAA);
    public static final TextFormatting DARK_GRAY = new TextFormatting("dark_gray", '8', 0x555555);
    public static final TextFormatting BLUE = new TextFormatting("blue", '9', 0x5555FF);
    public static final TextFormatting GREEN = new TextFormatting("green", 'a', 0x55FF55);
    public static final TextFormatting AQUA = new TextFormatting("aqua", 'b', 0x55FFFF);
    public static final TextFormatting RED = new TextFormatting("red", 'c', 0xFF5555);
    public static final TextFormatting LIGHT_PURPLE = new TextFormatting("light_purple", 'd', 0xFF55FF);
    public static final TextFormatting YELLOW = new TextFormatting("yellow", 'e', 0xFFFF55);
    public static final TextFormatting WHITE = new TextFormatting("white", 'f', 0xFFFFFF);
    public static final TextFormatting OBFUSCATED = new TextFormatting("obfuscated", 'k');
    public static final TextFormatting BOLD = new TextFormatting("bold", 'l');
    public static final TextFormatting STRIKETHROUGH = new TextFormatting("strikethrough", 'm');
    public static final TextFormatting UNDERLINE = new TextFormatting("underline", 'n');
    public static final TextFormatting ITALIC = new TextFormatting("italic", 'o');
    public static final TextFormatting RESET = new TextFormatting("reset", 'r');
    private final Type type;
    private final int ordinal;
    private final String name;
    private final char code;
    private final int rgbValue;

    @Nullable
    public static TextFormatting getByOrdinal(int ordinal) {
        return ALL.values().stream().filter(formatting -> formatting.ordinal == ordinal).findFirst().orElse(null);
    }

    @Nullable
    public static TextFormatting getByName(String name) {
        return ALL.get(name.toLowerCase());
    }

    @Nullable
    public static TextFormatting getByCode(char code) {
        for (TextFormatting formatting : ALL.values()) {
            if (formatting.getCode() != code) continue;
            return formatting;
        }
        return null;
    }

    @Nullable
    public static TextFormatting parse(String s) {
        if (s.startsWith("#")) {
            try {
                return new TextFormatting(Integer.parseInt(s.substring(1), 16));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return TextFormatting.getByName(s);
    }

    public static TextFormatting getClosestFormattingColor(int rgb) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        TextFormatting closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (TextFormatting color : COLORS.values()) {
            int colorB;
            int colorG;
            int colorR = color.getRgbValue() >> 16 & 0xFF;
            int distance = (r - colorR) * (r - colorR) + (g - (colorG = color.getRgbValue() >> 8 & 0xFF)) * (g - colorG) + (b - (colorB = color.getRgbValue() & 0xFF)) * (b - colorB);
            if (distance >= closestDistance) continue;
            closest = color;
            closestDistance = distance;
        }
        return closest;
    }

    private TextFormatting(String name, char code, int rgbValue) {
        this.type = Type.COLOR;
        this.ordinal = ALL.size();
        this.name = name;
        this.code = code;
        this.rgbValue = rgbValue;
        ALL.put(name, this);
        COLORS.put(name, this);
    }

    private TextFormatting(String name, char code) {
        this.type = Type.FORMATTING;
        this.ordinal = ALL.size();
        this.name = name;
        this.code = code;
        this.rgbValue = -1;
        ALL.put(name, this);
        FORMATTINGS.put(name, this);
    }

    public TextFormatting(int rgbValue) {
        this.type = Type.RGB;
        this.ordinal = -1;
        this.name = "RGB_COLOR";
        this.code = '\u0000';
        this.rgbValue = rgbValue & 0xFFFFFF;
    }

    public boolean isColor() {
        return Type.COLOR.equals((Object)this.type) || Type.RGB.equals((Object)this.type);
    }

    public boolean isFormattingColor() {
        return Type.COLOR.equals((Object)this.type);
    }

    public boolean isRGBColor() {
        return Type.RGB.equals((Object)this.type);
    }

    public boolean isFormatting() {
        return Type.FORMATTING.equals((Object)this.type);
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public String getName() {
        return this.name;
    }

    public char getCode() {
        return this.code;
    }

    public int getRgbValue() {
        return this.rgbValue;
    }

    public String toLegacy() {
        return String.valueOf('\u00a7') + this.code;
    }

    public String serialize() {
        if (Type.RGB.equals((Object)this.type)) {
            return "#" + String.format("%06X", this.rgbValue);
        }
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TextFormatting that = (TextFormatting)o;
        return this.code == that.code && this.rgbValue == that.rgbValue && this.type == that.type && Objects.equals(this.name, that.name);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.type, this.name, Character.valueOf(this.code), this.rgbValue});
    }

    public String toString() {
        return "TextFormatting{type=" + (Object)((Object)this.type) + ", name='" + this.name + '\'' + ", code=" + this.code + ", rgbValue=" + this.rgbValue + "}";
    }

    private static enum Type {
        COLOR,
        FORMATTING,
        RGB;

    }
}

