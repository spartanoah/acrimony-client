/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.lobby;

import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;

public class LobbyMemberTransaction {
    private final long pointer;

    LobbyMemberTransaction(long pointer) {
        this.pointer = pointer;
    }

    public long getPointer() {
        return this.pointer;
    }

    public void setMetadata(String key, String value) {
        if (key.getBytes().length >= 256) {
            throw new IllegalArgumentException("max key length is 255");
        }
        if (value.getBytes().length >= 4096) {
            throw new IllegalArgumentException("max value length is 4095");
        }
        Result result = this.setMetadata(this.pointer, key, value);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void deleteMetadata(String key) {
        if (key.getBytes().length >= 256) {
            throw new IllegalArgumentException("max key length is 255");
        }
        Result result = this.deleteMetadata(this.pointer, key);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    private native Result setMetadata(long var1, String var3, String var4);

    private native Result deleteMetadata(long var1, String var3);
}

