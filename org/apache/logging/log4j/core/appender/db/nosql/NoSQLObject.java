/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.db.nosql;

public interface NoSQLObject<W> {
    public void set(String var1, Object var2);

    public void set(String var1, NoSQLObject<W> var2);

    public void set(String var1, Object[] var2);

    public void set(String var1, NoSQLObject<W>[] var2);

    public W unwrap();
}

