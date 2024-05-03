/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.lookup.LookupResult;
import org.apache.logging.log4j.core.lookup.StrLookup;

public final class PropertiesLookup
implements StrLookup {
    private final Map<String, String> contextProperties;
    private final Map<String, ConfigurationPropertyResult> configurationProperties;

    public PropertiesLookup(Property[] configProperties, Map<String, String> contextProperties) {
        this.contextProperties = contextProperties == null ? Collections.emptyMap() : contextProperties;
        this.configurationProperties = configProperties == null ? Collections.emptyMap() : PropertiesLookup.createConfigurationPropertyMap(configProperties);
    }

    public PropertiesLookup(Map<String, String> properties) {
        this(Property.EMPTY_ARRAY, properties);
    }

    @Override
    public String lookup(LogEvent event, String key) {
        return this.lookup(key);
    }

    @Override
    public String lookup(String key) {
        LookupResult result = this.evaluate(key);
        return result == null ? null : result.value();
    }

    @Override
    public LookupResult evaluate(String key) {
        if (key == null) {
            return null;
        }
        LookupResult configResult = this.configurationProperties.get(key);
        if (configResult != null) {
            return configResult;
        }
        String contextResult = this.contextProperties.get(key);
        return contextResult == null ? null : new ContextPropertyResult(contextResult);
    }

    @Override
    public LookupResult evaluate(LogEvent event, String key) {
        return this.evaluate(key);
    }

    public String toString() {
        return "PropertiesLookup{contextProperties=" + this.contextProperties + ", configurationProperties=" + this.configurationProperties + '}';
    }

    private static Map<String, ConfigurationPropertyResult> createConfigurationPropertyMap(Property[] props) {
        HashMap<String, ConfigurationPropertyResult> result = new HashMap<String, ConfigurationPropertyResult>(props.length);
        for (Property property : props) {
            result.put(property.getName(), new ConfigurationPropertyResult(property.getRawValue()));
        }
        return result;
    }

    private static final class ContextPropertyResult
    implements LookupResult {
        private final String value;

        ContextPropertyResult(String value) {
            this.value = Objects.requireNonNull(value, "value is required");
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public boolean isLookupEvaluationAllowedInValue() {
            return false;
        }

        public String toString() {
            return "ContextPropertyResult{'" + this.value + "'}";
        }
    }

    private static final class ConfigurationPropertyResult
    implements LookupResult {
        private final String value;

        ConfigurationPropertyResult(String value) {
            this.value = Objects.requireNonNull(value, "value is required");
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public boolean isLookupEvaluationAllowedInValue() {
            return true;
        }

        public String toString() {
            return "ConfigurationPropertyResult{'" + this.value + "'}";
        }
    }
}

