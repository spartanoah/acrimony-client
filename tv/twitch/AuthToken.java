/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package tv.twitch;

public class AuthToken {
    public String data;

    public boolean getIsValid() {
        return this.data != null && this.data.length() > 0;
    }
}

