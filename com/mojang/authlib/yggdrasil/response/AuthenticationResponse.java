/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.authlib.yggdrasil.response;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.response.Response;
import com.mojang.authlib.yggdrasil.response.User;

public class AuthenticationResponse
extends Response {
    private String accessToken;
    private String clientToken;
    private GameProfile selectedProfile;
    private GameProfile[] availableProfiles;
    private User user;

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getClientToken() {
        return this.clientToken;
    }

    public GameProfile[] getAvailableProfiles() {
        return this.availableProfiles;
    }

    public GameProfile getSelectedProfile() {
        return this.selectedProfile;
    }

    public User getUser() {
        return this.user;
    }
}

