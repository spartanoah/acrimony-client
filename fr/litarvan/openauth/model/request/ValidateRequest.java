/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth.model.request;

public class ValidateRequest {
    private String accessToken;

    public ValidateRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}

