/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PixelFormat {
    TTV_PF_BGRA(66051),
    TTV_PF_ABGR(16909056),
    TTV_PF_RGBA(33619971),
    TTV_PF_ARGB(50462976);

    private static Map<Integer, PixelFormat> s_Map;
    private int m_Value;

    public static PixelFormat lookupValue(int n) {
        PixelFormat pixelFormat = s_Map.get(n);
        return pixelFormat;
    }

    private PixelFormat(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, PixelFormat>();
        EnumSet<PixelFormat> enumSet = EnumSet.allOf(PixelFormat.class);
        for (PixelFormat pixelFormat : enumSet) {
            s_Map.put(pixelFormat.getValue(), pixelFormat);
        }
    }
}

