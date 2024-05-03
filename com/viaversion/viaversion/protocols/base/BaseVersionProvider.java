/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.protocols.base;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;

public class BaseVersionProvider
implements VersionProvider {
    @Override
    public int getClosestServerProtocol(UserConnection connection) throws Exception {
        return Via.getAPI().getServerVersion().lowestSupportedVersion();
    }
}

