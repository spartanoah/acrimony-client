/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EncodingCpuUsage {
    TTV_ECU_LOW(0),
    TTV_ECU_MEDIUM(1),
    TTV_ECU_HIGH(2);

    private static Map<Integer, EncodingCpuUsage> s_Map;
    private int m_Value;

    public static EncodingCpuUsage lookupValue(int n) {
        EncodingCpuUsage encodingCpuUsage = s_Map.get(n);
        return encodingCpuUsage;
    }

    private EncodingCpuUsage(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, EncodingCpuUsage>();
        EnumSet<EncodingCpuUsage> enumSet = EnumSet.allOf(EncodingCpuUsage.class);
        for (EncodingCpuUsage encodingCpuUsage : enumSet) {
            s_Map.put(encodingCpuUsage.getValue(), encodingCpuUsage);
        }
    }
}

