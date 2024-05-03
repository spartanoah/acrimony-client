/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.serializer.v1_12;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParseException;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.gson.JsonSerializationContext;
import com.viaversion.viaversion.libs.gson.JsonSerializer;
import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.KeybindComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.ScoreComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.SelectorComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import java.lang.reflect.Type;
import java.util.Map;

public class TextSerializer_v1_12
implements JsonSerializer<ATextComponent> {
    @Override
    public JsonElement serialize(ATextComponent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement serializedStyle;
        JsonObject serializedComponent = new JsonObject();
        if (!src.getStyle().isEmpty() && (serializedStyle = context.serialize(src.getStyle())).isJsonObject()) {
            JsonObject serializedStyleObject = serializedStyle.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : serializedStyleObject.entrySet()) {
                serializedComponent.add(entry.getKey(), entry.getValue());
            }
        }
        if (!src.getSiblings().isEmpty()) {
            JsonArray siblings = new JsonArray();
            for (ATextComponent sibling : src.getSiblings()) {
                siblings.add(this.serialize(sibling, (Type)sibling.getClass(), context));
            }
            serializedComponent.add("extra", siblings);
        }
        if (src instanceof StringComponent) {
            serializedComponent.addProperty("text", ((StringComponent)src).getText());
        } else if (src instanceof TranslationComponent) {
            TranslationComponent translationComponent = (TranslationComponent)src;
            serializedComponent.addProperty("translate", translationComponent.getKey());
            if (translationComponent.getArgs().length > 0) {
                Object[] args;
                JsonArray with = new JsonArray();
                for (Object arg : args = translationComponent.getArgs()) {
                    if (arg instanceof ATextComponent) {
                        with.add(this.serialize((ATextComponent)arg, (Type)arg.getClass(), context));
                        continue;
                    }
                    with.add(new JsonPrimitive(String.valueOf(arg)));
                }
                serializedComponent.add("with", with);
            }
        } else if (src instanceof ScoreComponent) {
            ScoreComponent scoreComponent = (ScoreComponent)src;
            JsonObject serializedScore = new JsonObject();
            serializedScore.addProperty("name", scoreComponent.getName());
            serializedScore.addProperty("objective", scoreComponent.getObjective());
            serializedScore.addProperty("value", scoreComponent.getValue());
            serializedComponent.add("score", serializedScore);
        } else if (src instanceof SelectorComponent) {
            serializedComponent.addProperty("selector", ((SelectorComponent)src).getSelector());
        } else if (src instanceof KeybindComponent) {
            serializedComponent.addProperty("keybind", ((KeybindComponent)src).getKeybind());
        } else {
            throw new JsonParseException("Don't know how to serialize " + src + " as a Component");
        }
        return serializedComponent;
    }
}

