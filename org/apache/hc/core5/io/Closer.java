/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.io;

import java.io.Closeable;
import java.io.IOException;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;

public final class Closer {
    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    public static void close(ModalCloseable closeable, CloseMode closeMode) {
        if (closeable != null) {
            closeable.close(closeMode);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            Closer.close(closeable);
        } catch (IOException iOException) {
            // empty catch block
        }
    }
}

