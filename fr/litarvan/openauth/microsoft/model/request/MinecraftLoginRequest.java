/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth.microsoft.model.request;

public class MinecraftLoginRequest {
    private final String identityToken;

    public MinecraftLoginRequest(String identityToken) {
        this.identityToken = identityToken;
    }

    public String getIdentityToken() {
        return this.identityToken;
    }
}

