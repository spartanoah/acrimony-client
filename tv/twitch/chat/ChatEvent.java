/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ChatEvent {
    TTV_CHAT_JOINED_CHANNEL(0),
    TTV_CHAT_LEFT_CHANNEL(1);

    private static Map<Integer, ChatEvent> s_Map;
    private int m_Value;

    public static ChatEvent lookupValue(int n) {
        ChatEvent chatEvent = s_Map.get(n);
        return chatEvent;
    }

    private ChatEvent(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, ChatEvent>();
        EnumSet<ChatEvent> enumSet = EnumSet.allOf(ChatEvent.class);
        for (ChatEvent chatEvent : enumSet) {
            s_Map.put(chatEvent.getValue(), chatEvent);
        }
    }
}

