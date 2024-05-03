/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.db.jdbc.AbstractConnectionSource;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="DataSource", category="Core", elementType="connectionSource", printObject=true)
public final class DataSourceConnectionSource
extends AbstractConnectionSource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final DataSource dataSource;
    private final String description;

    private DataSourceConnectionSource(String dataSourceName, DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.description = "dataSource{ name=" + dataSourceName + ", value=" + dataSource + " }";
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public String toString() {
        return this.description;
    }

    @PluginFactory
    public static DataSourceConnectionSource createConnectionSource(@PluginAttribute(value="jndiName") String jndiName) {
        if (!JndiManager.isJndiJdbcEnabled()) {
            LOGGER.error("JNDI must be enabled by setting log4j2.enableJndiJdbc=true");
            return null;
        }
        if (Strings.isEmpty(jndiName)) {
            LOGGER.error("No JNDI name provided.");
            return null;
        }
        try {
            DataSource dataSource = (DataSource)JndiManager.getDefaultManager(DataSourceConnectionSource.class.getCanonicalName()).lookup(jndiName);
            if (dataSource == null) {
                LOGGER.error("No DataSource found with JNDI name [" + jndiName + "].");
                return null;
            }
            return new DataSourceConnectionSource(jndiName, dataSource);
        } catch (NamingException e) {
            LOGGER.error(e.getMessage(), (Throwable)e);
            return null;
        }
    }
}

