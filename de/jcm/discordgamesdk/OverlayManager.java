/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.ActivityActionType;
import java.util.Objects;
import java.util.function.Consumer;

public class OverlayManager {
    private final long pointer;
    private final Core core;

    OverlayManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public boolean isEnabled() {
        return this.core.execute(() -> this.isEnabled(this.pointer));
    }

    public boolean isLocked() {
        return this.core.execute(() -> this.isLocked(this.pointer));
    }

    public void setLocked(boolean locked) {
        this.setLocked(locked, Core.DEFAULT_CALLBACK);
    }

    public void setLocked(boolean locked, Consumer<Result> callback) {
        this.core.execute(() -> this.setLocked(this.pointer, locked, Objects.requireNonNull(callback)));
    }

    public void openActivityInvite(ActivityActionType type) {
        this.openActivityInvite(type, Core.DEFAULT_CALLBACK);
    }

    public void openActivityInvite(ActivityActionType type, Consumer<Result> callback) {
        this.core.execute(() -> this.openActivityInvite(this.pointer, type.ordinal(), Objects.requireNonNull(callback)));
    }

    public void openGuildInvite(String code) {
        this.openGuildInvite(code, Core.DEFAULT_CALLBACK);
    }

    public void openGuildInvite(String code, Consumer<Result> callback) {
        this.core.execute(() -> this.openGuildInvite(this.pointer, code, Objects.requireNonNull(callback)));
    }

    public void openVoiceSettings() {
        this.openVoiceSettings(Core.DEFAULT_CALLBACK);
    }

    public void openVoiceSettings(Consumer<Result> callback) {
        this.core.execute(() -> this.openVoiceSettings(this.pointer, Objects.requireNonNull(callback)));
    }

    private native boolean isEnabled(long var1);

    private native boolean isLocked(long var1);

    private native void setLocked(long var1, boolean var3, Consumer<Result> var4);

    private native void openActivityInvite(long var1, int var3, Consumer<Result> var4);

    private native void openGuildInvite(long var1, String var3, Consumer<Result> var4);

    private native void openVoiceSettings(long var1, Consumer<Result> var3);
}

