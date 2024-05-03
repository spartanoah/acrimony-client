/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.ConfigurationAware;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.lookup.AbstractConfigurationAwareLookup;
import org.apache.logging.log4j.core.lookup.LookupResult;
import org.apache.logging.log4j.core.lookup.PropertiesLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.status.StatusLogger;

public class Interpolator
extends AbstractConfigurationAwareLookup {
    public static final char PREFIX_SEPARATOR = ':';
    private static final String LOOKUP_KEY_WEB = "web";
    private static final String LOOKUP_KEY_DOCKER = "docker";
    private static final String LOOKUP_KEY_KUBERNETES = "kubernetes";
    private static final String LOOKUP_KEY_SPRING = "spring";
    private static final String LOOKUP_KEY_JNDI = "jndi";
    private static final String LOOKUP_KEY_JVMRUNARGS = "jvmrunargs";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Map<String, StrLookup> strLookupMap = new HashMap<String, StrLookup>();
    private final StrLookup defaultLookup;

    public Interpolator(StrLookup defaultLookup) {
        this(defaultLookup, null);
    }

    public Interpolator(StrLookup defaultLookup, List<String> pluginPackages) {
        this.defaultLookup = defaultLookup == null ? new PropertiesLookup(new HashMap<String, String>()) : defaultLookup;
        PluginManager manager = new PluginManager("Lookup");
        manager.collectPlugins(pluginPackages);
        Map<String, PluginType<?>> plugins = manager.getPlugins();
        for (Map.Entry<String, PluginType<?>> entry : plugins.entrySet()) {
            try {
                Class<StrLookup> clazz = entry.getValue().getPluginClass().asSubclass(StrLookup.class);
                if (clazz.getName().equals("org.apache.logging.log4j.core.lookup.JndiLookup") && !JndiManager.isJndiLookupEnabled()) continue;
                this.strLookupMap.put(entry.getKey().toLowerCase(), ReflectionUtil.instantiate(clazz));
            } catch (Throwable t) {
                this.handleError(entry.getKey(), t);
            }
        }
    }

    public Interpolator() {
        this((Map<String, String>)null);
    }

    public Interpolator(Map<String, String> properties) {
        this(new PropertiesLookup(properties), Collections.emptyList());
    }

    public StrLookup getDefaultLookup() {
        return this.defaultLookup;
    }

    public Map<String, StrLookup> getStrLookupMap() {
        return this.strLookupMap;
    }

    private void handleError(String lookupKey, Throwable t) {
        switch (lookupKey) {
            case "jndi": {
                LOGGER.warn("JNDI lookup class is not available because this JRE does not support JNDI. JNDI string lookups will not be available, continuing configuration. Ignoring " + t);
                break;
            }
            case "jvmrunargs": {
                LOGGER.warn("JMX runtime input lookup class is not available because this JRE does not support JMX. JMX lookups will not be available, continuing configuration. Ignoring " + t);
                break;
            }
            case "web": {
                LOGGER.info("Log4j appears to be running in a Servlet environment, but there's no log4j-web module available. If you want better web container support, please add the log4j-web JAR to your web archive or server lib directory.");
                break;
            }
            case "docker": 
            case "spring": {
                break;
            }
            case "kubernetes": {
                if (!(t instanceof NoClassDefFoundError)) break;
                LOGGER.warn("Unable to create Kubernetes lookup due to missing dependency: {}", (Object)t.getMessage());
                break;
            }
            default: {
                LOGGER.error("Unable to create Lookup for {}", (Object)lookupKey, (Object)t);
            }
        }
    }

    @Override
    public String lookup(LogEvent event, String var2) {
        LookupResult result = this.evaluate(event, var2);
        return result == null ? null : result.value();
    }

    @Override
    public LookupResult evaluate(LogEvent event, String var2) {
        if (var2 == null) {
            return null;
        }
        int prefixPos = var2.indexOf(58);
        if (prefixPos >= 0) {
            String prefix = var2.substring(0, prefixPos).toLowerCase(Locale.US);
            String name = var2.substring(prefixPos + 1);
            StrLookup lookup = this.strLookupMap.get(prefix);
            if (lookup instanceof ConfigurationAware) {
                ((ConfigurationAware)((Object)lookup)).setConfiguration(this.configuration);
            }
            LookupResult value = null;
            if (lookup != null) {
                LookupResult lookupResult = value = event == null ? lookup.evaluate(name) : lookup.evaluate(event, name);
            }
            if (value != null) {
                return value;
            }
            var2 = var2.substring(prefixPos + 1);
        }
        if (this.defaultLookup != null) {
            return event == null ? this.defaultLookup.evaluate(var2) : this.defaultLookup.evaluate(event, var2);
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : this.strLookupMap.keySet()) {
            if (sb.length() == 0) {
                sb.append('{');
            } else {
                sb.append(", ");
            }
            sb.append(name);
        }
        if (sb.length() > 0) {
            sb.append('}');
        }
        return sb.toString();
    }
}

