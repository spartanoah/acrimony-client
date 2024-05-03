/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.lobby;

import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.lobby.LobbyType;

public class LobbyTransaction {
    private final long pointer;

    LobbyTransaction(long pointer) {
        this.pointer = pointer;
    }

    public long getPointer() {
        return this.pointer;
    }

    public void setType(LobbyType type) {
        Result result = this.setType(this.pointer, type.ordinal() + 1);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void setOwner(long ownerId) {
        Result result = this.setOwner(this.pointer, ownerId);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public void setCapacity(int capacity) {
        Result result = this.setCapacity(this.pointer, capacity);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
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

    public void setLocked(boolean locked) {
        Result result = this.setLocked(this.pointer, locked);
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    private native Result setType(long var1, int var3);

    private native Result setOwner(long var1, long var3);

    private native Result setCapacity(long var1, int var3);

    private native Result setMetadata(long var1, String var3, String var4);

    private native Result deleteMetadata(long var1, String var3);

    private native Result setLocked(long var1, boolean var3);
}

