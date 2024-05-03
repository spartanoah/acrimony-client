/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth.model;

public class AuthProfile {
    private String name;
    private String id;

    public AuthProfile() {
        this.name = "";
        this.id = "";
    }

    public AuthProfile(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }
}

