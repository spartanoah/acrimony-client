/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import tv.twitch.ErrorCode;
import tv.twitch.chat.ChatChannelInfo;
import tv.twitch.chat.ChatEvent;
import tv.twitch.chat.ChatRawMessage;
import tv.twitch.chat.ChatTokenizedMessage;
import tv.twitch.chat.ChatUserInfo;

public interface IChatChannelListener {
    public void chatStatusCallback(String var1, ErrorCode var2);

    public void chatChannelMembershipCallback(String var1, ChatEvent var2, ChatChannelInfo var3);

    public void chatChannelUserChangeCallback(String var1, ChatUserInfo[] var2, ChatUserInfo[] var3, ChatUserInfo[] var4);

    public void chatChannelRawMessageCallback(String var1, ChatRawMessage[] var2);

    public void chatChannelTokenizedMessageCallback(String var1, ChatTokenizedMessage[] var2);

    public void chatClearCallback(String var1, String var2);

    public void chatBadgeDataDownloadCallback(String var1, ErrorCode var2);
}

