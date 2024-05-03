/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ConnectionManager {
    public boolean isClientConnected(UUID var1);

    default public boolean isFrontEnd(UserConnection connection) {
        return !connection.isClientSide();
    }

    public @Nullable UserConnection getConnectedClient(UUID var1);

    public @Nullable UUID getConnectedClientId(UserConnection var1);

    public Set<UserConnection> getConnections();

    public Map<UUID, UserConnection> getConnectedClients();

    public void onLoginSuccess(UserConnection var1);

    public void onDisconnect(UserConnection var1);
}

