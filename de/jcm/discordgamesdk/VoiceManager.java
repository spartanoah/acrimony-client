/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.voice.VoiceInputMode;
import java.util.Objects;
import java.util.function.Consumer;

public class VoiceManager {
    private final long pointer;
    private final Core core;

    VoiceManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public VoiceInputMode getInputMode() {
        Object ret = this.core.execute(() -> this.getInputMode(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (VoiceInputMode)ret;
    }

    public void setInputMode(VoiceInputMode inputMode, Consumer<Result> callback) {
        this.core.execute(() -> this.setInputMode(this.pointer, inputMode, Objects.requireNonNull(callback)));
    }

    public void setInputMode(VoiceInputMode inputMode) {
        this.setInputMode(inputMode, Core.DEFAULT_CALLBACK);
    }

    public boolean isSelfMute() {
        Object ret = this.core.execute(() -> this.isSelfMute(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Boolean)ret;
    }

    public void setSelfMute(boolean selfMute) {
        Result result = this.core.execute(() -> this.setSelfMute(this.pointer, selfMute));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public boolean isSelfDeaf() {
        Object ret = this.core.execute(() -> this.isSelfDeaf(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Boolean)ret;
    }

    public void setSelfDeaf(boolean selfDeaf) {
        Result result = this.core.execute(() -> this.setSelfDeaf(this.pointer, selfDeaf));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public boolean isLocalMute(long userId) {
        Object ret = this.core.execute(() -> this.isLocalMute(this.pointer, userId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Boolean)ret;
    }

    public void setLocalMute(long userId, boolean mute) {
        Result result = this.core.execute(() -> this.setLocalMute(this.pointer, userId, mute));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    public int getLocalVolume(long userId) {
        Object ret = this.core.execute(() -> this.getLocalVolume(this.pointer, userId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Integer)ret;
    }

    public void setLocalVolume(long userId, int volume) {
        if (volume < 0 || volume > 200) {
            throw new IllegalArgumentException("volume out of range: " + volume);
        }
        Result result = this.core.execute(() -> this.setLocalVolume(this.pointer, userId, (byte)volume));
        if (result != Result.OK) {
            throw new GameSDKException(result);
        }
    }

    private native Object getInputMode(long var1);

    private native void setInputMode(long var1, VoiceInputMode var3, Consumer<Result> var4);

    private native Object isSelfMute(long var1);

    private native Result setSelfMute(long var1, boolean var3);

    private native Object isSelfDeaf(long var1);

    private native Result setSelfDeaf(long var1, boolean var3);

    private native Object isLocalMute(long var1, long var3);

    private native Result setLocalMute(long var1, long var3, boolean var5);

    private native Object getLocalVolume(long var1, long var3);

    private native Result setLocalVolume(long var1, long var3, byte var5);
}

