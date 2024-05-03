/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;
import org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig;
import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.helpers.Closer;
import org.apache.logging.log4j.core.layout.PatternLayout;

public final class JDBCDatabaseManager
extends AbstractDatabaseManager {
    private static final JDBCDatabaseManagerFactory FACTORY = new JDBCDatabaseManagerFactory();
    private final List<Column> columns;
    private final ConnectionSource connectionSource;
    private final String sqlStatement;
    private Connection connection;
    private PreparedStatement statement;

    private JDBCDatabaseManager(String name, int bufferSize, ConnectionSource connectionSource, String sqlStatement, List<Column> columns) {
        super(name, bufferSize);
        this.connectionSource = connectionSource;
        this.sqlStatement = sqlStatement;
        this.columns = columns;
    }

    protected void connectInternal() throws SQLException {
        this.connection = this.connectionSource.getConnection();
        this.statement = this.connection.prepareStatement(this.sqlStatement);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void disconnectInternal() throws SQLException {
        try {
            Closer.close(this.statement);
        } finally {
            Closer.close(this.connection);
        }
    }

    @Override
    protected void writeInternal(LogEvent event) {
        StringReader reader = null;
        try {
            if (!this.isConnected() || this.connection == null || this.connection.isClosed()) {
                throw new AppenderLoggingException("Cannot write logging event; JDBC manager not connected to the database.");
            }
            int i = 1;
            for (Column column : this.columns) {
                if (column.isEventTimestamp) {
                    this.statement.setTimestamp(i++, new Timestamp(event.getMillis()));
                    continue;
                }
                if (column.isClob) {
                    reader = new StringReader(column.layout.toSerializable(event));
                    if (column.isUnicode) {
                        this.statement.setNClob(i++, reader);
                        continue;
                    }
                    this.statement.setClob(i++, reader);
                    continue;
                }
                if (column.isUnicode) {
                    this.statement.setNString(i++, column.layout.toSerializable(event));
                    continue;
                }
                this.statement.setString(i++, column.layout.toSerializable(event));
            }
            if (this.statement.executeUpdate() == 0) {
                throw new AppenderLoggingException("No records inserted in database table for log event in JDBC manager.");
            }
        } catch (SQLException e) {
            throw new AppenderLoggingException("Failed to insert record for log event in JDBC manager: " + e.getMessage(), e);
        } finally {
            Closer.closeSilent(reader);
        }
    }

    public static JDBCDatabaseManager getJDBCDatabaseManager(String name, int bufferSize, ConnectionSource connectionSource, String tableName, ColumnConfig[] columnConfigs) {
        return AbstractDatabaseManager.getManager(name, new FactoryData(bufferSize, connectionSource, tableName, columnConfigs), FACTORY);
    }

    private static final class Column {
        private final PatternLayout layout;
        private final boolean isEventTimestamp;
        private final boolean isUnicode;
        private final boolean isClob;

        private Column(PatternLayout layout, boolean isEventDate, boolean isUnicode, boolean isClob) {
            this.layout = layout;
            this.isEventTimestamp = isEventDate;
            this.isUnicode = isUnicode;
            this.isClob = isClob;
        }
    }

    private static final class JDBCDatabaseManagerFactory
    implements ManagerFactory<JDBCDatabaseManager, FactoryData> {
        private JDBCDatabaseManagerFactory() {
        }

        @Override
        public JDBCDatabaseManager createManager(String name, FactoryData data) {
            StringBuilder columnPart = new StringBuilder();
            StringBuilder valuePart = new StringBuilder();
            ArrayList<Column> columns = new ArrayList<Column>();
            int i = 0;
            for (ColumnConfig config : data.columnConfigs) {
                if (i++ > 0) {
                    columnPart.append(',');
                    valuePart.append(',');
                }
                columnPart.append(config.getColumnName());
                if (config.getLiteralValue() != null) {
                    valuePart.append(config.getLiteralValue());
                    continue;
                }
                columns.add(new Column(config.getLayout(), config.isEventTimestamp(), config.isUnicode(), config.isClob()));
                valuePart.append('?');
            }
            String sqlStatement = "INSERT INTO " + data.tableName + " (" + columnPart + ") VALUES (" + valuePart + ")";
            return new JDBCDatabaseManager(name, data.getBufferSize(), data.connectionSource, sqlStatement, columns);
        }
    }

    private static final class FactoryData
    extends AbstractDatabaseManager.AbstractFactoryData {
        private final ColumnConfig[] columnConfigs;
        private final ConnectionSource connectionSource;
        private final String tableName;

        protected FactoryData(int bufferSize, ConnectionSource connectionSource, String tableName, ColumnConfig[] columnConfigs) {
            super(bufferSize);
            this.connectionSource = connectionSource;
            this.tableName = tableName;
            this.columnConfigs = columnConfigs;
        }
    }
}

