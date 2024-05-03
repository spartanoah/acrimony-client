/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

public class PropertiesPropertySource
implements PropertySource {
    private static final int DEFAULT_PRIORITY = 200;
    private static final String PREFIX = "log4j2.";
    private final Properties properties;
    private final int priority;

    public PropertiesPropertySource(Properties properties) {
        this(properties, 200);
    }

    public PropertiesPropertySource(Properties properties, int priority) {
        this.properties = properties;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        for (Map.Entry entry : this.properties.entrySet()) {
            action.accept((String)entry.getKey(), (String)entry.getValue());
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return PREFIX + PropertySource.Util.joinAsCamelCase(tokens);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return this.properties.stringPropertyNames();
    }

    @Override
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    @Override
    public boolean containsProperty(String key) {
        return this.getProperty(key) != null;
    }
}

