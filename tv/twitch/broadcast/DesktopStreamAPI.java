/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.HashSet;
import tv.twitch.AuthToken;
import tv.twitch.ErrorCode;
import tv.twitch.broadcast.AudioDeviceType;
import tv.twitch.broadcast.AudioParams;
import tv.twitch.broadcast.AuthFlag;
import tv.twitch.broadcast.AuthParams;
import tv.twitch.broadcast.IStatCallbacks;
import tv.twitch.broadcast.IStreamCallbacks;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.StreamAPI;
import tv.twitch.broadcast.StreamInfoForSetting;
import tv.twitch.broadcast.VideoParams;

public class DesktopStreamAPI
extends StreamAPI {
    protected void finalize() {
        DesktopStreamAPI.TTV_Java_SetStreamCallbacks(null);
        DesktopStreamAPI.TTV_Java_SetStatCallbacks(null);
    }

    private static native void TTV_Java_SetStreamCallbacks(IStreamCallbacks var0);

    private static native IStreamCallbacks TTV_Java_GetStreamCallbacks();

    private static native void TTV_Java_SetStatCallbacks(IStatCallbacks var0);

    private static native IStatCallbacks TTV_Java_GetStatCallbacks();

    private static native ErrorCode TTV_Java_RequestAuthToken(AuthParams var0, int var1);

    private static native ErrorCode TTV_Java_Login(AuthToken var0);

    private static native ErrorCode TTV_Java_GetIngestServers(AuthToken var0);

    private static native ErrorCode TTV_Java_GetUserInfo(AuthToken var0);

    private static native ErrorCode TTV_Java_GetStreamInfo(AuthToken var0, String var1);

    private static native ErrorCode TTV_Java_SetStreamInfo(AuthToken var0, String var1, StreamInfoForSetting var2);

    private static native ErrorCode TTV_Java_GetArchivingState(AuthToken var0);

    private static native ErrorCode TTV_Java_RunCommercial(AuthToken var0);

    private static native ErrorCode TTV_Java_SetVolume(AudioDeviceType var0, float var1);

    private static native float TTV_Java_GetVolume(AudioDeviceType var0);

    private static native ErrorCode TTV_Java_GetGameNameList(String var0);

    private static native ErrorCode TTV_Java_GetDefaultParams(VideoParams var0);

    private static native ErrorCode TTV_GetMaxResolution(int var0, int var1, float var2, float var3, int[] var4);

    private static native ErrorCode TTV_Java_PollTasks();

    private static native ErrorCode TTV_Java_PollStats();

    private static native ErrorCode TTV_Java_Init(int var0);

    private static native ErrorCode TTV_Java_Shutdown();

    private static native ErrorCode TTV_Java_SendActionMetaData(AuthToken var0, String var1, long var2, String var4, String var5);

    private static native long TTV_Java_SendStartSpanMetaData(AuthToken var0, String var1, long var2, String var4, String var5);

    private static native ErrorCode TTV_Java_SendEndSpanMetaData(AuthToken var0, String var1, long var2, long var4, String var6, String var7);

    private static native ErrorCode TTV_Java_SubmitVideoFrame(long var0);

    private static native ErrorCode TTV_Java_Start(VideoParams var0, AudioParams var1, IngestServer var2, int var3, boolean var4);

    private static native ErrorCode TTV_Java_Stop(boolean var0);

    private static native ErrorCode TTV_Java_PauseVideo();

    private static native long TTV_Java_AllocateFrameBuffer(int var0);

    private static native ErrorCode TTV_Java_FreeFrameBuffer(long var0);

    private static native ErrorCode TTV_Java_MemsetFrameBuffer(long var0, int var2, int var3);

    private static native ErrorCode TTV_Java_RandomizeFrameBuffer(long var0, int var2);

    private static native ErrorCode TTV_Java_CaptureFrameBuffer_ReadPixels(long var0);

    private static native long TTV_Java_GetStreamTime();

    @Override
    public void setStreamCallbacks(IStreamCallbacks iStreamCallbacks) {
        DesktopStreamAPI.TTV_Java_SetStreamCallbacks(iStreamCallbacks);
    }

    @Override
    public IStreamCallbacks getStreamCallbacks() {
        return DesktopStreamAPI.TTV_Java_GetStreamCallbacks();
    }

    @Override
    public void setStatCallbacks(IStatCallbacks iStatCallbacks) {
        DesktopStreamAPI.TTV_Java_SetStatCallbacks(iStatCallbacks);
    }

    @Override
    public IStatCallbacks getStatCallbacks() {
        return DesktopStreamAPI.TTV_Java_GetStatCallbacks();
    }

    @Override
    public ErrorCode requestAuthToken(AuthParams authParams, HashSet<AuthFlag> hashSet) {
        if (hashSet == null) {
            hashSet = new HashSet();
        }
        int n = AuthFlag.getNativeValue(hashSet);
        return DesktopStreamAPI.TTV_Java_RequestAuthToken(authParams, n);
    }

    @Override
    public ErrorCode login(AuthToken authToken) {
        return DesktopStreamAPI.TTV_Java_Login(authToken);
    }

    @Override
    public ErrorCode getIngestServers(AuthToken authToken) {
        return DesktopStreamAPI.TTV_Java_GetIngestServers(authToken);
    }

    @Override
    public ErrorCode getUserInfo(AuthToken authToken) {
        return DesktopStreamAPI.TTV_Java_GetUserInfo(authToken);
    }

    @Override
    public ErrorCode getStreamInfo(AuthToken authToken, String string) {
        return DesktopStreamAPI.TTV_Java_GetStreamInfo(authToken, string);
    }

    @Override
    public ErrorCode setStreamInfo(AuthToken authToken, String string, StreamInfoForSetting streamInfoForSetting) {
        return DesktopStreamAPI.TTV_Java_SetStreamInfo(authToken, string, streamInfoForSetting);
    }

    @Override
    public ErrorCode getArchivingState(AuthToken authToken) {
        return DesktopStreamAPI.TTV_Java_GetArchivingState(authToken);
    }

    @Override
    public ErrorCode runCommercial(AuthToken authToken) {
        return DesktopStreamAPI.TTV_Java_RunCommercial(authToken);
    }

    @Override
    public ErrorCode setVolume(AudioDeviceType audioDeviceType, float f) {
        return DesktopStreamAPI.TTV_Java_SetVolume(audioDeviceType, f);
    }

    @Override
    public float getVolume(AudioDeviceType audioDeviceType) {
        return DesktopStreamAPI.TTV_Java_GetVolume(audioDeviceType);
    }

    @Override
    public ErrorCode getGameNameList(String string) {
        return DesktopStreamAPI.TTV_Java_GetGameNameList(string);
    }

    @Override
    public ErrorCode getDefaultParams(VideoParams videoParams) {
        return DesktopStreamAPI.TTV_Java_GetDefaultParams(videoParams);
    }

    @Override
    public int[] getMaxResolution(int n, int n2, float f, float f2) {
        int[] nArray = new int[]{0, 0};
        DesktopStreamAPI.TTV_GetMaxResolution(n, n2, f, f2, nArray);
        return nArray;
    }

    @Override
    public ErrorCode pollTasks() {
        return DesktopStreamAPI.TTV_Java_PollTasks();
    }

    @Override
    public ErrorCode pollStats() {
        return DesktopStreamAPI.TTV_Java_PollStats();
    }

    @Override
    public ErrorCode sendActionMetaData(AuthToken authToken, String string, long l, String string2, String string3) {
        return DesktopStreamAPI.TTV_Java_SendActionMetaData(authToken, string, l, string2, string3);
    }

    @Override
    public long sendStartSpanMetaData(AuthToken authToken, String string, long l, String string2, String string3) {
        return DesktopStreamAPI.TTV_Java_SendStartSpanMetaData(authToken, string, l, string2, string3);
    }

    @Override
    public ErrorCode sendEndSpanMetaData(AuthToken authToken, String string, long l, long l2, String string2, String string3) {
        return DesktopStreamAPI.TTV_Java_SendEndSpanMetaData(authToken, string, l, l2, string2, string3);
    }

    @Override
    public ErrorCode submitVideoFrame(long l) {
        return DesktopStreamAPI.TTV_Java_SubmitVideoFrame(l);
    }

    @Override
    public ErrorCode start(VideoParams videoParams, AudioParams audioParams, IngestServer ingestServer, int n, boolean bl) {
        return DesktopStreamAPI.TTV_Java_Start(videoParams, audioParams, ingestServer, n, bl);
    }

    @Override
    public ErrorCode stop(boolean bl) {
        return DesktopStreamAPI.TTV_Java_Stop(bl);
    }

    @Override
    public ErrorCode pauseVideo() {
        return DesktopStreamAPI.TTV_Java_PauseVideo();
    }

    @Override
    public long allocateFrameBuffer(int n) {
        return DesktopStreamAPI.TTV_Java_AllocateFrameBuffer(n);
    }

    @Override
    public ErrorCode freeFrameBuffer(long l) {
        return DesktopStreamAPI.TTV_Java_FreeFrameBuffer(l);
    }

    @Override
    public ErrorCode memsetFrameBuffer(long l, int n, int n2) {
        return DesktopStreamAPI.TTV_Java_MemsetFrameBuffer(l, n, n2);
    }

    @Override
    public ErrorCode randomizeFrameBuffer(long l, int n) {
        return DesktopStreamAPI.TTV_Java_RandomizeFrameBuffer(l, n);
    }

    @Override
    public ErrorCode captureFrameBuffer_ReadPixels(long l) {
        return DesktopStreamAPI.TTV_Java_CaptureFrameBuffer_ReadPixels(l);
    }

    @Override
    public long getStreamTime() {
        return DesktopStreamAPI.TTV_Java_GetStreamTime();
    }
}

