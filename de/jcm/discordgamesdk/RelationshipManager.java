/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.ActivityType;
import de.jcm.discordgamesdk.user.OnlineStatus;
import de.jcm.discordgamesdk.user.Relationship;
import de.jcm.discordgamesdk.user.RelationshipType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class RelationshipManager {
    public static final Predicate<Relationship> NO_FILTER = r -> true;
    public static final Predicate<Relationship> FRIEND_FILTER = r -> r.getType() == RelationshipType.FRIEND;
    public static final Predicate<Relationship> ONLINE_FILTER = r -> r.getPresence().getStatus() == OnlineStatus.ONLINE;
    public static final Predicate<Relationship> OFFLINE_FILTER = r -> r.getPresence().getStatus() == OnlineStatus.OFFLINE;
    public static final Predicate<Relationship> SPECIAL_FILTER = r -> r.getPresence().getActivity().getType() == ActivityType.PLAYING && r.getPresence().getActivity().getApplicationId() != 0L || r.getPresence().getActivity().getType() != ActivityType.PLAYING;
    private final long pointer;
    private final Core core;

    RelationshipManager(long pointer, Core core) {
        this.pointer = pointer;
        this.core = core;
    }

    public Relationship getWith(long userId) {
        Object ret = this.core.execute(() -> this.get(this.pointer, userId));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Relationship)ret;
    }

    public void filter(Predicate<Relationship> filter) {
        this.core.execute(() -> this.filter(this.pointer, Objects.requireNonNull(filter)));
    }

    public int count() {
        Object ret = this.core.execute(() -> this.count(this.pointer));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Integer)ret;
    }

    public Relationship getAt(int index) {
        Object ret = this.core.execute(() -> this.getAt(this.pointer, index));
        if (ret instanceof Result) {
            throw new GameSDKException((Result)((Object)ret));
        }
        return (Relationship)ret;
    }

    public List<Relationship> asList() {
        int count = this.count();
        Relationship[] relationships = new Relationship[count];
        for (int i = 0; i < relationships.length; ++i) {
            relationships[i] = this.getAt(i);
        }
        return Arrays.asList(relationships);
    }

    private native void filter(long var1, Predicate<Relationship> var3);

    private native Object count(long var1);

    private native Object get(long var1, long var3);

    private native Object getAt(long var1, int var3);
}

