/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.nosql;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;
import org.apache.logging.log4j.core.appender.nosql.NoSqlConnection;
import org.apache.logging.log4j.core.appender.nosql.NoSqlObject;
import org.apache.logging.log4j.core.appender.nosql.NoSqlProvider;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

public final class NoSqlDatabaseManager<W>
extends AbstractDatabaseManager {
    private static final NoSQLDatabaseManagerFactory FACTORY = new NoSQLDatabaseManagerFactory();
    private final NoSqlProvider<NoSqlConnection<W, ? extends NoSqlObject<W>>> provider;
    private NoSqlConnection<W, ? extends NoSqlObject<W>> connection;
    private final KeyValuePair[] additionalFields;

    @Deprecated
    public static NoSqlDatabaseManager<?> getNoSqlDatabaseManager(String name, int bufferSize, NoSqlProvider<?> provider) {
        return (NoSqlDatabaseManager)AbstractDatabaseManager.getManager(name, new FactoryData(null, bufferSize, provider, null), FACTORY);
    }

    public static NoSqlDatabaseManager<?> getNoSqlDatabaseManager(String name, int bufferSize, NoSqlProvider<?> provider, KeyValuePair[] additionalFields, Configuration configuration) {
        return (NoSqlDatabaseManager)AbstractDatabaseManager.getManager(name, new FactoryData(configuration, bufferSize, provider, additionalFields), FACTORY);
    }

    private NoSqlDatabaseManager(String name, int bufferSize, NoSqlProvider<NoSqlConnection<W, ? extends NoSqlObject<W>>> provider, KeyValuePair[] additionalFields, Configuration configuration) {
        super(name, bufferSize, null, configuration);
        this.provider = provider;
        this.additionalFields = additionalFields;
    }

    private NoSqlObject<W> buildMarkerEntity(Marker marker) {
        NoSqlObject<W> entity = this.connection.createObject();
        entity.set("name", marker.getName());
        Marker[] parents = marker.getParents();
        if (parents != null) {
            NoSqlObject[] parentEntities = new NoSqlObject[parents.length];
            for (int i = 0; i < parents.length; ++i) {
                parentEntities[i] = this.buildMarkerEntity(parents[i]);
            }
            entity.set("parents", parentEntities);
        }
        return entity;
    }

    @Override
    protected boolean commitAndClose() {
        return true;
    }

    @Override
    protected void connectAndStart() {
        try {
            this.connection = this.provider.getConnection();
        } catch (Exception e) {
            throw new AppenderLoggingException("Failed to get connection from NoSQL connection provider.", e);
        }
    }

    private NoSqlObject<W>[] convertStackTrace(StackTraceElement[] stackTrace) {
        NoSqlObject[] stackTraceEntities = this.connection.createList(stackTrace.length);
        for (int i = 0; i < stackTrace.length; ++i) {
            stackTraceEntities[i] = this.convertStackTraceElement(stackTrace[i]);
        }
        return stackTraceEntities;
    }

    private NoSqlObject<W> convertStackTraceElement(StackTraceElement element) {
        NoSqlObject<W> elementEntity = this.connection.createObject();
        elementEntity.set("className", element.getClassName());
        elementEntity.set("methodName", element.getMethodName());
        elementEntity.set("fileName", element.getFileName());
        elementEntity.set("lineNumber", element.getLineNumber());
        return elementEntity;
    }

    private void setAdditionalFields(NoSqlObject<W> entity) {
        if (this.additionalFields != null) {
            NoSqlObject object = this.connection.createObject();
            StrSubstitutor strSubstitutor = this.getStrSubstitutor();
            Stream.of(this.additionalFields).forEach(f -> object.set(f.getKey(), strSubstitutor != null ? strSubstitutor.replace(f.getValue()) : f.getValue()));
            entity.set("additionalFields", object);
        }
    }

    private void setFields(LogEvent event, NoSqlObject<W> entity) {
        entity.set("level", event.getLevel());
        entity.set("loggerName", event.getLoggerName());
        entity.set("message", event.getMessage() == null ? null : event.getMessage().getFormattedMessage());
        StackTraceElement source = event.getSource();
        if (source == null) {
            entity.set("source", (Object)null);
        } else {
            entity.set("source", this.convertStackTraceElement(source));
        }
        Marker marker = event.getMarker();
        if (marker == null) {
            entity.set("marker", (Object)null);
        } else {
            entity.set("marker", this.buildMarkerEntity(marker));
        }
        entity.set("threadId", event.getThreadId());
        entity.set("threadName", event.getThreadName());
        entity.set("threadPriority", event.getThreadPriority());
        entity.set("millis", event.getTimeMillis());
        entity.set("date", new Date(event.getTimeMillis()));
        Throwable thrown = event.getThrown();
        if (thrown == null) {
            entity.set("thrown", (Object)null);
        } else {
            NoSqlObject<W> originalExceptionEntity;
            NoSqlObject<W> exceptionEntity = originalExceptionEntity = this.connection.createObject();
            exceptionEntity.set("type", thrown.getClass().getName());
            exceptionEntity.set("message", thrown.getMessage());
            exceptionEntity.set("stackTrace", this.convertStackTrace(thrown.getStackTrace()));
            while (thrown.getCause() != null) {
                thrown = thrown.getCause();
                NoSqlObject<W> causingExceptionEntity = this.connection.createObject();
                causingExceptionEntity.set("type", thrown.getClass().getName());
                causingExceptionEntity.set("message", thrown.getMessage());
                causingExceptionEntity.set("stackTrace", this.convertStackTrace(thrown.getStackTrace()));
                exceptionEntity.set("cause", causingExceptionEntity);
                exceptionEntity = causingExceptionEntity;
            }
            entity.set("thrown", originalExceptionEntity);
        }
        ReadOnlyStringMap contextMap = event.getContextData();
        if (contextMap == null) {
            entity.set("contextMap", (Object)null);
        } else {
            NoSqlObject contextMapEntity = this.connection.createObject();
            contextMap.forEach((key, val2) -> contextMapEntity.set((String)key, val2));
            entity.set("contextMap", contextMapEntity);
        }
        ThreadContext.ContextStack contextStack = event.getContextStack();
        if (contextStack == null) {
            entity.set("contextStack", (Object)null);
        } else {
            entity.set("contextStack", contextStack.asList().toArray());
        }
    }

    private void setFields(MapMessage<?, ?> mapMessage, NoSqlObject<W> noSqlObject) {
        mapMessage.forEach((key, value) -> noSqlObject.set((String)key, value));
    }

    @Override
    protected boolean shutdownInternal() {
        return Closer.closeSilently(this.connection);
    }

    @Override
    protected void startupInternal() {
    }

    @Override
    protected void writeInternal(LogEvent event, Serializable serializable) {
        if (!this.isRunning() || this.connection == null || this.connection.isClosed()) {
            throw new AppenderLoggingException("Cannot write logging event; NoSQL manager not connected to the database.");
        }
        NoSqlObject<W> entity = this.connection.createObject();
        if (serializable instanceof MapMessage) {
            this.setFields((MapMessage)serializable, entity);
        } else {
            this.setFields(event, entity);
        }
        this.setAdditionalFields(entity);
        this.connection.insertObject(entity);
    }

    private static final class NoSQLDatabaseManagerFactory
    implements ManagerFactory<NoSqlDatabaseManager<?>, FactoryData> {
        private NoSQLDatabaseManagerFactory() {
        }

        @Override
        public NoSqlDatabaseManager<?> createManager(String name, FactoryData data) {
            Objects.requireNonNull(data, "data");
            return new NoSqlDatabaseManager(name, data.getBufferSize(), data.provider, data.additionalFields, data.getConfiguration());
        }
    }

    private static final class FactoryData
    extends AbstractDatabaseManager.AbstractFactoryData {
        private final NoSqlProvider<?> provider;
        private final KeyValuePair[] additionalFields;

        protected FactoryData(Configuration configuration, int bufferSize, NoSqlProvider<?> provider, KeyValuePair[] additionalFields) {
            super(configuration, bufferSize, null);
            this.provider = Objects.requireNonNull(provider, "provider");
            this.additionalFields = additionalFields;
        }
    }
}

