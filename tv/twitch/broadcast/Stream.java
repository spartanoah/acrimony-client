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
import tv.twitch.broadcast.FrameBuffer;
import tv.twitch.broadcast.IStatCallbacks;
import tv.twitch.broadcast.IStreamCallbacks;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.broadcast.StartFlags;
import tv.twitch.broadcast.StreamAPI;
import tv.twitch.broadcast.StreamInfoForSetting;
import tv.twitch.broadcast.VideoParams;

public class Stream {
    static Stream s_Instance = null;
    StreamAPI m_StreamAPI = null;

    public static Stream getInstance() {
        return s_Instance;
    }

    public Stream(StreamAPI streamAPI) {
        this.m_StreamAPI = streamAPI;
        if (s_Instance == null) {
            s_Instance = this;
        }
    }

    protected void finalize() {
        if (s_Instance == this) {
            s_Instance = null;
        }
    }

    public IStreamCallbacks getStreamCallbacks() {
        return this.m_StreamAPI.getStreamCallbacks();
    }

    public void setStreamCallbacks(IStreamCallbacks iStreamCallbacks) {
        this.m_StreamAPI.setStreamCallbacks(iStreamCallbacks);
    }

    public IStatCallbacks getStatCallbacks() {
        return this.m_StreamAPI.getStatCallbacks();
    }

    public void setStatCallbacks(IStatCallbacks iStatCallbacks) {
        this.m_StreamAPI.setStatCallbacks(iStatCallbacks);
    }

    public FrameBuffer allocateFrameBuffer(int n) {
        return new FrameBuffer(this.m_StreamAPI, n);
    }

    public ErrorCode memsetFrameBuffer(FrameBuffer frameBuffer, int n) {
        return this.m_StreamAPI.memsetFrameBuffer(frameBuffer.getAddress(), frameBuffer.getSize(), n);
    }

    public ErrorCode randomizeFrameBuffer(FrameBuffer frameBuffer) {
        return this.m_StreamAPI.randomizeFrameBuffer(frameBuffer.getAddress(), frameBuffer.getSize());
    }

    public ErrorCode requestAuthToken(AuthParams authParams, HashSet<AuthFlag> hashSet) {
        ErrorCode errorCode = this.m_StreamAPI.requestAuthToken(authParams, hashSet);
        return errorCode;
    }

    public ErrorCode login(AuthToken authToken) {
        ErrorCode errorCode = this.m_StreamAPI.login(authToken);
        return errorCode;
    }

    public ErrorCode getIngestServers(AuthToken authToken) {
        return this.m_StreamAPI.getIngestServers(authToken);
    }

    public ErrorCode getUserInfo(AuthToken authToken) {
        return this.m_StreamAPI.getUserInfo(authToken);
    }

    public ErrorCode getStreamInfo(AuthToken authToken, String string) {
        return this.m_StreamAPI.getStreamInfo(authToken, string);
    }

    public ErrorCode setStreamInfo(AuthToken authToken, String string, StreamInfoForSetting streamInfoForSetting) {
        return this.m_StreamAPI.setStreamInfo(authToken, string, streamInfoForSetting);
    }

    public ErrorCode getArchivingState(AuthToken authToken) {
        return this.m_StreamAPI.getArchivingState(authToken);
    }

    public ErrorCode runCommercial(AuthToken authToken) {
        return this.m_StreamAPI.runCommercial(authToken);
    }

    public ErrorCode setVolume(AudioDeviceType audioDeviceType, float f) {
        return this.m_StreamAPI.setVolume(audioDeviceType, f);
    }

    public float getVolume(AudioDeviceType audioDeviceType) {
        return this.m_StreamAPI.getVolume(audioDeviceType);
    }

    public ErrorCode getGameNameList(String string) {
        return this.m_StreamAPI.getGameNameList(string);
    }

    public ErrorCode getDefaultParams(VideoParams videoParams) {
        return this.m_StreamAPI.getDefaultParams(videoParams);
    }

    public int[] getMaxResolution(int n, int n2, float f, float f2) {
        return this.m_StreamAPI.getMaxResolution(n, n2, f, f2);
    }

    public ErrorCode pollTasks() {
        ErrorCode errorCode = this.m_StreamAPI.pollTasks();
        return errorCode;
    }

    public ErrorCode pollStats() {
        ErrorCode errorCode = this.m_StreamAPI.pollStats();
        return errorCode;
    }

    public ErrorCode sendActionMetaData(AuthToken authToken, String string, long l, String string2, String string3) {
        ErrorCode errorCode = this.m_StreamAPI.sendActionMetaData(authToken, string, l, string2, string3);
        return errorCode;
    }

    public long sendStartSpanMetaData(AuthToken authToken, String string, long l, String string2, String string3) {
        long l2 = this.m_StreamAPI.sendStartSpanMetaData(authToken, string, l, string2, string3);
        return l2;
    }

    public ErrorCode sendEndSpanMetaData(AuthToken authToken, String string, long l, long l2, String string2, String string3) {
        ErrorCode errorCode = this.m_StreamAPI.sendEndSpanMetaData(authToken, string, l, l2, string2, string3);
        return errorCode;
    }

    public ErrorCode submitVideoFrame(FrameBuffer frameBuffer) {
        ErrorCode errorCode = this.m_StreamAPI.submitVideoFrame(frameBuffer.getAddress());
        return errorCode;
    }

    public ErrorCode captureFrameBuffer_ReadPixels(FrameBuffer frameBuffer) {
        ErrorCode errorCode = this.m_StreamAPI.captureFrameBuffer_ReadPixels(frameBuffer.getAddress());
        return errorCode;
    }

    public ErrorCode start(VideoParams videoParams, AudioParams audioParams, IngestServer ingestServer, StartFlags startFlags, boolean bl) {
        if (startFlags == null) {
            startFlags = StartFlags.None;
        }
        ErrorCode errorCode = this.m_StreamAPI.start(videoParams, audioParams, ingestServer, startFlags.getValue(), bl);
        return errorCode;
    }

    public ErrorCode stop(boolean bl) {
        ErrorCode errorCode = this.m_StreamAPI.stop(bl);
        return errorCode;
    }

    public ErrorCode pauseVideo() {
        ErrorCode errorCode = this.m_StreamAPI.pauseVideo();
        return errorCode;
    }

    public long getStreamTime() {
        return this.m_StreamAPI.getStreamTime();
    }
}

