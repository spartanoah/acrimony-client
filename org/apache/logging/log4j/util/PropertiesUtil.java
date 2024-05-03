/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.util.LowLevelLogUtil;
import org.apache.logging.log4j.util.PropertiesPropertySource;
import org.apache.logging.log4j.util.PropertyFilePropertySource;
import org.apache.logging.log4j.util.PropertySource;
import org.apache.logging.log4j.util.ServiceLoaderUtil;
import org.apache.logging.log4j.util.Supplier;

public final class PropertiesUtil {
    private static final String LOG4J_PROPERTIES_FILE_NAME = "log4j2.component.properties";
    private static final String LOG4J_SYSTEM_PROPERTIES_FILE_NAME = "log4j2.system.properties";
    private static final PropertiesUtil LOG4J_PROPERTIES = new PropertiesUtil("log4j2.component.properties", false);
    private final Environment environment;

    public PropertiesUtil(Properties props) {
        this(new PropertiesPropertySource(props));
    }

    public PropertiesUtil(String propertiesFileName) {
        this(propertiesFileName, true);
    }

    private PropertiesUtil(String propertiesFileName, boolean useTccl) {
        this(new PropertyFilePropertySource(propertiesFileName, useTccl));
    }

    PropertiesUtil(PropertySource source) {
        this.environment = new Environment(source);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Properties loadClose(InputStream in, Object source) {
        Properties props = new Properties();
        if (null != in) {
            try {
                props.load(in);
            } catch (IOException e) {
                LowLevelLogUtil.logException("Unable to read " + source, e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    LowLevelLogUtil.logException("Unable to close " + source, e);
                }
            }
        }
        return props;
    }

    public static PropertiesUtil getProperties() {
        return LOG4J_PROPERTIES;
    }

    public boolean hasProperty(String name) {
        return this.environment.containsKey(name);
    }

    public boolean getBooleanProperty(String name) {
        return this.getBooleanProperty(name, false);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    public boolean getBooleanProperty(String name, boolean defaultValueIfAbsent, boolean defaultValueIfPresent) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValueIfAbsent : (prop.isEmpty() ? defaultValueIfPresent : "true".equalsIgnoreCase(prop));
    }

    public Boolean getBooleanProperty(String[] prefixes, String key, Supplier<Boolean> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getBooleanProperty(prefix + key);
        }
        return supplier != null ? supplier.get() : null;
    }

    public Charset getCharsetProperty(String name) {
        return this.getCharsetProperty(name, Charset.defaultCharset());
    }

    public Charset getCharsetProperty(String name, Charset defaultValue) {
        String mapped;
        String charsetName = this.getStringProperty(name);
        if (charsetName == null) {
            return defaultValue;
        }
        if (Charset.isSupported(charsetName)) {
            return Charset.forName(charsetName);
        }
        ResourceBundle bundle = PropertiesUtil.getCharsetsResourceBundle();
        if (bundle.containsKey(name) && Charset.isSupported(mapped = bundle.getString(name))) {
            return Charset.forName(mapped);
        }
        LowLevelLogUtil.log("Unable to get Charset '" + charsetName + "' for property '" + name + "', using default " + defaultValue + " and continuing.");
        return defaultValue;
    }

    public double getDoubleProperty(String name, double defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            try {
                return Double.parseDouble(prop);
            } catch (Exception exception) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    public int getIntegerProperty(String name, int defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            try {
                return Integer.parseInt(prop.trim());
            } catch (Exception exception) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    public Integer getIntegerProperty(String[] prefixes, String key, Supplier<Integer> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getIntegerProperty(prefix + key, 0);
        }
        return supplier != null ? supplier.get() : null;
    }

    public long getLongProperty(String name, long defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            try {
                return Long.parseLong(prop);
            } catch (Exception exception) {
                // empty catch block
            }
        }
        return defaultValue;
    }

