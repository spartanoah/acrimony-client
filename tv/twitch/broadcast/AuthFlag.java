/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch.broadcast;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public enum AuthFlag {
    TTV_AuthOption_None(0),
    TTV_AuthOption_Broadcast(1),
    TTV_AuthOption_Chat(2);

    private static Map<Integer, AuthFlag> s_Map;
    private int m_Value;

    public static AuthFlag lookupValue(int n) {
        AuthFlag authFlag = s_Map.get(n);
        return authFlag;
    }

    public static int getNativeValue(HashSet<AuthFlag> hashSet) {
        if (hashSet == null) {
            return TTV_AuthOption_None.getValue();
        }
        int n = 0;
        for (AuthFlag authFlag : hashSet) {
            if (authFlag == null) continue;
            n |= authFlag.getValue();
        }
        return n;
    }

    private AuthFlag(int n2) {
        this.m_Value = n2;
    }

    public int getValue() {
        return this.m_Value;
    }

    static {
        s_Map = new HashMap<Integer, AuthFlag>();
        EnumSet<AuthFlag> enumSet = EnumSet.allOf(AuthFlag.class);
        for (AuthFlag authFlag : enumSet) {
            s_Map.put(authFlag.getValue(), authFlag);
        }
    }
}

