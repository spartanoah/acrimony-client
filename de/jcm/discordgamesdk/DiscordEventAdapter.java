/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.Relationship;

public abstract class DiscordEventAdapter {
    public void onActivityJoin(String secret) {
    }

    public void onActivitySpectate(String secret) {
    }

    public void onActivityJoinRequest(DiscordUser user) {
    }

    public void onCurrentUserUpdate() {
    }

    public void onOverlayToggle(boolean locked) {
    }

    public void onRelationshipRefresh() {
    }

    public void onRelationshipUpdate(Relationship relationship) {
    }

    public void onLobbyUpdate(long lobbyId) {
    }

    public void onLobbyDelete(long lobbyId, int reason) {
    }

    public void onMemberConnect(long lobbyId, long userId) {
    }

    public void onMemberUpdate(long lobbyId, long userId) {
    }

    public void onMemberDisconnect(long lobbyId, long userId) {
    }

    public void onLobbyMessage(long lobbyId, long userId, byte[] data) {
    }

    public void onSpeaking(long lobbyId, long userId, boolean speaking) {
    }

    public void onNetworkMessage(long lobbyId, long userId, byte channelId, byte[] data) {
    }

    public void onMessage(long peerId, byte channelId, byte[] data) {
    }

    public void onRouteUpdate(String routeData) {
    }
}

