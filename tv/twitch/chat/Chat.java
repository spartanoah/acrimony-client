/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import java.util.HashSet;
import tv.twitch.ErrorCode;
import tv.twitch.chat.ChatAPI;
import tv.twitch.chat.ChatBadgeData;
import tv.twitch.chat.ChatEmoticonData;
import tv.twitch.chat.ChatTokenizationOption;
import tv.twitch.chat.IChatAPIListener;
import tv.twitch.chat.IChatChannelListener;

public class Chat {
    private static Chat s_Instance = null;
    private ChatAPI m_ChatAPI = null;

    public static Chat getInstance() {
        return s_Instance;
    }

    public Chat(ChatAPI chatAPI) {
        this.m_ChatAPI = chatAPI;
        if (s_Instance == null) {
            s_Instance = this;
        }
    }

    public ErrorCode initialize(HashSet<ChatTokenizationOption> hashSet, IChatAPIListener iChatAPIListener) {
        return this.m_ChatAPI.initialize(hashSet, iChatAPIListener);
    }

    public ErrorCode shutdown() {
        return this.m_ChatAPI.shutdown();
    }

    public ErrorCode connect(String string, String string2, String string3, IChatChannelListener iChatChannelListener) {
        return this.m_ChatAPI.connect(string, string2, string3, iChatChannelListener);
    }

    public ErrorCode connectAnonymous(String string, IChatChannelListener iChatChannelListener) {
        return this.m_ChatAPI.connectAnonymous(string, iChatChannelListener);
    }

    public ErrorCode disconnect(String string) {
        return this.m_ChatAPI.disconnect(string);
    }

    public ErrorCode sendMessage(String string, String string2) {
        return this.m_ChatAPI.sendMessage(string, string2);
    }

    public ErrorCode flushEvents() {
        return this.m_ChatAPI.flushEvents();
    }

    public ErrorCode downloadEmoticonData() {
        return this.m_ChatAPI.downloadEmoticonData();
    }

    public ErrorCode getEmoticonData(ChatEmoticonData chatEmoticonData) {
        return this.m_ChatAPI.getEmoticonData(chatEmoticonData);
    }

    public ErrorCode clearEmoticonData() {
        return this.m_ChatAPI.clearEmoticonData();
    }

    public ErrorCode downloadBadgeData(String string) {
        return this.m_ChatAPI.downloadBadgeData(string);
    }

    public ErrorCode getBadgeData(String string, ChatBadgeData chatBadgeData) {
        return this.m_ChatAPI.getBadgeData(string, chatBadgeData);
    }

    public ErrorCode clearBadgeData(String string) {
        return this.m_ChatAPI.clearBadgeData(string);
    }

    public int getMessageFlushInterval() {
        return this.m_ChatAPI.getMessageFlushInterval();
    }

    public ErrorCode setMessageFlushInterval(int n) {
        return this.m_ChatAPI.setMessageFlushInterval(n);
    }

    public int getUserChangeEventInterval() {
        return this.m_ChatAPI.getUserChangeEventInterval();
    }

    public ErrorCode setUserChangeEventInterval(int n) {
        return this.m_ChatAPI.setUserChangeEventInterval(n);
    }
}

