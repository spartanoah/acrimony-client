/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LegacyStringUtils {
    public static LegacyStyle getStyleAt(String s, int position, boolean unknownWhite) {
        return LegacyStringUtils.getStyleAt(s, position, c -> {
            TextFormatting formatting = TextFormatting.getByCode(c.charValue());
            if (formatting == null) {
                if (unknownWhite) {
                    return TextFormatting.WHITE;
                }
                return null;
            }
            return formatting;
        });
    }

    public static LegacyStyle getStyleAt(String s, int position, Function<Character, TextFormatting> formattingResolver) {
        char[] chars = s.toCharArray();
        LegacyStyle legacyStyle = new LegacyStyle();
        for (int i = 0; i < Math.min(chars.length, position); ++i) {
            char code;
            TextFormatting formatting;
            char c = chars[i];
            if (c != '\u00a7' || i + 1 >= chars.length || (formatting = formattingResolver.apply(Character.valueOf(code = chars[++i]))) == null) continue;
            if (TextFormatting.RESET.equals(formatting)) {
                legacyStyle.setColor(null);
                legacyStyle.getStyles().clear();
                continue;
            }
            if (formatting.isColor()) {
                legacyStyle.setColor(formatting);
                legacyStyle.getStyles().clear();
                continue;
            }
            legacyStyle.getStyles().add(formatting);
        }
        return legacyStyle;
    }

    public static String[] split(String s, String split, boolean unknownWhite) {
        return LegacyStringUtils.split(s, split, c -> {
            TextFormatting formatting = TextFormatting.getByCode(c.charValue());
            if (formatting == null) {
                if (unknownWhite) {
                    return TextFormatting.WHITE;
                }
                return null;
            }
            return formatting;
        });
    }

    public static String[] split(String s, String split, Function<Character, TextFormatting> formattingResolver) {
        String[] parts = s.split(Pattern.quote(split));
        for (int i = 1; i < parts.length; ++i) {
            String prev = parts[i - 1];
            LegacyStyle style = LegacyStringUtils.getStyleAt(prev, prev.length(), formattingResolver);
            parts[i] = style.toLegacy() + parts[i];
        }
        return parts;
    }

    public static class LegacyStyle {
        private TextFormatting color = null;
        private final Set<TextFormatting> styles = new HashSet<TextFormatting>();

        private LegacyStyle() {
        }

        public void setColor(@Nullable TextFormatting color) {
            this.color = color;
        }

        @Nullable
        public TextFormatting getColor() {
            return this.color;
        }

        @Nonnull
        public Set<TextFormatting> getStyles() {
            return this.styles;
        }

        public String toLegacy() {
            StringBuilder out = new StringBuilder();
            if (this.color != null) {
                out.append(this.color.toLegacy());
            }
            for (TextFormatting style : this.styles) {
                out.append(style.toLegacy());
            }
            return out.toString();
        }

        public String toString() {
            return "LegacyStyle{color=" + this.color + ", styles=" + this.styles + '}';
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            LegacyStyle that = (LegacyStyle)o;
            return Objects.equals(this.color, that.color) && Objects.equals(this.styles, that.styles);
        }

        public int hashCode() {
            return Objects.hash(this.color, this.styles);
        }
    }
}

