/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.protocol.version;

import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;

public interface ServerProtocolVersion {
    public int lowestSupportedVersion();

    public int highestSupportedVersion();

    public IntSortedSet supportedVersions();

    default public boolean isKnown() {
        return this.lowestSupportedVersion() != -1 && this.highestSupportedVersion() != -1;
    }
}

