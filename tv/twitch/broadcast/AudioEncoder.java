/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AudioEncoder {
    TTV_AUD_ENC_DEFAULT(-1),
    TTV_AUD_ENC_LAMEMP3(0),
    TTV_AUD_ENC_APPLEAAC(1);

    private static Map<Integer, AudioEncoder> s_Map;
    private int m_Value;

    public static AudioEncoder lookupValue(int n) {
        AudioEncoder audioEncoder = s_Map.get(n);
        return audioEncoder;
    }

    private AudioEncoder(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, AudioEncoder>();
        EnumSet<AudioEncoder> enumSet = EnumSet.allOf(AudioEncoder.class);
        for (AudioEncoder audioEncoder : enumSet) {
            s_Map.put(audioEncoder.getValue(), audioEncoder);
        }
    }
}

