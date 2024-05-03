/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AudioSampleFormat {
    TTV_ASF_PCM_S16(0);

    private static Map<Integer, AudioSampleFormat> s_Map;
    private int m_Value;

    public static AudioSampleFormat lookupValue(int n) {
        AudioSampleFormat audioSampleFormat = s_Map.get(n);
        return audioSampleFormat;
    }

    private AudioSampleFormat(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, AudioSampleFormat>();
        EnumSet<AudioSampleFormat> enumSet = EnumSet.allOf(AudioSampleFormat.class);
        for (AudioSampleFormat audioSampleFormat : enumSet) {
            s_Map.put(audioSampleFormat.getValue(), audioSampleFormat);
        }
    }
}

