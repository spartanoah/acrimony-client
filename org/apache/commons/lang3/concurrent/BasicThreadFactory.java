/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class BasicThreadFactory
implements ThreadFactory {
    private final AtomicLong threadCounter;
    private final ThreadFactory wrappedFactory;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private final String namingPattern;
    private final Integer priority;
    private final Boolean daemonFlag;

    private BasicThreadFactory(Builder builder) {
        this.wrappedFactory = builder.wrappedFactory == null ? Executors.defaultThreadFactory() : builder.wrappedFactory;
        this.namingPattern = builder.namingPattern;
        this.priority = builder.priority;
        this.daemonFlag = builder.daemonFlag;
        this.uncaughtExceptionHandler = builder.exceptionHandler;
        this.threadCounter = new AtomicLong();
    }

    public final ThreadFactory getWrappedFactory() {
        return this.wrappedFactory;
    }

    public final String getNamingPattern() {
        return this.namingPattern;
    }

    public final Boolean getDaemonFlag() {
        return this.daemonFlag;
    }

    public final Integer getPriority() {
        return this.priority;
    }

    public final Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.uncaughtExceptionHandler;
    }

    public long getThreadCount() {
        return this.threadCounter.get();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = this.getWrappedFactory().newThread(r);
        this.initializeThread(t);
        return t;
    }

    private void initializeThread(Thread t) {
        if (this.getNamingPattern() != null) {
            Long count = this.threadCounter.incrementAndGet();
            t.setName(String.format(this.getNamingPattern(), count));
        }
        if (this.getUncaughtExceptionHandler() != null) {
            t.setUncaughtExceptionHandler(this.getUncaughtExceptionHandler());
        }
        if (this.getPriority() != null) {
            t.setPriority(this.getPriority());
        }
        if (this.getDaemonFlag() != null) {
            t.setDaemon(this.getDaemonFlag());
        }
    }

    public static class Builder
    implements org.apache.commons.lang3.builder.Builder<BasicThreadFactory> {
        private ThreadFactory wrappedFactory;
        private Thread.UncaughtExceptionHandler exceptionHandler;
        private String namingPattern;
        private Integer priority;
        private Boolean daemonFlag;

        public Builder wrappedFactory(ThreadFactory factory) {
            if (factory == null) {
                throw new NullPointerException("Wrapped ThreadFactory must not be null!");
            }
            this.wrappedFactory = factory;
            return this;
        }

        public Builder namingPattern(String pattern) {
            if (pattern == null) {
                throw new NullPointerException("Naming pattern must not be null!");
            }
            this.namingPattern = pattern;
            return this;
        }

        public Builder daemon(boolean f) {
            this.daemonFlag = f;
            return this;
        }

        public Builder priority(int prio) {
            this.priority = prio;
            return this;
        }

        public Builder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
            if (handler == null) {
                throw new NullPointerException("Uncaught exception handler must not be null!");
            }
            this.exceptionHandler = handler;
            return this;
        }

        public void reset() {
            this.wrappedFactory = null;
            this.exceptionHandler = null;
            this.namingPattern = null;
            this.priority = null;
            this.daemonFlag = null;
        }

        @Override
        public BasicThreadFactory build() {
            BasicThreadFactory factory = new BasicThreadFactory(this);
            this.reset();
            return factory;
        }
    }
}

