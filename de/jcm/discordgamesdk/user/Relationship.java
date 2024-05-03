/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.user;

import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.OnlineStatus;
import de.jcm.discordgamesdk.user.Presence;
import de.jcm.discordgamesdk.user.RelationshipType;

public class Relationship {
    private final RelationshipType type;
    private final DiscordUser user;
    private final Presence presence;

    private Relationship(RelationshipType type, DiscordUser user, Presence presence) {
        this.type = type;
        this.user = user;
        this.presence = presence;
    }

    public RelationshipType getType() {
        return this.type;
    }

    public DiscordUser getUser() {
        return this.user;
    }

    public Presence getPresence() {
        return this.presence;
    }

    public String toString() {
        return "Relationship{type=" + (Object)((Object)this.type) + ", user=" + this.user + ", presence=" + this.presence + '}';
    }

    static Relationship createRelationship(int type, DiscordUser user, int status, long activity) {
        RelationshipType type1 = RelationshipType.values()[type];
        OnlineStatus status1 = OnlineStatus.values()[status];
        Activity activity1 = new Activity(activity);
        Presence presence = new Presence(status1, activity1);
        return new Relationship(type1, user, presence);
    }
}

