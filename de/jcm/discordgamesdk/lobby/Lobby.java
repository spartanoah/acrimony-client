/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.lobby;

import de.jcm.discordgamesdk.lobby.LobbyType;

public class Lobby {
    private final long id;
    private final LobbyType type;
    private final long ownerId;
    private final String secret;
    private final int capacity;
    private final boolean locked;

    public Lobby(long id, int type, long ownerId, String secret, int capacity, boolean locked) {
        this.id = id;
        this.type = LobbyType.values()[type - 1];
        this.ownerId = ownerId;
        this.secret = secret;
        this.capacity = capacity;
        this.locked = locked;
        if (this.secret.getBytes().length >= 128) {
            throw new IllegalArgumentException("max secret length is 127");
        }
    }

    public long getId() {
        return this.id;
    }

    public LobbyType getType() {
        return this.type;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public String getSecret() {
        return this.secret;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public String toString() {
        return "Lobby{id=" + this.id + ", type=" + (Object)((Object)this.type) + ", ownerId=" + this.ownerId + ", secret='" + this.secret + '\'' + ", capacity=" + this.capacity + ", locked=" + this.locked + '}';
    }
}

