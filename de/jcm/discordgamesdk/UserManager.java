/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.PremiumType;
import java.util.Objects;
import java.util.function.BiConsumer;

public class UserManager {
    private final long pointer;
    private final Core core;
    public static final int USER_FLAG_PARTNER = 2;
    public static final int USER_FLAG_HYPE_SQUAD_EVENTS = 4;
    public static final int USER_FLAG_HYPE_SQUAD_HOUSE1 = 64;
    public static final int USER_FLAG_HYPE_SQUAD_HOUSE2 = 128;
    public static final int USER_FLAG_HYPE_SQUAD_HOUSE3 = 256;

    UserManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public DiscordUser getCurrentUser() {
        Object ret = this.core.execute(() -> this.getCurrentUser(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (DiscordUser)ret;
    }

    public void getUser(long userId, BiConsumer<Result, DiscordUser> callback) {
        this.core.execute(() -> this.getUser(this.pointer, userId, Objects.requireNonNull(callback)));
    }

    public PremiumType getCurrentUserPremiumType() {
        Object ret = this.core.execute(() -> this.getCurrentUserPremiumType(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return PremiumType.values()[(Integer)ret];
    }

    public boolean currentUserHasFlag(int flag) {
        Object ret = this.core.execute(() -> this.currentUserHasFlag(this.pointer, flag));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Boolean)ret;
    }

    private native Object getCurrentUser(long var1);

    private native void getUser(long var1, long var3, BiConsumer<Result, DiscordUser> var5);

    private native Object getCurrentUserPremiumType(long var1);

    private native Object currentUserHasFlag(long var1, int var3);
}

