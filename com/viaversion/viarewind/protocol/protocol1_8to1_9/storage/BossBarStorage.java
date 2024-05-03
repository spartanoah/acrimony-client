/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.bossbar.WitherBossBar;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.PlayerPosition;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarStorage
extends StoredObject {
    private final Map<UUID, WitherBossBar> bossBars = new HashMap<UUID, WitherBossBar>();

    public BossBarStorage(UserConnection user) {
        super(user);
    }

    public void add(UUID uuid, String title, float health) {
        WitherBossBar bossBar = new WitherBossBar(this.getUser(), uuid, title, health);
        PlayerPosition playerPosition = this.getUser().get(PlayerPosition.class);
        bossBar.setPlayerLocation(playerPosition.getPosX(), playerPosition.getPosY(), playerPosition.getPosZ(), playerPosition.getYaw(), playerPosition.getPitch());
        bossBar.show();
        this.bossBars.put(uuid, bossBar);
    }

    public void remove(UUID uuid) {
        WitherBossBar bossBar = this.bossBars.remove(uuid);
        if (bossBar == null) {
            return;
        }
        bossBar.hide();
    }

    public void updateLocation() {
        PlayerPosition playerPosition = this.getUser().get(PlayerPosition.class);
        this.bossBars.values().forEach(bossBar -> bossBar.setPlayerLocation(playerPosition.getPosX(), playerPosition.getPosY(), playerPosition.getPosZ(), playerPosition.getYaw(), playerPosition.getPitch()));
    }

    public void changeWorld() {
        this.bossBars.values().forEach(bossBar -> {
            bossBar.hide();
            bossBar.show();
        });
    }

    public void updateHealth(UUID uuid, float health) {
        WitherBossBar bossBar = this.bossBars.get(uuid);
        if (bossBar == null) {
            return;
        }
        bossBar.setHealth(health);
    }

    public void updateTitle(UUID uuid, String title) {
        WitherBossBar bossBar = this.bossBars.get(uuid);
        if (bossBar == null) {
            return;
        }
        bossBar.setTitle(title);
    }
}

