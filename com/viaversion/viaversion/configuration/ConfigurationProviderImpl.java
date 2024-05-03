/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.configuration;

import com.viaversion.viaversion.api.configuration.Config;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConfigurationProviderImpl
implements ConfigurationProvider {
    private final List<Config> configs = new ArrayList<Config>();

    @Override
    public void register(Config config) {
        this.configs.add(config);
    }

    @Override
    public Collection<Config> configs() {
        return Collections.unmodifiableCollection(this.configs);
    }

    @Override
    public void reloadConfigs() {
        for (Config config : this.configs) {
            config.reload();
        }
    }
}

