/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.configuration;

import com.viaversion.viaversion.api.configuration.Config;
import java.util.Collection;

public interface ConfigurationProvider {
    public void register(Config var1);

    public Collection<Config> configs();

    public void reloadConfigs();
}

