/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.nosql;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.core.appender.nosql.NoSqlConnection;
import org.apache.logging.log4j.core.appender.nosql.NoSqlObject;

public abstract class AbstractNoSqlConnection<W, T extends NoSqlObject<W>>
implements NoSqlConnection<W, T> {
    private final AtomicBoolean closed = new AtomicBoolean();

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            this.closeImpl();
        }
    }

    protected abstract void closeImpl();

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }
}

