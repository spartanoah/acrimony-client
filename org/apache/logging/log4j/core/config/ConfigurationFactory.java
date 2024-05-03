/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.config;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.OrderComparator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.lookup.ConfigurationStrSubstitutor;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.core.util.BasicAuthorizationProvider;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public abstract class ConfigurationFactory
extends ConfigurationBuilderFactory {
    public static final String CONFIGURATION_FACTORY_PROPERTY = "log4j.configurationFactory";
    public static final String CONFIGURATION_FILE_PROPERTY = "log4j.configurationFile";
    public static final String LOG4J1_CONFIGURATION_FILE_PROPERTY = "log4j.configuration";
    public static final String LOG4J1_EXPERIMENTAL = "log4j1.compatibility";
    public static final String AUTHORIZATION_PROVIDER = "authorizationProvider";
    public static final String CATEGORY = "ConfigurationFactory";
    protected static final Logger LOGGER = StatusLogger.getLogger();
    protected static final String TEST_PREFIX = "log4j2-test";
    protected static final String DEFAULT_PREFIX = "log4j2";
    protected static final String LOG4J1_VERSION = "1";
    protected static final String LOG4J2_VERSION = "2";
    private static final String CLASS_LOADER_SCHEME = "classloader";
    private static final String CLASS_PATH_SCHEME = "classpath";
    private static final String OVERRIDE_PARAM = "override";
    private static volatile List<ConfigurationFactory> factories;
    private static ConfigurationFactory configFactory;
    protected final StrSubstitutor substitutor = new ConfigurationStrSubstitutor(new Interpolator());
    private static final Lock LOCK;
    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static final String[] PREFIXES;
    private static volatile AuthorizationProvider authorizationProvider;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ConfigurationFactory getInstance() {
        if (factories == null) {
            LOCK.lock();
            try {
                if (factories == null) {
                    ArrayList<ConfigurationFactory> list = new ArrayList<ConfigurationFactory>();
                    PropertiesUtil props = PropertiesUtil.getProperties();
                    String factoryClass = props.getStringProperty(CONFIGURATION_FACTORY_PROPERTY);
                    if (factoryClass != null) {
                        ConfigurationFactory.addFactory(list, factoryClass);
                    }
                    PluginManager manager = new PluginManager(CATEGORY);
                    manager.collectPlugins();
                    Map<String, PluginType<?>> plugins = manager.getPlugins();
                    ArrayList<Class<ConfigurationFactory>> ordered = new ArrayList<Class<ConfigurationFactory>>(plugins.size());
                    for (PluginType<?> pluginType : plugins.values()) {
                        try {
                            ordered.add(pluginType.getPluginClass().asSubclass(ConfigurationFactory.class));
                        } catch (Exception ex) {
                            LOGGER.warn("Unable to add class {}", (Object)pluginType.getPluginClass(), (Object)ex);
                        }
                    }
                    Collections.sort(ordered, OrderComparator.getInstance());
                    for (Class clazz : ordered) {
                        ConfigurationFactory.addFactory(list, clazz);
                    }
                    factories = Collections.unmodifiableList(list);
                    authorizationProvider = ConfigurationFactory.authorizationProvider(props);
                }
            } finally {
                LOCK.unlock();
            }
        }
        LOGGER.debug("Using configurationFactory {}", (Object)configFactory);
        return configFactory;
    }

    public static AuthorizationProvider authorizationProvider(PropertiesUtil props) {
        String authClass = props.getStringProperty(PREFIXES, AUTHORIZATION_PROVIDER, null);
        AuthorizationProvider provider = null;
        if (authClass != null) {
            try {
                Object obj = LoaderUtil.newInstanceOf(authClass);
                if (obj instanceof AuthorizationProvider) {
                    provider = (AuthorizationProvider)obj;
                } else {
                    LOGGER.warn("{} is not an AuthorizationProvider, using default", (Object)obj.getClass().getName());
                }
            } catch (Exception ex) {
                LOGGER.warn("Unable to create {}, using default: {}", (Object)authClass, (Object)ex.getMessage());
            }
        }
        if (provider == null) {
            provider = new BasicAuthorizationProvider(props);
        }
        return provider;
    }

    public static AuthorizationProvider getAuthorizationProvider() {
        return authorizationProvider;
    }

    private static void addFactory(Collection<ConfigurationFactory> list, String factoryClass) {
        try {
            ConfigurationFactory.addFactory(list, Loader.loadClass(factoryClass).asSubclass(ConfigurationFactory.class));
        } catch (Exception ex) {
            LOGGER.error("Unable to load class {}", (Object)factoryClass, (Object)ex);
        }
    }

    private static void addFactory(Collection<ConfigurationFactory> list, Class<? extends ConfigurationFactory> factoryClass) {
        try {
            list.add(ReflectionUtil.instantiate(factoryClass));
        } catch (Exception ex) {
            LOGGER.error("Unable to create instance of {}", (Object)factoryClass.getName(), (Object)ex);
        }
    }

    public static void setConfigurationFactory(ConfigurationFactory factory) {
        configFactory = factory;
    }

    public static void resetConfigurationFactory() {
        configFactory = new Factory();
    }

    public static void removeConfigurationFactory(ConfigurationFactory factory) {
        if (configFactory == factory) {
            configFactory = new Factory();
        }
    }

    protected abstract String[] getSupportedTypes();

    protected String getTestPrefix() {
        return TEST_PREFIX;
    }

    protected String getDefaultPrefix() {
        return DEFAULT_PREFIX;
    }

    protected String getVersion() {
        return LOG4J2_VERSION;
    }

    protected boolean isActive() {
        return true;
    }

    public abstract Configuration getConfiguration(LoggerContext var1, ConfigurationSource var2);

    public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
        ConfigurationSource source;
        if (!this.isActive()) {
            return null;
        }
        if (configLocation != null && (source = ConfigurationSource.fromUri(configLocation)) != null) {
            return this.getConfiguration(loggerContext, source);
        }
        return null;
    }

    public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation, ClassLoader loader) {
        Configuration configuration;
        String path;
        ConfigurationSource source;
        if (!this.isActive()) {
            return null;
        }
        if (loader == null) {
            return this.getConfiguration(loggerContext, name, configLocation);
        }
        if (ConfigurationFactory.isClassLoaderUri(configLocation) && (source = ConfigurationSource.fromResource(path = ConfigurationFactory.extractClassLoaderUriPath(configLocation), loader)) != null && (configuration = this.getConfiguration(loggerContext, source)) != null) {
            return configuration;
        }
        return this.getConfiguration(loggerContext, name, configLocation);
    }

    static boolean isClassLoaderUri(URI uri) {
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        return scheme == null || scheme.equals(CLASS_LOADER_SCHEME) || scheme.equals(CLASS_PATH_SCHEME);
    }

    static String extractClassLoaderUriPath(URI uri) {
        return uri.getScheme() == null ? uri.getPath() : uri.getSchemeSpecificPart();
    }

    @Deprecated
    protected ConfigurationSource getInputFromString(String config, ClassLoader loader) {
        return ConfigurationSource.fromUri(NetUtils.toURI(config));
    }

    static List<ConfigurationFactory> getFactories() {
        return factories;
    }

    static {
        configFactory = new Factory();
        LOCK = new ReentrantLock();
        PREFIXES = new String[]{"log4j2.", "log4j2.Configuration."};
    }

    private static class Factory
    extends ConfigurationFactory {
        private static final String ALL_TYPES = "*";

        private Factory() {
        }

        @Override
        public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
            Configuration config;
            if (configLocation == null) {
                String configLocationStr = this.substitutor.replace(PropertiesUtil.getProperties().getStringProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY));
                if (configLocationStr != null) {
                    String[] sources = this.parseConfigLocations(configLocationStr);
                    if (sources.length > 1) {
                        ArrayList<AbstractConfiguration> configs = new ArrayList<AbstractConfiguration>();
                        for (String sourceLocation : sources) {
                            Configuration config2 = this.getConfiguration(loggerContext, sourceLocation.trim());
                            if (config2 != null) {
                                if (config2 instanceof AbstractConfiguration) {
                                    configs.add((AbstractConfiguration)config2);
                                    continue;
                                }
                                LOGGER.error("Failed to created configuration at {}", (Object)sourceLocation);
                                return null;
                            }
                            LOGGER.warn("Unable to create configuration for {}, ignoring", (Object)sourceLocation);
                        }
                        if (configs.size() > 1) {
                            return new CompositeConfiguration(configs);
                        }
                        if (configs.size() == 1) {
                            return (Configuration)configs.get(0);
                        }
                    }
                    return this.getConfiguration(loggerContext, configLocationStr);
                }
                String log4j1ConfigStr = this.substitutor.replace(PropertiesUtil.getProperties().getStringProperty(ConfigurationFactory.LOG4J1_CONFIGURATION_FILE_PROPERTY));
                if (log4j1ConfigStr != null) {
                    System.setProperty(ConfigurationFactory.LOG4J1_EXPERIMENTAL, "true");
                    return this.getConfiguration(ConfigurationFactory.LOG4J1_VERSION, loggerContext, log4j1ConfigStr);
                }
                for (ConfigurationFactory factory : Factory.getFactories()) {
                    String[] types = factory.getSupportedTypes();
                    if (types == null) continue;
                    for (String type : types) {
                        Configuration config3;
                        if (!type.equals(ALL_TYPES) || (config3 = factory.getConfiguration(loggerContext, name, configLocation)) == null) continue;
                        return config3;
                    }
                }
            } else {
                String[] sources = this.parseConfigLocations(configLocation);
                if (sources.length > 1) {
                    ArrayList<AbstractConfiguration> configs = new ArrayList<AbstractConfiguration>();
                    for (String sourceLocation : sources) {
                        Configuration config4 = this.getConfiguration(loggerContext, sourceLocation.trim());
                        if (!(config4 instanceof AbstractConfiguration)) {
                            LOGGER.error("Failed to created configuration at {}", (Object)sourceLocation);
                            return null;
                        }
                        configs.add((AbstractConfiguration)config4);
                    }
                    return new CompositeConfiguration(configs);
                }
                String configLocationStr = configLocation.toString();
                for (ConfigurationFactory factory : Factory.getFactories()) {
                    String[] types = factory.getSupportedTypes();
                    if (types == null) continue;
                    for (String type : types) {
                        Configuration config5;
                        if (!type.equals(ALL_TYPES) && !configLocationStr.endsWith(type) || (config5 = factory.getConfiguration(loggerContext, name, configLocation)) == null) continue;
                        return config5;
                    }
                }
            }
            if ((config = this.getConfiguration(loggerContext, true, name)) == null && (config = this.getConfiguration(loggerContext, true, null)) == null && (config = this.getConfiguration(loggerContext, false, name)) == null) {
                config = this.getConfiguration(loggerContext, false, null);
            }
            if (config != null) {
                return config;
            }
            LOGGER.warn("No Log4j 2 configuration file found. Using default configuration (logging only errors to the console), or user programmatically provided configurations. Set system property 'log4j2.debug' to show Log4j 2 internal initialization logging. See https://logging.apache.org/log4j/2.x/manual/configuration.html for instructions on how to configure Log4j 2");
            return new DefaultConfiguration();
        }

        private Configuration getConfiguration(LoggerContext loggerContext, String configLocationStr) {
            return this.getConfiguration(null, loggerContext, configLocationStr);
        }

        private Configuration getConfiguration(String requiredVersion, LoggerContext loggerContext, String configLocationStr) {
            ConfigurationSource source = null;
            try {
                source = ConfigurationSource.fromUri(NetUtils.toURI(configLocationStr));
            } catch (Exception ex) {
                LOGGER.catching(Level.DEBUG, ex);
            }
            if (source == null) {
                ClassLoader loader = LoaderUtil.getThreadContextClassLoader();
                source = this.getInputFromString(configLocationStr, loader);
            }
            if (source != null) {
                for (ConfigurationFactory factory : Factory.getFactories()) {
                    String[] types;
                    if (requiredVersion != null && !factory.getVersion().equals(requiredVersion) || (types = factory.getSupportedTypes()) == null) continue;
                    for (String type : types) {
                        Configuration config;
                        if (!type.equals(ALL_TYPES) && !configLocationStr.endsWith(type) || (config = factory.getConfiguration(loggerContext, source)) == null) continue;
                        return config;
                    }
                }
            }
            return null;
        }

        private Configuration getConfiguration(LoggerContext loggerContext, boolean isTest, String name) {
            boolean named = Strings.isNotEmpty(name);
            ClassLoader loader = LoaderUtil.getThreadContextClassLoader();
            for (ConfigurationFactory factory : Factory.getFactories()) {
                String prefix = isTest ? factory.getTestPrefix() : factory.getDefaultPrefix();
                String[] types = factory.getSupportedTypes();
                if (types == null) continue;
                for (String suffix : types) {
                    String configName;
                    ConfigurationSource source;
                    if (suffix.equals(ALL_TYPES) || (source = ConfigurationSource.fromResource(configName = named ? prefix + name + suffix : prefix + suffix, loader)) == null) continue;
                    if (!factory.isActive()) {
                        LOGGER.warn("Found configuration file {} for inactive ConfigurationFactory {}", (Object)configName, (Object)factory.getClass().getName());
                    }
                    return factory.getConfiguration(loggerContext, source);
                }
            }
            return null;
        }

        @Override
        public String[] getSupportedTypes() {
            return null;
        }

        @Override
        public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
            if (source != null) {
                String config = source.getLocation();
                for (ConfigurationFactory factory : Factory.getFactories()) {
                    String[] types = factory.getSupportedTypes();
                    if (types == null) continue;
                    for (String type : types) {
                        if (!type.equals(ALL_TYPES) && (config == null || !config.endsWith(type))) continue;
                        Configuration c = factory.getConfiguration(loggerContext, source);
                        if (c != null) {
                            LOGGER.debug("Loaded configuration from {}", (Object)source);
                            return c;
                        }
                        LOGGER.error("Cannot determine the ConfigurationFactory to use for {}", (Object)config);
                        return null;
                    }
                }
            }
            LOGGER.error("Cannot process configuration, input source is null");
            return null;
        }

        private String[] parseConfigLocations(URI configLocations) {
            String[] uris = configLocations.toString().split("\\?");
            ArrayList<String> locations = new ArrayList<String>();
            if (uris.length > 1) {
                String[] pairs;
                locations.add(uris[0]);
                for (String pair : pairs = configLocations.getQuery().split("&")) {
                    int idx = pair.indexOf("=");
                    try {
                        String key;
                        String string = key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                        if (!key.equalsIgnoreCase(ConfigurationFactory.OVERRIDE_PARAM)) continue;
                        locations.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        LOGGER.warn("Invalid query parameter in {}", (Object)configLocations);
                    }
                }
                return locations.toArray(Strings.EMPTY_ARRAY);
            }
            return new String[]{uris[0]};
        }

        private String[] parseConfigLocations(String configLocations) {
            String[] uris = configLocations.split(",");
            if (uris.length > 1) {
                return uris;
            }
            try {
                return this.parseConfigLocations(new URI(configLocations));
            } catch (URISyntaxException ex) {
                LOGGER.warn("Error parsing URI {}", (Object)configLocations);
                return new String[]{configLocations};
            }
        }
    }
}

