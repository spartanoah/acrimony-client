/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseAppender;
import org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig;
import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.appender.db.jdbc.JDBCDatabaseManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.Booleans;

@Plugin(name="JDBC", category="Core", elementType="appender", printObject=true)
public final class JDBCAppender
extends AbstractDatabaseAppender<JDBCDatabaseManager> {
    private final String description = this.getName() + "{ manager=" + this.getManager() + " }";

    private JDBCAppender(String name, Filter filter, boolean ignoreExceptions, JDBCDatabaseManager manager) {
        super(name, filter, ignoreExceptions, manager);
    }

    @Override
    public String toString() {
        return this.description;
    }

    @PluginFactory
    public static JDBCAppender createAppender(@PluginAttribute(value="name") String name, @PluginAttribute(value="ignoreExceptions") String ignore, @PluginElement(value="Filter") Filter filter, @PluginElement(value="ConnectionSource") ConnectionSource connectionSource, @PluginAttribute(value="bufferSize") String bufferSize, @PluginAttribute(value="tableName") String tableName, @PluginElement(value="ColumnConfigs") ColumnConfig[] columnConfigs) {
        int bufferSizeInt = AbstractAppender.parseInt(bufferSize, 0);
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        StringBuilder managerName = new StringBuilder("jdbcManager{ description=").append(name).append(", bufferSize=").append(bufferSizeInt).append(", connectionSource=").append(((Object)connectionSource).toString()).append(", tableName=").append(tableName).append(", columns=[ ");
        int i = 0;
        for (ColumnConfig column : columnConfigs) {
            if (i++ > 0) {
                managerName.append(", ");
            }
            managerName.append(column.toString());
        }
        managerName.append(" ] }");
        JDBCDatabaseManager manager = JDBCDatabaseManager.getJDBCDatabaseManager(managerName.toString(), bufferSizeInt, connectionSource, tableName, columnConfigs);
        if (manager == null) {
            return null;
        }
        return new JDBCAppender(name, filter, ignoreExceptions, manager);
    }
}

