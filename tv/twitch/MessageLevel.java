/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MessageLevel {
    TTV_ML_DEBUG(0),
    TTV_ML_INFO(1),
    TTV_ML_WARNING(2),
    TTV_ML_ERROR(3),
    TTV_ML_CHAT(4),
    TTV_ML_NONE(5);

    private static Map<Integer, MessageLevel> s_Map;
    private int m_Value;

    public static MessageLevel lookupValue(int n) {
        MessageLevel messageLevel = s_Map.get(n);
        return messageLevel;
    }

    private MessageLevel(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, MessageLevel>();
        EnumSet<MessageLevel> enumSet = EnumSet.allOf(MessageLevel.class);
        for (MessageLevel messageLevel : enumSet) {
            s_Map.put(messageLevel.getValue(), messageLevel);
        }
    }
}

