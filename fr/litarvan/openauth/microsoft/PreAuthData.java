/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth.microsoft;

public class PreAuthData {
    private final String ppft;
    private final String urlPost;

    public PreAuthData(String ppft, String urlPost) {
        this.ppft = ppft;
        this.urlPost = urlPost;
    }

    public String getPPFT() {
        return this.ppft;
    }

    public String getUrlPost() {
        return this.urlPost;
    }
}

