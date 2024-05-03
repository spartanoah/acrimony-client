/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.BlockingWaitStrategy
 *  com.lmax.disruptor.BusySpinWaitStrategy
 *  com.lmax.disruptor.SleepingWaitStrategy
 *  com.lmax.disruptor.WaitStrategy
 *  com.lmax.disruptor.YieldingWaitStrategy
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.async.AsyncWaitStrategyFactory;
import org.apache.logging.log4j.core.async.TimeoutBlockingWaitStrategy;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

class DefaultAsyncWaitStrategyFactory
implements AsyncWaitStrategyFactory {
    static final String DEFAULT_WAIT_STRATEGY_CLASSNAME = TimeoutBlockingWaitStrategy.class.getName();
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final String propertyName;

    public DefaultAsyncWaitStrategyFactory(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public WaitStrategy createWaitStrategy() {
        String strategyUp;
        String strategy = PropertiesUtil.getProperties().getStringProperty(this.propertyName, "TIMEOUT");
        LOGGER.trace("DefaultAsyncWaitStrategyFactory property {}={}", (Object)this.propertyName, (Object)strategy);
        switch (strategyUp = Strings.toRootUpperCase(strategy)) {
            case "SLEEP": {
                long sleepTimeNs = DefaultAsyncWaitStrategyFactory.parseAdditionalLongProperty(this.propertyName, "SleepTimeNs", 100L);
                String key = DefaultAsyncWaitStrategyFactory.getFullPropertyKey(this.propertyName, "Retries");
                int retries = PropertiesUtil.getProperties().getIntegerProperty(key, 200);
                LOGGER.trace("DefaultAsyncWaitStrategyFactory creating SleepingWaitStrategy(retries={}, sleepTimeNs={})", (Object)retries, (Object)sleepTimeNs);
                return new SleepingWaitStrategy(retries, sleepTimeNs);
            }
            case "YIELD": {
                LOGGER.trace("DefaultAsyncWaitStrategyFactory creating YieldingWaitStrategy");
                return new YieldingWaitStrategy();
            }
            case "BLOCK": {
                LOGGER.trace("DefaultAsyncWaitStrategyFactory creating BlockingWaitStrategy");
                return new BlockingWaitStrategy();
            }
            case "BUSYSPIN": {
                LOGGER.trace("DefaultAsyncWaitStrategyFactory creating BusySpinWaitStrategy");
                return new BusySpinWaitStrategy();
            }
            case "TIMEOUT": {
                return DefaultAsyncWaitStrategyFactory.createDefaultWaitStrategy(this.propertyName);
            }
        }
        return DefaultAsyncWaitStrategyFactory.createDefaultWaitStrategy(this.propertyName);
    }

    static WaitStrategy createDefaultWaitStrategy(String propertyName) {
        long timeoutMillis = DefaultAsyncWaitStrategyFactory.parseAdditionalLongProperty(propertyName, "Timeout", 10L);
        LOGGER.trace("DefaultAsyncWaitStrategyFactory creating TimeoutBlockingWaitStrategy(timeout={}, unit=MILLIS)", (Object)timeoutMillis);
        return new TimeoutBlockingWaitStrategy(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    private static String getFullPropertyKey(String strategyKey, String additionalKey) {
        if (strategyKey.startsWith("AsyncLogger.")) {
            return "AsyncLogger." + additionalKey;
        }
        if (strategyKey.startsWith("AsyncLoggerConfig.")) {
            return "AsyncLoggerConfig." + additionalKey;
        }
        return strategyKey + additionalKey;
    }

    private static long parseAdditionalLongProperty(String propertyName, String additionalKey, long defaultValue) {
        String key = DefaultAsyncWaitStrategyFactory.getFullPropertyKey(propertyName, additionalKey);
        return PropertiesUtil.getProperties().getLongProperty(key, defaultValue);
    }
}

