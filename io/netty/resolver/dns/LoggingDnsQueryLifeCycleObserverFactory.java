/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.logging.LogLevel;
import io.netty.resolver.dns.DnsQueryLifecycleObserver;
import io.netty.resolver.dns.DnsQueryLifecycleObserverFactory;
import io.netty.resolver.dns.LoggingDnsQueryLifecycleObserver;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public final class LoggingDnsQueryLifeCycleObserverFactory
implements DnsQueryLifecycleObserverFactory {
    private static final InternalLogger DEFAULT_LOGGER = InternalLoggerFactory.getInstance(LoggingDnsQueryLifeCycleObserverFactory.class);
    private final InternalLogger logger;
    private final InternalLogLevel level;

    public LoggingDnsQueryLifeCycleObserverFactory() {
        this(LogLevel.DEBUG);
    }

    public LoggingDnsQueryLifeCycleObserverFactory(LogLevel level) {
        this.level = LoggingDnsQueryLifeCycleObserverFactory.checkAndConvertLevel(level);
        this.logger = DEFAULT_LOGGER;
    }

    public LoggingDnsQueryLifeCycleObserverFactory(Class<?> classContext, LogLevel level) {
        this.level = LoggingDnsQueryLifeCycleObserverFactory.checkAndConvertLevel(level);
        this.logger = InternalLoggerFactory.getInstance(ObjectUtil.checkNotNull(classContext, "classContext"));
    }

    public LoggingDnsQueryLifeCycleObserverFactory(String name, LogLevel level) {
        this.level = LoggingDnsQueryLifeCycleObserverFactory.checkAndConvertLevel(level);
        this.logger = InternalLoggerFactory.getInstance(ObjectUtil.checkNotNull(name, "name"));
    }

    private static InternalLogLevel checkAndConvertLevel(LogLevel level) {
        return ObjectUtil.checkNotNull(level, "level").toInternalLevel();
    }

    @Override
    public DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
        return new LoggingDnsQueryLifecycleObserver(question, this.logger, this.level);
    }
}

