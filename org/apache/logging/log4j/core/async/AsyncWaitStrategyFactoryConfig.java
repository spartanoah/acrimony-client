/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.async;

import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.async.AsyncWaitStrategyFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="AsyncWaitStrategyFactory", category="Core", printObject=true)
public class AsyncWaitStrategyFactoryConfig {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private final String factoryClassName;

    public AsyncWaitStrategyFactoryConfig(String factoryClassName) {
        this.factoryClassName = Objects.requireNonNull(factoryClassName, "factoryClassName");
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder().asBuilder();
    }

    public AsyncWaitStrategyFactory createWaitStrategyFactory() {
        try {
            Class<?> klass = Loader.loadClass(this.factoryClassName);
            if (AsyncWaitStrategyFactory.class.isAssignableFrom(klass)) {
                return (AsyncWaitStrategyFactory)klass.newInstance();
            }
            LOGGER.error("Ignoring factory '{}': it is not assignable to AsyncWaitStrategyFactory", (Object)this.factoryClassName);
            return null;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.info("Invalid implementation class name value: error creating AsyncWaitStrategyFactory {}: {}", (Object)this.factoryClassName, (Object)e);
            return null;
        }
    }

    public static class Builder<B extends Builder<B>>
    implements org.apache.logging.log4j.core.util.Builder<AsyncWaitStrategyFactoryConfig> {
        @PluginBuilderAttribute(value="class")
        @Required(message="AsyncWaitStrategyFactory cannot be configured without a factory class name")
        private String factoryClassName;

        public String getFactoryClassName() {
            return this.factoryClassName;
        }

        public B withFactoryClassName(String className) {
            this.factoryClassName = className;
            return this.asBuilder();
        }

        @Override
        public AsyncWaitStrategyFactoryConfig build() {
            return new AsyncWaitStrategyFactoryConfig(this.factoryClassName);
        }

        public B asBuilder() {
            return (B)this;
        }
    }
}

