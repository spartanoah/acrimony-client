/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

public final class PackageVersion
implements Versioned {
    public static final Version VERSION = VersionUtil.parseVersion("2.11.2", "com.fasterxml.jackson.core", "jackson-core");

    @Override
    public Version version() {
        return VERSION;
    }
}

