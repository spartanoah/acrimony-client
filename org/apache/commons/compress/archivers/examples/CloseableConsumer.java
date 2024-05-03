/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.examples;

import java.io.Closeable;
import java.io.IOException;

public interface CloseableConsumer {
    public static final CloseableConsumer CLOSING_CONSUMER = Closeable::close;
    public static final CloseableConsumer NULL_CONSUMER = c -> {};

    public void accept(Closeable var1) throws IOException;
}

