/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.db.nosql;

import org.apache.logging.log4j.core.appender.db.nosql.NoSQLConnection;
import org.apache.logging.log4j.core.appender.db.nosql.NoSQLObject;

public interface NoSQLProvider<C extends NoSQLConnection<?, ? extends NoSQLObject<?>>> {
    public C getConnection();

    public String toString();
}

