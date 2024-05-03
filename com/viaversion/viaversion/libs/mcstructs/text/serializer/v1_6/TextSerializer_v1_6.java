/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_6;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonSerializationContext;
import com.viaversion.viaversion.libs.gson.JsonSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import java.lang.reflect.Type;

public class TextSerializer_v1_6
implements JsonSerializer<ATextComponent> {
    @Override
    public JsonElement serialize(ATextComponent src, Type typeOfSrc, JsonSerializationContext context) {
        Style style = src.getStyle();
        JsonObject component = new JsonObject();
        if (style.getColor() != null && !style.getColor().isRGBColor()) {
            component.addProperty("color", style.getColor().serialize());
        }
        if (style.getBold() != null) {
            component.addProperty("bold", style.isBold());
        }
        if (style.getItalic() != null) {
            component.addProperty("italic", style.isItalic());
        }
        if (style.getUnderlined() != null) {
            component.addProperty("underlined", style.isUnderlined());
        }
        if (style.getObfuscated() != null) {
            component.addProperty("obfuscated", style.isObfuscated());
        }
        if (src instanceof StringComponent) {
            StringComponent stringComponent = (StringComponent)src;
            if (stringComponent.getSiblings().isEmpty()) {
                component.addProperty("text", stringComponent.getText());
            } else {
                JsonArray text = new JsonArray();
                text.add(stringComponent.getText());
                for (ATextComponent sibling : stringComponent.getSiblings()) {
                    if (sibling instanceof StringComponent && sibling.getStyle().isEmpty() && sibling.getSiblings().isEmpty()) {
                        text.add(((StringComponent)sibling).getText());
                        continue;
                    }
                    text.add(this.serialize(sibling, typeOfSrc, context));
                }
                component.add("text", text);
            }
        } else if (src instanceof TranslationComponent) {
            TranslationComponent translationComponent = (TranslationComponent)src;
            component.addProperty("translate", translationComponent.getKey());
            Object[] args = translationComponent.getArgs();
            if (args != null && args.length > 0) {
                if (args.length == 1 && args[0] instanceof String) {
                    component.addProperty("using", (String)args[0]);
                } else {
                    JsonArray using = new JsonArray();
                    for (Object arg : args) {
                        if (arg instanceof String) {
                            using.add((String)arg);
                            continue;
                        }
                        if (arg instanceof Boolean) {
                            using.add((Boolean)arg);
                            continue;
                        }
                        if (arg instanceof Character) {
                            using.add((Character)arg);
                            continue;
                        }
                        if (arg instanceof Number) {
                            using.add((Number)arg);
                            continue;
                        }
                        if (arg instanceof StringComponent) {
                            StringComponent stringComponent = (StringComponent)arg;
                            if ((stringComponent.getStyle() == null || stringComponent.getStyle().isEmpty()) && stringComponent.getSiblings().isEmpty()) {
                                using.add(stringComponent.getText());
                                continue;
                            }
                            using.add(this.serialize(stringComponent, typeOfSrc, context));
                            continue;
                        }
                        if (arg instanceof TranslationComponent) {
                            using.add(this.serialize((TranslationComponent)arg, typeOfSrc, context));
                            continue;
                        }
                        throw new IllegalArgumentException("Minecraft 1.9 did not support translation arguments of type " + arg.getClass().getName());
                    }
                    component.add("using", using);
                }
            }
        }
        return component;
    }
}

