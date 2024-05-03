/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.LookupResult;
import org.apache.logging.log4j.core.lookup.PropertiesLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

@Plugin(name="properties", category="Core", printObject=true)
public final class PropertiesPlugin {
    private static final StrSubstitutor UNESCAPING_SUBSTITUTOR = PropertiesPlugin.createUnescapingSubstitutor();

    private PropertiesPlugin() {
    }

    @PluginFactory
    public static StrLookup configureSubstitutor(@PluginElement(value="Properties") Property[] properties, @PluginConfiguration Configuration config) {
        Property[] unescapedProperties = new Property[properties == null ? 0 : properties.length];
        for (int i = 0; i < unescapedProperties.length; ++i) {
            unescapedProperties[i] = PropertiesPlugin.unescape(properties[i]);
        }
        return new Interpolator(new PropertiesLookup(unescapedProperties, config.getProperties()), config.getPluginPackages());
    }

    private static Property unescape(Property input) {
        return Property.createProperty(input.getName(), PropertiesPlugin.unescape(input.getRawValue()), input.getValue());
    }

    static String unescape(String input) {
        return UNESCAPING_SUBSTITUTOR.replace(input);
    }

    private static StrSubstitutor createUnescapingSubstitutor() {
        StrSubstitutor substitutor = new StrSubstitutor(NullLookup.INSTANCE);
        substitutor.setValueDelimiter(null);
        substitutor.setValueDelimiterMatcher(null);
        return substitutor;
    }

    private static enum NullLookup implements StrLookup
    {
        INSTANCE;


        @Override
        public String lookup(String key) {
            return null;
        }

        @Override
        public String lookup(LogEvent event, String key) {
            return null;
        }

        @Override
        public LookupResult evaluate(String key) {
            return null;
        }

        @Override
        public LookupResult evaluate(LogEvent event, String key) {
            return null;
        }
    }
}

