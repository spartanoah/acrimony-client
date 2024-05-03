/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer;

import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import java.util.function.Function;

public class LegacyStringDeserializer {
    public static ATextComponent parse(String s, boolean unknownWhite) {
        return LegacyStringDeserializer.parse(s, '\u00a7', unknownWhite);
    }

    public static ATextComponent parse(String s, char colorChar, boolean unknownWhite) {
        return LegacyStringDeserializer.parse(s, colorChar, c -> {
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

    public static ATextComponent parse(String s, char colorChar, Function<Character, TextFormatting> formattingResolver) {
        char[] chars = s.toCharArray();
        Style style = new Style();
        StringBuilder currentPart = new StringBuilder();
        StringComponent out = new StringComponent("");
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c == colorChar) {
                char format;
                TextFormatting formatting;
                if (i + 1 >= chars.length || (formatting = formattingResolver.apply(Character.valueOf(format = chars[++i]))) == null) continue;
                if (currentPart.length() != 0) {
                    out.append(new StringComponent(currentPart.toString()).setStyle(style.copy()));
                    currentPart = new StringBuilder();
                    if (formatting.isColor() || TextFormatting.RESET.equals(formatting)) {
                        style = new Style();
                    }
                }
                style.setFormatting(formatting);
                continue;
            }
            currentPart.append(c);
        }
        if (currentPart.length() != 0) {
            out.append(new StringComponent(currentPart.toString()).setStyle(style));
        }
        if (out.getSiblings().size() == 1) {
            return out.getSiblings().get(0);
        }
        return out;
    }
}

