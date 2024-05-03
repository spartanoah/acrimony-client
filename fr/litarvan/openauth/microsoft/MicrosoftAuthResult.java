/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth.microsoft;

import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;

public class MicrosoftAuthResult {
    private final MinecraftProfile profile;
    private final String accessToken;
    private final String refreshToken;
    private final String xuid;
    private final String clientId;

    public MicrosoftAuthResult(MinecraftProfile profile, String accessToken, String refreshToken, String xuid, String clientId) {
        this.profile = profile;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.xuid = xuid;
        this.clientId = clientId;
    }

    public MinecraftProfile getProfile() {
        return this.profile;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getXuid() {
        return this.xuid;
    }

    public String getClientId() {
        return this.clientId;
    }
}

