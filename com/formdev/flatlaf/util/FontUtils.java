/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.formdev.flatlaf.util;

import com.formdev.flatlaf.util.LoggingFacade;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.plaf.UIResource;
import javax.swing.text.StyleContext;

public class FontUtils {
    private static Map<String, Runnable> loadersMap;

    public static Font getCompositeFont(String family, int style, int size) {
        FontUtils.loadFontFamily(family);
        Font font = StyleContext.getDefaultStyleContext().getFont(family, style, size);
        if (font instanceof UIResource) {
            font = font.deriveFont(font.getStyle());
        }
        return font;
    }

    public static void registerFontFamilyLoader(String family, Runnable loader) {
        if (loadersMap == null) {
            loadersMap = new HashMap<String, Runnable>();
        }
        loadersMap.put(family, loader);
    }

    public static void loadFontFamily(String family) {
        if (!FontUtils.hasLoaders()) {
            return;
        }
        Runnable loader = loadersMap.remove(family);
        if (loader != null) {
            loader.run();
        }
        if (loadersMap.isEmpty()) {
            loadersMap = null;
        }
    }

    public static boolean installFont(URL url) {
        boolean bl;
        block8: {
            InputStream in = url.openStream();
            try {
                Font font = Font.createFont(0, in);
                bl = GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                if (in == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (FontFormatException | IOException ex) {
                    LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to install font " + url, ex);
                    return false;
                }
            }
            in.close();
        }
        return bl;
    }

    public static String[] getAvailableFontFamilyNames() {
        String[] availableFontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        if (!FontUtils.hasLoaders()) {
            return availableFontFamilyNames;
        }
        ArrayList<String> result = new ArrayList<String>(availableFontFamilyNames.length + loadersMap.size());
        for (String name : availableFontFamilyNames) {
            result.add(name);
        }
        for (String name : loadersMap.keySet()) {
            if (result.contains(name)) continue;
            result.add(name);
        }
        return result.toArray(new String[result.size()]);
    }

    public static Font[] getAllFonts() {
        if (FontUtils.hasLoaders()) {
            String[] families;
            for (String family : families = loadersMap.keySet().toArray(new String[loadersMap.size()])) {
                FontUtils.loadFontFamily(family);
            }
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    private static boolean hasLoaders() {
        return loadersMap != null && !loadersMap.isEmpty();
    }
}

