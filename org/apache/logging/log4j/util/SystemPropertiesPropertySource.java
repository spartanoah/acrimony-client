/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.util.Collection;
import java.util.Objects;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

public class SystemPropertiesPropertySource
implements PropertySource {
    private static final int DEFAULT_PRIORITY = 0;
    private static final String PREFIX = "log4j2.";

    @Override
    public int getPriority() {
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void forEach(BiConsumer<String, String> action) {
        Object[] properties;
        try {
            properties = System.getProperties();
        } catch (SecurityException e) {
            return;
        }
        Object[] objectArray = properties;
        synchronized (properties) {
            Object[] keySet = properties.keySet().toArray();
            // ** MonitorExit[var4_5] (shouldn't be in output)
            for (Object key : keySet) {
                String keyStr = Objects.toString(key, null);
                action.accept(keyStr, properties.getProperty(keyStr));
            }
            return;
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return PREFIX + PropertySource.Util.joinAsCamelCase(tokens);
    }

    @Override
    public Collection<String> getPropertyNames() {
        try {
            return System.getProperties().stringPropertyNames();
        } catch (SecurityException e) {
            return PropertySource.super.getPropertyNames();
        }
    }

    @Override
    public String getProperty(String key) {
        try {
            return System.getProperty(key);
        } catch (SecurityException e) {
            return PropertySource.super.getProperty(key);
        }
    }

    @Override
    public boolean containsProperty(String key) {
        return this.getProperty(key) != null;
    }
}

