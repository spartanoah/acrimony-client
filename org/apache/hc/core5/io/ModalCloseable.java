/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.io;

import java.io.Closeable;
import org.apache.hc.core5.io.CloseMode;

public interface ModalCloseable
extends Closeable {
    public void close(CloseMode var1);
}

