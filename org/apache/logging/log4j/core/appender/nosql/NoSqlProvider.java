/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.nosql;

import org.apache.logging.log4j.core.appender.nosql.NoSqlConnection;
import org.apache.logging.log4j.core.appender.nosql.NoSqlObject;

public interface NoSqlProvider<C extends NoSqlConnection<?, ? extends NoSqlObject<?>>> {
    public C getConnection();

    public String toString();
}

