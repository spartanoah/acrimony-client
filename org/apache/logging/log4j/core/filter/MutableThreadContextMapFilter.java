/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.filter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.filter.ThreadContextMapFilter;
import org.apache.logging.log4j.core.filter.mutable.KeyValuePairConfig;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.internal.HttpInputStreamUtil;
import org.apache.logging.log4j.core.util.internal.LastModifiedSource;
import org.apache.logging.log4j.core.util.internal.Status;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PropertiesUtil;

@Plugin(name="MutableThreadContextMapFilter", category="Core", elementType="filter", printObject=true)
@PluginAliases(value={"MutableContextMapFilter"})
@PerformanceSensitive(value={"allocation"})
public class MutableThreadContextMapFilter
extends AbstractFilter {
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final KeyValuePair[] EMPTY_ARRAY = new KeyValuePair[0];
    private volatile Filter filter;
    private final long pollInterval;
    private final ConfigurationScheduler scheduler;
    private final LastModifiedSource source;
    private final AuthorizationProvider authorizationProvider;
    private final List<FilterConfigUpdateListener> listeners = new ArrayList<FilterConfigUpdateListener>();
    private ScheduledFuture<?> future = null;

    private MutableThreadContextMapFilter(Filter filter, LastModifiedSource source, long pollInterval, AuthorizationProvider authorizationProvider, Filter.Result onMatch, Filter.Result onMismatch, Configuration configuration) {
        super(onMatch, onMismatch);
        this.filter = filter;
        this.pollInterval = pollInterval;
        this.source = source;
        this.scheduler = configuration.getScheduler();
        this.authorizationProvider = authorizationProvider;
    }

    @Override
    public void start() {
        if (this.pollInterval > 0L) {
            this.future = this.scheduler.scheduleWithFixedDelay(new FileMonitor(), 0L, this.pollInterval, TimeUnit.SECONDS);
            LOGGER.debug("Watching {} with poll interval {}", (Object)this.source.toString(), (Object)this.pollInterval);
        }
        super.start();
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        this.future.cancel(true);
        return super.stop(timeout, timeUnit);
    }

    public void registerListener(FilterConfigUpdateListener listener) {
        this.listeners.add(listener);
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Filter.Result filter(LogEvent event) {
        return this.filter.filter(event);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return this.filter.filter(logger, level, marker, msg, t);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return this.filter.filter(logger, level, marker, msg, t);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object ... params) {
        return this.filter.filter(logger, level, marker, msg, params);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) {
        return this.filter.filter(logger, level, marker, msg, p0);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) {
        return this.filter.filter(logger, level, marker, msg, p0, p1);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3, p4);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.filter.filter(logger, level, marker, msg, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    private static LastModifiedSource getSource(String configLocation) {
        LastModifiedSource source = null;
        try {
            URI uri = new URI(configLocation);
            source = uri.getScheme() != null ? new LastModifiedSource(new URI(configLocation)) : new LastModifiedSource(new File(configLocation));
        } catch (Exception ex) {
            source = new LastModifiedSource(new File(configLocation));
        }
        return source;
    }

    private static ConfigResult getConfig(LastModifiedSource source, AuthorizationProvider authorizationProvider) {
        ConfigResult configResult;
        block22: {
            HttpInputStreamUtil.Result result;
            InputStream inputStream;
            block21: {
                File inputFile = source.getFile();
                inputStream = null;
                result = null;
                long lastModified = source.getLastModified();
                if (inputFile != null && inputFile.exists()) {
                    try {
                        long modified = inputFile.lastModified();
                        if (modified > lastModified) {
                            source.setLastModified(modified);
                            inputStream = new FileInputStream(inputFile);
                            result = new HttpInputStreamUtil.Result(Status.SUCCESS);
                            break block21;
                        }
                        result = new HttpInputStreamUtil.Result(Status.NOT_MODIFIED);
                    } catch (Exception ex) {
                        result = new HttpInputStreamUtil.Result(Status.ERROR);
                    }
                } else if (source.getURI() != null) {
                    try {
                        result = HttpInputStreamUtil.getInputStream(source, authorizationProvider);
                        inputStream = result.getInputStream();
                    } catch (ConfigurationException ex) {
                        result = new HttpInputStreamUtil.Result(Status.ERROR);
                    }
                } else {
                    result = new HttpInputStreamUtil.Result(Status.NOT_FOUND);
                }
            }
            configResult = new ConfigResult();
            if (result.getStatus() == Status.SUCCESS) {
                LOGGER.debug("Processing Debug key/value pairs from: {}", (Object)source.toString());
                try {
                    KeyValuePairConfig keyValuePairConfig = MAPPER.readValue(inputStream, KeyValuePairConfig.class);
                    if (keyValuePairConfig != null) {
                        Map<String, String[]> configs = keyValuePairConfig.getConfigs();
                        if (configs != null && configs.size() > 0) {
                            ArrayList<KeyValuePair> pairs = new ArrayList<KeyValuePair>();
                            for (Map.Entry<String, String[]> entry : configs.entrySet()) {
                                String key = entry.getKey();
                                for (String value : entry.getValue()) {
                                    if (value != null) {
                                        pairs.add(new KeyValuePair(key, value));
                                        continue;
                                    }
                                    LOGGER.warn("Ignoring null value for {}", (Object)key);
                                }
                            }
                            if (pairs.size() > 0) {
                                configResult.pairs = pairs.toArray(EMPTY_ARRAY);
                                configResult.status = Status.SUCCESS;
                            } else {
                                configResult.status = Status.EMPTY;
                            }
                        } else {
                            LOGGER.debug("No configuration data in {}", (Object)source.toString());
                            configResult.status = Status.EMPTY;
                        }
                        break block22;
                    }
                    LOGGER.warn("No configs element in MutableThreadContextMapFilter configuration");
                    configResult.status = Status.ERROR;
                } catch (Exception ex) {
                    LOGGER.warn("Invalid key/value pair configuration, input ignored: {}", (Object)ex.getMessage());
                    configResult.status = Status.ERROR;
                }
            } else {
                configResult.status = result.getStatus();
            }
        }
        return configResult;
    }

    private static class ConfigResult
    extends HttpInputStreamUtil.Result {
        public KeyValuePair[] pairs;
        public Status status;

        private ConfigResult() {
        }
    }

    public static interface FilterConfigUpdateListener {
        public void onEvent();
    }

    private static class NoOpFilter
    extends AbstractFilter {
        public NoOpFilter() {
            super(Filter.Result.NEUTRAL, Filter.Result.NEUTRAL);
        }
    }

    private class FileMonitor
    implements Runnable {
        private FileMonitor() {
        }

        @Override
        public void run() {
            ConfigResult result = MutableThreadContextMapFilter.getConfig(MutableThreadContextMapFilter.this.source, MutableThreadContextMapFilter.this.authorizationProvider);
            if (result.status == Status.SUCCESS) {
                MutableThreadContextMapFilter.this.filter = ThreadContextMapFilter.createFilter(result.pairs, "or", MutableThreadContextMapFilter.this.getOnMatch(), MutableThreadContextMapFilter.this.getOnMismatch());
                LOGGER.info("Filter configuration was updated: {}", (Object)MutableThreadContextMapFilter.this.filter.toString());
                for (FilterConfigUpdateListener listener : MutableThreadContextMapFilter.this.listeners) {
                    listener.onEvent();
                }
            } else if (result.status == Status.NOT_FOUND) {
                if (!(MutableThreadContextMapFilter.this.filter instanceof NoOpFilter)) {
                    LOGGER.info("Filter configuration was removed");
                    MutableThreadContextMapFilter.this.filter = new NoOpFilter();
                    for (FilterConfigUpdateListener listener : MutableThreadContextMapFilter.this.listeners) {
                        listener.onEvent();
                    }
                }
            } else if (result.status == Status.EMPTY) {
                LOGGER.debug("Filter configuration is empty");
                MutableThreadContextMapFilter.this.filter = new NoOpFilter();
            }
        }
    }

    public static class Builder
    extends AbstractFilter.AbstractFilterBuilder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<MutableThreadContextMapFilter> {
        @PluginBuilderAttribute
        private String configLocation;
        @PluginBuilderAttribute
        private long pollInterval;
        @PluginConfiguration
        private Configuration configuration;

        public Builder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder setPollInterval(long pollInterval) {
            this.pollInterval = pollInterval;
            return this;
        }

        public Builder setConfigLocation(String configLocation) {
            this.configLocation = configLocation;
            return this;
        }

        @Override
        public MutableThreadContextMapFilter build() {
            AbstractFilter filter;
            LastModifiedSource source = MutableThreadContextMapFilter.getSource(this.configLocation);
            if (source == null) {
                return new MutableThreadContextMapFilter(new NoOpFilter(), null, 0L, null, this.getOnMatch(), this.getOnMismatch(), this.configuration);
            }
            AuthorizationProvider authorizationProvider = ConfigurationFactory.authorizationProvider(PropertiesUtil.getProperties());
            if (this.pollInterval <= 0L) {
                ConfigResult result = MutableThreadContextMapFilter.getConfig(source, authorizationProvider);
                if (result.status == Status.SUCCESS) {
                    filter = result.pairs.length > 0 ? ThreadContextMapFilter.createFilter(result.pairs, "or", this.getOnMatch(), this.getOnMismatch()) : new NoOpFilter();
                } else if (result.status == Status.NOT_FOUND || result.status == Status.EMPTY) {
                    filter = new NoOpFilter();
                } else {
                    LOGGER.warn("Unexpected response returned on initial call: {}", (Object)result.status);
                    filter = new NoOpFilter();
                }
            } else {
                filter = new NoOpFilter();
            }
            if (this.pollInterval > 0L) {
                this.configuration.getScheduler().incrementScheduledItems();
            }
            return new MutableThreadContextMapFilter(filter, source, this.pollInterval, authorizationProvider, this.getOnMatch(), this.getOnMismatch(), this.configuration);
        }
    }
}

