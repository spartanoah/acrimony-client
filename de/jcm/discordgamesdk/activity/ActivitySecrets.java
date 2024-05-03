/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.activity;

public class ActivitySecrets {
    private final long pointer;

    ActivitySecrets(long pointer) {
        this.pointer = pointer;
    }

    public void setMatchSecret(String secret) {
        if (secret.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 128");
        }
        this.setMatchSecret(this.pointer, secret);
    }

    public String getMatchSecret() {
        return this.getMatchSecret(this.pointer);
    }

    public void setJoinSecret(String secret) {
        if (secret.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 128");
        }
        this.setJoinSecret(this.pointer, secret);
    }

    public String getJoinSecret() {
        return this.getJoinSecret(this.pointer);
    }

    public void setSpectateSecret(String secret) {
        if (secret.getBytes().length >= 128) {
            throw new IllegalArgumentException("max length is 128");
        }
        this.setSpectateSecret(this.pointer, secret);
    }

    public String getSpectateSecret() {
        return this.getSpectateSecret(this.pointer);
    }

    private native void setMatchSecret(long var1, String var3);

    private native String getMatchSecret(long var1);

    private native void setJoinSecret(long var1, String var3);

    private native String getJoinSecret(long var1);

    private native void setSpectateSecret(long var1, String var3);

    private native String getSpectateSecret(long var1);
}

