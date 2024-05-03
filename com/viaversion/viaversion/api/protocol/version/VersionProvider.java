/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.protocol.version;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;

@FunctionalInterface
public interface VersionProvider
extends Provider {
    public int getClosestServerProtocol(UserConnection var1) throws Exception;
}

