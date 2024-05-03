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

public class StandardChatAPI
extends ChatAPI {
    protected void finalize() {
    }

    private static native ErrorCode TTV_Java_Chat_Initialize(int var0, IChatAPIListener var1);

    private static native ErrorCode TTV_Java_Chat_Shutdown();

    private static native ErrorCode TTV_Java_Chat_Connect(String var0, String var1, String var2, IChatChannelListener var3);

    private static native ErrorCode TTV_Java_Chat_ConnectAnonymous(String var0, IChatChannelListener var1);

    private static native ErrorCode TTV_Java_Chat_Disconnect(String var0);

    private static native ErrorCode TTV_Java_Chat_SendMessage(String var0, String var1);

    private static native ErrorCode TTV_Java_Chat_FlushEvents();

    private static native ErrorCode TTV_Java_Chat_DownloadEmoticonData();

    private static native ErrorCode TTV_Java_Chat_GetEmoticonData(ChatEmoticonData var0);

    private static native ErrorCode TTV_Java_Chat_ClearEmoticonData();

    private static native ErrorCode TTV_Java_Chat_DownloadBadgeData(String var0);

    private static native ErrorCode TTV_Java_Chat_GetBadgeData(String var0, ChatBadgeData var1);

    private static native ErrorCode TTV_Java_Chat_ClearBadgeData(String var0);

    private static native long TTV_Java_Chat_GetMessageFlushInterval();

    private static native ErrorCode TTV_Java_Chat_SetMessageFlushInterval(long var0);

    private static native long TTV_Java_Chat_GetUserChangeEventInterval();

    private static native ErrorCode TTV_Java_Chat_SetUserChangeEventInterval(long var0);

    @Override
    public ErrorCode initialize(HashSet<ChatTokenizationOption> hashSet, IChatAPIListener iChatAPIListener) {
        int n = ChatTokenizationOption.getNativeValue(hashSet);
        return StandardChatAPI.TTV_Java_Chat_Initialize(n, iChatAPIListener);
    }

    @Override
    public ErrorCode shutdown() {
        return StandardChatAPI.TTV_Java_Chat_Shutdown();
    }

    @Override
    public ErrorCode connect(String string, String string2, String string3, IChatChannelListener iChatChannelListener) {
        return StandardChatAPI.TTV_Java_Chat_Connect(string, string2, string3, iChatChannelListener);
    }

    @Override
    public ErrorCode connectAnonymous(String string, IChatChannelListener iChatChannelListener) {
        return StandardChatAPI.TTV_Java_Chat_ConnectAnonymous(string, iChatChannelListener);
    }

    @Override
    public ErrorCode disconnect(String string) {
        return StandardChatAPI.TTV_Java_Chat_Disconnect(string);
    }

    @Override
    public ErrorCode sendMessage(String string, String string2) {
        return StandardChatAPI.TTV_Java_Chat_SendMessage(string, string2);
    }

    @Override
    public ErrorCode flushEvents() {
        return StandardChatAPI.TTV_Java_Chat_FlushEvents();
    }

    @Override
    public ErrorCode downloadEmoticonData() {
        return StandardChatAPI.TTV_Java_Chat_DownloadEmoticonData();
    }

    @Override
    public ErrorCode getEmoticonData(ChatEmoticonData chatEmoticonData) {
        return StandardChatAPI.TTV_Java_Chat_GetEmoticonData(chatEmoticonData);
    }

    @Override
    public ErrorCode clearEmoticonData() {
        return StandardChatAPI.TTV_Java_Chat_ClearEmoticonData();
    }

    @Override
    public ErrorCode downloadBadgeData(String string) {
        return StandardChatAPI.TTV_Java_Chat_DownloadBadgeData(string);
    }

    @Override
    public ErrorCode getBadgeData(String string, ChatBadgeData chatBadgeData) {
        return StandardChatAPI.TTV_Java_Chat_GetBadgeData(string, chatBadgeData);
    }

    @Override
    public ErrorCode clearBadgeData(String string) {
        return StandardChatAPI.TTV_Java_Chat_ClearBadgeData(string);
    }

    @Override
    public int getMessageFlushInterval() {
        return (int)StandardChatAPI.TTV_Java_Chat_GetMessageFlushInterval();
    }

    @Override
    public ErrorCode setMessageFlushInterval(int n) {
        return StandardChatAPI.TTV_Java_Chat_SetMessageFlushInterval(n);
    }

    @Override
    public int getUserChangeEventInterval() {
        return (int)StandardChatAPI.TTV_Java_Chat_GetUserChangeEventInterval();
    }

    @Override
    public ErrorCode setUserChangeEventInterval(int n) {
        return StandardChatAPI.TTV_Java_Chat_SetUserChangeEventInterval(n);
    }
}

