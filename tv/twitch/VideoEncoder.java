/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum VideoEncoder {
    TTV_VID_ENC_DISABLE(-2),
    TTV_VID_ENC_DEFAULT(-1),
    TTV_VID_ENC_INTEL(0),
    TTV_VID_ENC_APPLE(2),
    TTV_VID_ENC_PLUGIN(100);

    private static Map<Integer, VideoEncoder> s_Map;
    private int m_Value;

    public static VideoEncoder lookupValue(int n) {
        VideoEncoder videoEncoder = s_Map.get(n);
        return videoEncoder;
    }

    private VideoEncoder(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, VideoEncoder>();
        EnumSet<VideoEncoder> enumSet = EnumSet.allOf(VideoEncoder.class);
        for (VideoEncoder videoEncoder : enumSet) {
            s_Map.put(videoEncoder.getValue(), videoEncoder);
        }
    }
}

