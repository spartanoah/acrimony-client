/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public class CompressionStatusTracker
extends StoredObject {
    public boolean removeCompression = false;

    public CompressionStatusTracker(UserConnection user) {
        super(user);
    }
}

