/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityActionType;
import de.jcm.discordgamesdk.activity.ActivityJoinRequestReply;
import java.util.Objects;
import java.util.function.Consumer;

public class ActivityManager {
    private final long pointer;
    private final Core core;

    ActivityManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public Result registerCommand(String command) {
        return this.core.execute(() -> this.registerCommand(this.pointer, Objects.requireNonNull(command)));
    }

    public Result registerSteam(int steamId) {
        return this.core.execute(() -> this.registerSteam(this.pointer, steamId));
    }

    public void updateActivity(Activity activity) {
        this.updateActivity(activity, Core.DEFAULT_CALLBACK);
    }

    public void updateActivity(Activity activity, Consumer<Result> callback) {
        this.core.execute(() -> this.updateActivity(this.pointer, activity.getPointer(), Objects.requireNonNull(callback)));
    }

    public void clearActivity() {
        this.clearActivity(Core.DEFAULT_CALLBACK);
    }

    public void clearActivity(Consumer<Result> callback) {
        this.core.execute(() -> this.clearActivity(this.pointer, Objects.requireNonNull(callback)));
    }

    public void sendRequestReply(long userId, ActivityJoinRequestReply reply) {
        this.sendRequestReply(userId, reply, Core.DEFAULT_CALLBACK);
    }

    public void sendRequestReply(long userId, ActivityJoinRequestReply reply, Consumer<Result> callback) {
        this.core.execute(() -> this.sendRequestReply(this.pointer, userId, reply.ordinal(), Objects.requireNonNull(callback)));
    }

    public void sendInvite(long userId, ActivityActionType type, String content) {
        this.sendInvite(userId, type, content, Core.DEFAULT_CALLBACK);
    }

    public void sendInvite(long userId, ActivityActionType type, String content, Consumer<Result> callback) {
        this.core.execute(() -> this.sendInvite(this.pointer, userId, type.ordinal(), Objects.requireNonNull(content), Objects.requireNonNull(callback)));
    }

    public void acceptRequest(long userId) {
        this.acceptRequest(userId, Core.DEFAULT_CALLBACK);
    }

    public void acceptRequest(long userId, Consumer<Result> callback) {
        this.core.execute(() -> this.acceptRequest(this.pointer, userId, Objects.requireNonNull(callback)));
    }

    private native Result registerCommand(long var1, String var3);

    private native Result registerSteam(long var1, int var3);

    private native void updateActivity(long var1, long var3, Consumer<Result> var5);

    private native void clearActivity(long var1, Consumer<Result> var3);

    private native void sendRequestReply(long var1, long var3, int var5, Consumer<Result> var6);

    private native void sendInvite(long var1, long var3, int var5, String var6, Consumer<Result> var7);

    private native void acceptRequest(long var1, long var3, Consumer<Result> var5);
}

