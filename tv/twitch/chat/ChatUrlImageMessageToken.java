/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import tv.twitch.chat.ChatMessageToken;
import tv.twitch.chat.ChatMessageTokenType;

public class ChatUrlImageMessageToken
extends ChatMessageToken {
    public String url;
    public short width;
    public short height;

    public ChatUrlImageMessageToken() {
        this.type = ChatMessageTokenType.TTV_CHAT_MSGTOKEN_URL_IMAGE;
    }
}