    public Long getLongProperty(String[] prefixes, String key, Supplier<Long> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getLongProperty(prefix + key, 0L);
        }
        return supplier != null ? supplier.get() : null;
    }

    public Duration getDurationProperty(String name, Duration defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            return TimeUnit.getDuration(prop);
        }
        return defaultValue;
    }

    public Duration getDurationProperty(String[] prefixes, String key, Supplier<Duration> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getDurationProperty(prefix + key, null);
        }
        return supplier != null ? supplier.get() : null;
    }

    public String getStringProperty(String[] prefixes, String key, Supplier<String> supplier) {
        for (String prefix : prefixes) {
            String result = this.getStringProperty(prefix + key);
            if (result == null) continue;
            return result;
        }
        return supplier != null ? supplier.get() : null;
    }

    public String getStringProperty(String name) {
        return this.environment.get(name);
    }

    public String getStringProperty(String name, String defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public static Properties getSystemProperties() {
        try {
            return new Properties(System.getProperties());
        } catch (SecurityException ex) {
            LowLevelLogUtil.logException("Unable to access system properties.", ex);
            return new Properties();
        }
    }

    public void reload() {
        this.environment.reload();
    }

    public static Properties extractSubset(Properties properties, String prefix) {
        Properties subset = new Properties();
        if (prefix == null || prefix.length() == 0) {
            return subset;
        }
        String prefixToMatch = prefix.charAt(prefix.length() - 1) != '.' ? prefix + '.' : prefix;
        ArrayList<String> keys = new ArrayList<String>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(prefixToMatch)) continue;
            subset.setProperty(key.substring(prefixToMatch.length()), properties.getProperty(key));
            keys.add(key);
        }
        for (String key : keys) {
            properties.remove(key);
        }
        return subset;
    }

    static ResourceBundle getCharsetsResourceBundle() {
        return ResourceBundle.getBundle("Log4j-charsets");
    }

    public static Map<String, Properties> partitionOnCommonPrefixes(Properties properties) {
        return PropertiesUtil.partitionOnCommonPrefixes(properties, false);
    }

    public static Map<String, Properties> partitionOnCommonPrefixes(Properties properties, boolean includeBaseKey) {
        ConcurrentHashMap<String, Properties> parts = new ConcurrentHashMap<String, Properties>();
        for (String key : properties.stringPropertyNames()) {
            int idx = key.indexOf(46);
            if (idx < 0) {
                if (!includeBaseKey) continue;
                if (!parts.containsKey(key)) {
                    parts.put(key, new Properties());
                }
                ((Properties)parts.get(key)).setProperty("", properties.getProperty(key));
                continue;
            }
            String prefix = key.substring(0, idx);
            if (!parts.containsKey(prefix)) {
                parts.put(prefix, new Properties());
            }
            ((Properties)parts.get(prefix)).setProperty(key.substring(idx + 1), properties.getProperty(key));
        }
        return parts;
    }

    public boolean isOsWindows() {
        return this.getStringProperty("os.name", "").startsWith("Windows");
    }

    private static enum TimeUnit {
        NANOS("ns,nano,nanos,nanosecond,nanoseconds", ChronoUnit.NANOS),
        MICROS("us,micro,micros,microsecond,microseconds", ChronoUnit.MICROS),
        MILLIS("ms,milli,millis,millsecond,milliseconds", ChronoUnit.MILLIS),
        SECONDS("s,second,seconds", ChronoUnit.SECONDS),
        MINUTES("m,minute,minutes", ChronoUnit.MINUTES),
        HOURS("h,hour,hours", ChronoUnit.HOURS),
        DAYS("d,day,days", ChronoUnit.DAYS);

        private final String[] descriptions;
        private final ChronoUnit timeUnit;

        private TimeUnit(String descriptions, ChronoUnit timeUnit) {
            this.descriptions = descriptions.split(",");
            this.timeUnit = timeUnit;
        }

        ChronoUnit getTimeUnit() {
            return this.timeUnit;
        }

        static Duration getDuration(String time) {
            String value = time.trim();
            ChronoUnit temporalUnit = ChronoUnit.MILLIS;
            long timeVal = 0L;
            for (TimeUnit timeUnit : TimeUnit.values()) {
                for (String suffix : timeUnit.descriptions) {
                    if (!value.endsWith(suffix)) continue;
                    temporalUnit = timeUnit.timeUnit;
                    timeVal = Long.parseLong(value.substring(0, value.length() - suffix.length()));
                }
            }
            return Duration.of(timeVal, temporalUnit);
        }
    }

    private static class Environment {
        private final Set<PropertySource> sources = new TreeSet<PropertySource>(new PropertySource.Comparator());
        private final Map<String, String> literal = new ConcurrentHashMap<String, String>();
        private final Map<String, String> normalized = new ConcurrentHashMap<String, String>();
        private final Map<List<CharSequence>, String> tokenized = new ConcurrentHashMap<List<CharSequence>, String>();

        private Environment(PropertySource propertySource) {
            PropertyFilePropertySource sysProps = new PropertyFilePropertySource(PropertiesUtil.LOG4J_SYSTEM_PROPERTIES_FILE_NAME, false);
            try {
                sysProps.forEach((key, value) -> {
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                });
            } catch (SecurityException securityException) {
                // empty catch block
            }
            this.sources.add(propertySource);
            ServiceLoaderUtil.loadServices(PropertySource.class, MethodHandles.lookup(), false, false).forEach(this.sources::add);
            this.reload();
        }

        private synchronized void reload() {
            this.literal.clear();
            this.normalized.clear();
            this.tokenized.clear();
            HashSet keys = new HashSet();
            this.sources.stream().map(PropertySource::getPropertyNames).reduce(keys, (left, right) -> {
                left.addAll(right);
                return left;
            });
            keys.stream().filter(Objects::nonNull).forEach(key -> {
                List<CharSequence> tokens = PropertySource.Util.tokenize(key);
                this.sources.forEach(source -> {
                    String normalValue;
                    CharSequence normalKey;
                    String value = source.getProperty((String)key);
                    if (value != null) {
                        this.literal.putIfAbsent((String)key, value);
                        if (!tokens.isEmpty()) {
                            this.tokenized.putIfAbsent(tokens, value);
                        }
                    }
                    if ((normalKey = source.getNormalForm(tokens)) != null && (normalValue = source.getProperty(normalKey.toString())) != null) {
                        this.normalized.putIfAbsent((String)key, normalValue);
                    }
                });
            });
        }

        private String get(String key) {
            if (this.normalized.containsKey(key)) {
                return this.normalized.get(key);
            }
            if (this.literal.containsKey(key)) {
                return this.literal.get(key);
            }
            List<CharSequence> tokens = PropertySource.Util.tokenize(key);
            for (PropertySource source : this.sources) {
                String normalKey = Objects.toString(source.getNormalForm(tokens), null);
                if (normalKey != null && source.containsProperty(normalKey)) {
                    String normalValue = source.getProperty(normalKey);
                    return normalValue;
                }
                if (!source.containsProperty(key)) continue;
                String value = source.getProperty(key);
                return value;
            }
            return this.tokenized.get(tokens);
        }

        private boolean containsKey(String key) {
            List<CharSequence> tokens = PropertySource.Util.tokenize(key);
            return this.normalized.containsKey(key) || this.literal.containsKey(key) || this.tokenized.containsKey(tokens) || this.sources.stream().anyMatch(s -> {
                CharSequence normalizedKey = s.getNormalForm(tokens);
                return s.containsProperty(key) || normalizedKey != null && s.containsProperty(normalizedKey.toString());
            });
        }
    }
}

