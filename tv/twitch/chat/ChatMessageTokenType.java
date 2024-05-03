/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ChatMessageTokenType {
    TTV_CHAT_MSGTOKEN_TEXT(0),
    TTV_CHAT_MSGTOKEN_TEXTURE_IMAGE(1),
    TTV_CHAT_MSGTOKEN_URL_IMAGE(2);

    private static Map<Integer, ChatMessageTokenType> s_Map;
    private int m_Value;

    public static ChatMessageTokenType lookupValue(int n) {
        ChatMessageTokenType chatMessageTokenType = s_Map.get(n);
        return chatMessageTokenType;
    }

    private ChatMessageTokenType(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, ChatMessageTokenType>();
        EnumSet<ChatMessageTokenType> enumSet = EnumSet.allOf(ChatMessageTokenType.class);
        for (ChatMessageTokenType chatMessageTokenType : enumSet) {
            s_Map.put(chatMessageTokenType.getValue(), chatMessageTokenType);
        }
    }
}

