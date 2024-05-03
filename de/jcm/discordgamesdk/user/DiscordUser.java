/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package de.jcm.discordgamesdk.user;

public class DiscordUser {
    private final long userId;
    private final String username;
    private final String discriminator;
    private final String avatar;
    private final boolean bot;

    public DiscordUser(long userId, String username, String discriminator, String avatar, boolean bot) {
        this.userId = userId;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.bot = bot;
    }

    public long getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDiscriminator() {
        return this.discriminator;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public boolean isBot() {
        return this.bot;
    }

    public String toString() {
        return "DiscordUser{userId=" + this.userId + ", username='" + this.username + '\'' + ", discriminator='" + this.discriminator + '\'' + ", avatar='" + this.avatar + '\'' + ", bot=" + this.bot + '}';
    }
}

