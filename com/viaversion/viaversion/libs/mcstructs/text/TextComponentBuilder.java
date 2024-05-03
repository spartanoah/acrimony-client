/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text;

import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.hover.AHoverEvent;
import java.awt.Color;

public class TextComponentBuilder {
    public static ATextComponent build(Object ... parts) {
        StringComponent out = new StringComponent("");
        ATextComponent current = null;
        Style style = new Style();
        for (Object part : parts) {
            if (part == null) {
                TextComponentBuilder.checkAppend(out, current, style);
                current = null;
                style = new Style();
                continue;
            }
            if (part instanceof TextFormatting) {
                style.setFormatting((TextFormatting)part);
                continue;
            }
            if (part instanceof Color) {
                style.setFormatting(new TextFormatting(((Color)part).getRGB()));
                continue;
            }
            if (part instanceof ClickEvent) {
                style.setClickEvent((ClickEvent)part);
                continue;
            }
            if (part instanceof AHoverEvent) {
                style.setHoverEvent((AHoverEvent)part);
                continue;
            }
            if (part instanceof Style) {
                style = (Style)part;
                continue;
            }
            if (part instanceof ATextComponent) {
                if (TextComponentBuilder.checkAppend(out, current, style)) {
                    current = null;
                    style = new Style();
                }
                if (current == null) {
                    current = (StringComponent)part;
                    continue;
                }
                current.append((ATextComponent)part);
                continue;
            }
            if (TextComponentBuilder.checkAppend(out, current, style)) {
                current = null;
                style = new Style();
            }
            if (current == null) {
                current = new StringComponent(part.toString());
                continue;
            }
            current.append(part.toString());
        }
        if (current != null) {
            if (!style.isEmpty()) {
                current.setStyle(style);
            }
            out.append(current);
        }
        if (out.getSiblings().size() == 1) {
            return out.getSiblings().get(0);
        }
        return out;
    }

    private static boolean checkAppend(ATextComponent out, ATextComponent current, Style style) {
        if (current == null) {
            return !style.isEmpty();
        }
        if (style.isEmpty()) {
            return false;
        }
        out.append(current.setStyle(style));
        return true;
    }
}

