/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import tv.twitch.broadcast.EncodingCpuUsage;
import tv.twitch.broadcast.PixelFormat;

public class VideoParams {
    public int outputWidth;
    public int outputHeight;
    public PixelFormat pixelFormat = PixelFormat.TTV_PF_BGRA;
    public int maxKbps;
    public int targetFps;
    public EncodingCpuUsage encodingCpuUsage = EncodingCpuUsage.TTV_ECU_HIGH;
    public boolean disableAdaptiveBitrate = false;
    public boolean verticalFlip = false;

    public VideoParams clone() {
        VideoParams videoParams = new VideoParams();
        videoParams.outputWidth = this.outputWidth;
        videoParams.outputHeight = this.outputHeight;
        videoParams.pixelFormat = this.pixelFormat;
        videoParams.maxKbps = this.maxKbps;
        videoParams.targetFps = this.targetFps;
        videoParams.encodingCpuUsage = this.encodingCpuUsage;
        videoParams.disableAdaptiveBitrate = this.disableAdaptiveBitrate;
        videoParams.verticalFlip = this.verticalFlip;
        return videoParams;
    }
}

