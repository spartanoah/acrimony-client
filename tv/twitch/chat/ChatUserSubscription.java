/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ChatUserSubscription {
    TTV_CHAT_USERSUB_NONE(0),
    TTV_CHAT_USERSUB_SUBSCRIBER(1),
    TTV_CHAT_USERSUB_TURBO(2);

    private static Map<Integer, ChatUserSubscription> s_Map;
    private int m_Value;

    public static ChatUserSubscription lookupValue(int n) {
        ChatUserSubscription chatUserSubscription = s_Map.get(n);
        return chatUserSubscription;
    }

    private ChatUserSubscription(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, ChatUserSubscription>();
        EnumSet<ChatUserSubscription> enumSet = EnumSet.allOf(ChatUserSubscription.class);
        for (ChatUserSubscription chatUserSubscription : enumSet) {
            s_Map.put(chatUserSubscription.getValue(), chatUserSubscription);
        }
    }
}

