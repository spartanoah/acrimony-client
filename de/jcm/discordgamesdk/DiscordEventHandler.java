/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.DiscordEventAdapter;
import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.Relationship;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DiscordEventHandler
extends DiscordEventAdapter {
    private final List<DiscordEventAdapter> listeners = new CopyOnWriteArrayList<DiscordEventAdapter>();

    public void addListener(DiscordEventAdapter listener) {
        this.listeners.add(listener);
    }

    public boolean removeListener(DiscordEventAdapter listener) {
        return this.listeners.remove(listener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

    @Override
    public void onActivityJoin(String secret) {
        this.listeners.forEach(l -> l.onActivityJoin(secret));
    }

    @Override
    public void onActivitySpectate(String secret) {
        this.listeners.forEach(l -> l.onActivitySpectate(secret));
    }

    @Override
    public void onActivityJoinRequest(DiscordUser user) {
        this.listeners.forEach(l -> l.onActivityJoinRequest(user));
    }

    @Override
    public void onCurrentUserUpdate() {
        this.listeners.forEach(DiscordEventAdapter::onCurrentUserUpdate);
    }

    @Override
    public void onOverlayToggle(boolean locked) {
        this.listeners.forEach(l -> l.onOverlayToggle(locked));
    }

    @Override
    public void onRelationshipRefresh() {
        this.listeners.forEach(DiscordEventAdapter::onRelationshipRefresh);
    }

    @Override
    public void onRelationshipUpdate(Relationship relationship) {
        this.listeners.forEach(l -> l.onRelationshipUpdate(relationship));
    }

    @Override
    public void onLobbyUpdate(long lobbyId) {
        this.listeners.forEach(l -> l.onLobbyUpdate(lobbyId));
    }

    @Override
    public void onLobbyDelete(long lobbyId, int reason) {
        this.listeners.forEach(l -> l.onLobbyDelete(lobbyId, reason));
    }

    @Override
    public void onMemberConnect(long lobbyId, long userId) {
        this.listeners.forEach(l -> l.onMemberConnect(lobbyId, userId));
    }

    @Override
    public void onMemberUpdate(long lobbyId, long userId) {
        this.listeners.forEach(l -> l.onMemberUpdate(lobbyId, userId));
    }

    @Override
    public void onMemberDisconnect(long lobbyId, long userId) {
        this.listeners.forEach(l -> l.onMemberDisconnect(lobbyId, userId));
    }

    @Override
    public void onLobbyMessage(long lobbyId, long userId, byte[] data) {
        this.listeners.forEach(l -> l.onLobbyMessage(lobbyId, userId, data));
    }

    @Override
    public void onSpeaking(long lobbyId, long userId, boolean speaking) {
        this.listeners.forEach(l -> this.onSpeaking(lobbyId, userId, speaking));
    }

    @Override
    public void onNetworkMessage(long lobbyId, long userId, byte channelId, byte[] data) {
        this.listeners.forEach(l -> this.onNetworkMessage(lobbyId, userId, channelId, data));
    }

    @Override
    public void onMessage(long peerId, byte channelId, byte[] data) {
        this.listeners.forEach(l -> l.onMessage(peerId, channelId, data));
    }

    @Override
    public void onRouteUpdate(String routeData) {
        this.listeners.forEach(l -> l.onRouteUpdate(routeData));
    }
}

