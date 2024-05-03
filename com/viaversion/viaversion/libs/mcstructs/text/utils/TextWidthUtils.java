/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.libs.mcstructs.text.utils;

import com.viaversion.viaversion.libs.mcstructs.text.ATextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.utils.TextUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class TextWidthUtils {
    private static float[] charWidths = null;

    private static void loadCharWidths() {
        if (charWidths == null) {
            InputStream is = TextUtils.class.getResourceAsStream("/mcstructs/text/charwidths.bin");
            if (is == null) {
                throw new IllegalStateException("Could not find charwidths.bin");
            }
            try (GZIPInputStream gis = new GZIPInputStream(is);){
                charWidths = new float[gis.read() << 24 | gis.read() << 16 | gis.read() << 8 | gis.read()];
                for (int i = 0; i < charWidths.length; ++i) {
                    TextWidthUtils.charWidths[i] = gis.read();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read char widths", e);
            }
        }
    }

    public static float[] getCharWidths() {
        TextWidthUtils.loadCharWidths();
        return charWidths;
    }

    public static float getCharWidth(char c, float boldOffset, boolean bold) {
        TextWidthUtils.loadCharWidths();
        if (c > charWidths.length) {
            return 0.0f;
        }
        return charWidths[c] + (bold ? boldOffset : 0.0f);
    }

    public static float getComponentWidth(ATextComponent component) {
        TextWidthUtils.loadCharWidths();
        return TextWidthUtils.getComponentWidth(component, charWidths, 1.0f);
    }

    public static float getComponentWidth(ATextComponent component, float[] widths) {
        return TextWidthUtils.getComponentWidth(component, widths, 1.0f);
    }

    public static float getComponentWidth(ATextComponent component, float[] widths, float boldOffset) {
        float[] width = new float[]{0.0f};
        component.forEach(comp -> {
            char[] chars;
            for (char c : chars = comp.asSingleString().toCharArray()) {
                width[0] = width[0] + (c >= widths.length ? 0.0f : widths[c]);
            }
            if (comp.getStyle().isBold()) {
                width[0] = width[0] + boldOffset * (float)chars.length;
            }
        });
        return width[0];
    }
}

