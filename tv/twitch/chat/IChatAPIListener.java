/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.chat;

import tv.twitch.ErrorCode;

public interface IChatAPIListener {
    public void chatInitializationCallback(ErrorCode var1);

    public void chatShutdownCallback(ErrorCode var1);

    public void chatEmoticonDataDownloadCallback(ErrorCode var1);
}

