/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AudioDeviceType {
    TTV_PLAYBACK_DEVICE(0),
    TTV_RECORDER_DEVICE(1),
    TTV_PASSTHROUGH_DEVICE(2),
    TTV_DEVICE_NUM(3);

    private static Map<Integer, AudioDeviceType> s_Map;
    private int m_Value;

    public static AudioDeviceType lookupValue(int n) {
        AudioDeviceType audioDeviceType = s_Map.get(n);
        return audioDeviceType;
    }

    private AudioDeviceType(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, AudioDeviceType>();
        EnumSet<AudioDeviceType> enumSet = EnumSet.allOf(AudioDeviceType.class);
        for (AudioDeviceType audioDeviceType : enumSet) {
            s_Map.put(audioDeviceType.getValue(), audioDeviceType);
        }
    }
}

