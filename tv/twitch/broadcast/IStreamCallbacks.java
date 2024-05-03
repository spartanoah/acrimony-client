/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import tv.twitch.AuthToken;
import tv.twitch.ErrorCode;
import tv.twitch.broadcast.ArchivingState;
import tv.twitch.broadcast.ChannelInfo;
import tv.twitch.broadcast.GameInfoList;
import tv.twitch.broadcast.IngestList;
import tv.twitch.broadcast.StreamInfo;
import tv.twitch.broadcast.UserInfo;

public interface IStreamCallbacks {
    public void requestAuthTokenCallback(ErrorCode var1, AuthToken var2);

    public void loginCallback(ErrorCode var1, ChannelInfo var2);

    public void getIngestServersCallback(ErrorCode var1, IngestList var2);

    public void getUserInfoCallback(ErrorCode var1, UserInfo var2);

    public void getStreamInfoCallback(ErrorCode var1, StreamInfo var2);

    public void getArchivingStateCallback(ErrorCode var1, ArchivingState var2);

    public void runCommercialCallback(ErrorCode var1);

    public void setStreamInfoCallback(ErrorCode var1);

    public void getGameNameListCallback(ErrorCode var1, GameInfoList var2);

    public void bufferUnlockCallback(long var1);

    public void startCallback(ErrorCode var1);

    public void stopCallback(ErrorCode var1);

    public void sendActionMetaDataCallback(ErrorCode var1);

    public void sendStartSpanMetaDataCallback(ErrorCode var1);

    public void sendEndSpanMetaDataCallback(ErrorCode var1);
}

