/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.nosql;

public interface NoSqlObject<W> {
    public void set(String var1, Object var2);

    public void set(String var1, NoSqlObject<W> var2);

    public void set(String var1, Object[] var2);

    public void set(String var1, NoSqlObject<W>[] var2);

    public W unwrap();
}

