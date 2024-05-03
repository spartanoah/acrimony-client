/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ChatUserMode {
    TTV_CHAT_USERMODE_VIEWER(0),
    TTV_CHAT_USERMODE_MODERATOR(1),
    TTV_CHAT_USERMODE_BROADCASTER(2),
    TTV_CHAT_USERMODE_ADMINSTRATOR(4),
    TTV_CHAT_USERMODE_STAFF(8),
    TTV_CHAT_USERMODE_BANNED(0x40000000);

    private static Map<Integer, ChatUserMode> s_Map;
    private int m_Value;

    public static ChatUserMode lookupValue(int n) {
        ChatUserMode chatUserMode = s_Map.get(n);
        return chatUserMode;
    }

    private ChatUserMode(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, ChatUserMode>();
        EnumSet<ChatUserMode> enumSet = EnumSet.allOf(ChatUserMode.class);
        for (ChatUserMode chatUserMode : enumSet) {
            s_Map.put(chatUserMode.getValue(), chatUserMode);
        }
    }
}

