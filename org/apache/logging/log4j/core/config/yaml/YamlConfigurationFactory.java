/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config.yaml;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.apache.logging.log4j.core.util.Loader;

@Plugin(name="YamlConfigurationFactory", category="ConfigurationFactory")
@Order(value=7)
public class YamlConfigurationFactory
extends ConfigurationFactory {
    private static final String[] SUFFIXES = new String[]{".yml", ".yaml"};
    private static final String[] dependencies = new String[]{"com.fasterxml.jackson.databind.ObjectMapper", "com.fasterxml.jackson.databind.JsonNode", "com.fasterxml.jackson.core.JsonParser", "com.fasterxml.jackson.dataformat.yaml.YAMLFactory"};
    private final boolean isActive;

    public YamlConfigurationFactory() {
        for (String dependency : dependencies) {
            if (Loader.isClassAvailable(dependency)) continue;
            LOGGER.debug("Missing dependencies for Yaml support, ConfigurationFactory {} is inactive", (Object)this.getClass().getName());
            this.isActive = false;
            return;
        }
        this.isActive = true;
    }

    @Override
    protected boolean isActive() {
        return this.isActive;
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        if (!this.isActive) {
            return null;
        }
        return new YamlConfiguration(loggerContext, source);
    }

    @Override
    public String[] getSupportedTypes() {
        return SUFFIXES;
    }
}

