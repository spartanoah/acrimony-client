/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.mcstructs.core.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEvent;
import com.viaversion.viaversion.libs.mcstructs.text.events.click.ClickEventAction;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static final String URL_PATTERN = "(?:https?://)?[\\w._-]+\\.\\w{2,}(?:/\\S*)?";

    public static ATextComponent makeURLsClickable(ATextComponent component) {
        return TextUtils.replace(component, URL_PATTERN, comp -> {
            comp.getStyle().setClickEvent(new ClickEvent(ClickEventAction.OPEN_URL, comp.asSingleString()));
            return comp;
        });
    }

    public static ATextComponent replace(ATextComponent component, String searchRegex, Function<ATextComponent, ATextComponent> replaceFunction) {
        ATextComponent out;
        Pattern pattern = Pattern.compile(searchRegex);
        if (component instanceof StringComponent) {
            String text = component.asSingleString();
            Matcher matcher = pattern.matcher(text);
            ArrayList<ATextComponent> parts = new ArrayList<ATextComponent>();
            int last = 0;
            while (matcher.find()) {
                ATextComponent replace;
                int start = matcher.start();
                String match = matcher.group();
                if (start > last) {
                    parts.add(new StringComponent(text.substring(last, start)).setStyle(component.getStyle().copy()));
                }
                if ((replace = replaceFunction.apply(new StringComponent(match).setStyle(component.getStyle().copy()))) != null) {
                    parts.add(replace);
                }
                last = matcher.end();
            }
            if (last < text.length()) {
                parts.add(new StringComponent(text.substring(last)).setStyle(component.getStyle().copy()));
            }
            if (parts.size() > 1) {
                out = new StringComponent("");
                for (ATextComponent part : parts) {
                    out.append(part);
                }
            } else {
                out = parts.size() == 1 ? ((ATextComponent)parts.get(0)).copy() : component.copy();
                out.getSiblings().clear();
            }
        } else {
            out = component.copy();
            out.getSiblings().clear();
        }
        for (ATextComponent sibling : component.getSiblings()) {
            ATextComponent replace = TextUtils.replace(sibling, searchRegex, replaceFunction);
            out.append(replace);
        }
        return out;
    }

    public static ATextComponent replaceRGBColors(ATextComponent component) {
        ATextComponent out = component.copy();
        out.forEach(comp -> {
            if (comp.getStyle().getColor() != null && comp.getStyle().getColor().isRGBColor()) {
                comp.getStyle().setFormatting(TextFormatting.getClosestFormattingColor(comp.getStyle().getColor().getRgbValue()));
            }
        });
        return out;
    }

    public static ATextComponent join(ATextComponent separator, ATextComponent ... components) {
        if (components.length == 0) {
            return new StringComponent("");
        }
        if (components.length == 1) {
            return components[0].copy();
        }
        ATextComponent out = null;
        for (ATextComponent component : components) {
            if (out == null) {
                out = new StringComponent("").append(component.copy());
                continue;
            }
            out.append(separator.copy()).append(component.copy());
        }
        return out;
    }
}

