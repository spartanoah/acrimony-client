/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum StatType {
    TTV_ST_RTMPSTATE(0),
    TTV_ST_RTMPDATASENT(1);

    private static Map<Integer, StatType> s_Map;
    private int m_Value;

    public static StatType lookupValue(int n) {
        StatType statType = s_Map.get(n);
        return statType;
    }

    private StatType(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, StatType>();
        EnumSet<StatType> enumSet = EnumSet.allOf(StatType.class);
        for (StatType statType : enumSet) {
            s_Map.put(statType.getValue(), statType);
        }
    }
}

