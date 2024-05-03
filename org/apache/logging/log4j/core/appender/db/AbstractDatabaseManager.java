/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.db;

import java.io.Flushable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.config.Configuration;

public abstract class AbstractDatabaseManager
extends AbstractManager
implements Flushable {
    private final ArrayList<LogEvent> buffer;
    private final int bufferSize;
    private final Layout<? extends Serializable> layout;
    private boolean running;

    protected static <M extends AbstractDatabaseManager, T extends AbstractFactoryData> M getManager(String name, T data, ManagerFactory<M, T> factory) {
        return (M)((AbstractDatabaseManager)AbstractManager.getManager(name, factory, data));
    }

    @Deprecated
    protected AbstractDatabaseManager(String name, int bufferSize) {
        this(name, bufferSize, null);
    }

    @Deprecated
    protected AbstractDatabaseManager(String name, int bufferSize, Layout<? extends Serializable> layout) {
        this(name, bufferSize, layout, null);
    }

    protected AbstractDatabaseManager(String name, int bufferSize, Layout<? extends Serializable> layout, Configuration configuration) {
        super(configuration != null ? configuration.getLoggerContext() : null, name);
        this.bufferSize = bufferSize;
        this.buffer = new ArrayList(bufferSize + 1);
        this.layout = layout;
    }

    protected void buffer(LogEvent event) {
        this.buffer.add(event.toImmutable());
        if (this.buffer.size() >= this.bufferSize || event.isEndOfBatch()) {
            this.flush();
        }
    }

    protected abstract boolean commitAndClose();

    protected abstract void connectAndStart();

    @Override
    public final synchronized void flush() {
        if (this.isRunning() && this.isBuffered()) {
            this.connectAndStart();
            try {
                for (LogEvent event : this.buffer) {
                    this.writeInternal(event, this.layout != null ? this.layout.toSerializable(event) : null);
                }
            } finally {
                this.commitAndClose();
                this.buffer.clear();
            }
        }
    }

    protected boolean isBuffered() {
        return this.bufferSize > 0;
    }

    public final boolean isRunning() {
        return this.running;
    }

    @Override
    public final boolean releaseSub(long timeout, TimeUnit timeUnit) {
        return this.shutdown();
    }

    public final synchronized boolean shutdown() {
        boolean closed = true;
        this.flush();
        if (this.isRunning()) {
            try {
                closed &= this.shutdownInternal();
            } catch (Exception e) {
                this.logWarn("Caught exception while performing database shutdown operations", e);
                closed = false;
            } finally {
                this.running = false;
            }
        }
        return closed;
    }

    protected abstract boolean shutdownInternal() throws Exception;

    public final synchronized void startup() {
        if (!this.isRunning()) {
            try {
                this.startupInternal();
                this.running = true;
            } catch (Exception e) {
                this.logError("Could not perform database startup operations", e);
            }
        }
    }

    protected abstract void startupInternal() throws Exception;

    public final String toString() {
        return this.getName();
    }

    @Deprecated
    public final synchronized void write(LogEvent event) {
        this.write(event, null);
    }

    public final synchronized void write(LogEvent event, Serializable serializable) {
        if (this.isBuffered()) {
            this.buffer(event);
        } else {
            this.writeThrough(event, serializable);
        }
    }

    @Deprecated
    protected void writeInternal(LogEvent event) {
        this.writeInternal(event, null);
    }

    protected abstract void writeInternal(LogEvent var1, Serializable var2);

    protected void writeThrough(LogEvent event, Serializable serializable) {
        this.connectAndStart();
        try {
            this.writeInternal(event, serializable);
        } finally {
            this.commitAndClose();
        }
    }

    protected static abstract class AbstractFactoryData
    extends AbstractManager.AbstractFactoryData {
        private final int bufferSize;
        private final Layout<? extends Serializable> layout;

        protected AbstractFactoryData(int bufferSize, Layout<? extends Serializable> layout) {
            this(null, bufferSize, layout);
        }

        protected AbstractFactoryData(Configuration configuration, int bufferSize, Layout<? extends Serializable> layout) {
            super(configuration);
            this.bufferSize = bufferSize;
            this.layout = layout;
        }

        public int getBufferSize() {
            return this.bufferSize;
        }

        public Layout<? extends Serializable> getLayout() {
            return this.layout;
        }
    }
}

