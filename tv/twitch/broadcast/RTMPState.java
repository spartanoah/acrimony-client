/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum RTMPState {
    Invalid(-1),
    Idle(0),
    Initialize(1),
    Handshake(2),
    Connect(3),
    CreateStream(4),
    Publish(5),
    SendVideo(6),
    Shutdown(7),
    Error(8);

    private static Map<Integer, RTMPState> s_Map;
    private int m_Value;

    public static RTMPState lookupValue(int n) {
        RTMPState rTMPState = s_Map.get(n);
        return rTMPState;
    }

    private RTMPState(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, RTMPState>();
        EnumSet<RTMPState> enumSet = EnumSet.allOf(RTMPState.class);
        for (RTMPState rTMPState : enumSet) {
            s_Map.put(rTMPState.getValue(), rTMPState);
        }
    }
}

