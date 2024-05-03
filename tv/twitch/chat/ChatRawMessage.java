/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import java.util.HashSet;
import tv.twitch.chat.ChatUserMode;
import tv.twitch.chat.ChatUserSubscription;

public class ChatRawMessage {
    public String userName = null;
    public String message = null;
    public HashSet<ChatUserMode> modes = new HashSet();
    public HashSet<ChatUserSubscription> subscriptions = new HashSet();
    public int nameColorARGB = 0;
    public boolean action = false;
}

